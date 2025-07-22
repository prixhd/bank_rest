package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.exception.ValidationException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardNumberGenerator;
import com.example.bankcards.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CardService {

    private final CardRepository cardRepository;
    private final UserService userService;
    private final CardNumberGenerator cardNumberGenerator;
    private final EncryptionUtil encryptionUtil;


    @Transactional
    public CardDto createCard(CreateCardRequest request) {
        log.info("Создание новой карты для пользователя ID: {}", request.getUserId());

        User user = userService.getUserEntity(request.getUserId());

        long activeCardsCount = cardRepository.countActiveCardsByOwner(user);
        if (activeCardsCount >= 5) {
            throw new ValidationException("Превышен лимит активных карт (максимум 5)");
        }

        String cardNumber;
        String encryptedNumber;
        int attempts = 0;
        final int maxAttempts = 10;

        do {
            cardNumber = cardNumberGenerator.generateCardNumber();
            encryptedNumber = encryptionUtil.encrypt(cardNumber);
            attempts++;

            if (attempts >= maxAttempts) {
                throw new RuntimeException("Не удалось сгенерировать уникальный номер карты");
            }
        } while (cardRepository.findByCardNumber(encryptedNumber).isPresent());

        String maskedNumber = maskCardNumber(cardNumber);

        Card card = new Card();
        card.setCardNumber(encryptedNumber);
        card.setMaskedNumber(maskedNumber);
        card.setOwner(user);
        card.setExpiryDate(LocalDate.now().plusYears(3).atStartOfDay());
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(request.getInitialBalance());

        Card savedCard = cardRepository.save(card);
        log.info("Карта успешно создана с ID: {} для пользователя: {}",
                savedCard.getId(), user.getUsername());

        return convertToDto(savedCard);
    }


    public Page<CardDto> getUserCards(Long userId, CardStatus status, Pageable pageable) {
        log.debug("Получение карт пользователя ID: {}, статус: {}", userId, status);

        User user = userService.getUserEntity(userId);

        Page<Card> cards = cardRepository.findByOwnerWithStatusFilter(user, status, pageable);
        return cards.map(this::convertToDto);
    }


    public CardDto getCardById(Long cardId) {
        log.debug("Поиск карты по ID: {}", cardId);
        Card card = getCardEntity(cardId);
        return convertToDto(card);
    }


    public CardDto getUserCardById(Long userId, Long cardId) {
        log.debug("Поиск карты ID: {} для пользователя ID: {}", cardId, userId);

        Card card = getCardEntity(cardId);

        if (!card.getOwner().getId().equals(userId)) {
            throw new ValidationException("Карта не принадлежит указанному пользователю");
        }

        return convertToDto(card);
    }


    public Page<CardDto> getAllCards(Pageable pageable) {
        log.debug("Получение всех карт");
        return cardRepository.findAll(pageable)
                .map(this::convertToDto);
    }


    @Transactional
    public CardDto blockCard(Long cardId) {
        log.info("Блокировка карты ID: {}", cardId);
        Card card = getCardEntity(cardId);

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new ValidationException("Карта уже заблокирована");
        }

        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new ValidationException("Нельзя заблокировать карту с истекшим сроком действия");
        }

        card.setStatus(CardStatus.BLOCKED);
        Card savedCard = cardRepository.save(card);

        log.info("Карта {} успешно заблокирована", cardId);
        return convertToDto(savedCard);
    }


    @Transactional
    public CardDto activateCard(Long cardId) {
        log.info("Активация карты ID: {}", cardId);
        Card card = getCardEntity(cardId);

        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new ValidationException("Карта уже активна");
        }

        if (card.getExpiryDate().isBefore(LocalDate.now().atStartOfDay())) {
            throw new ValidationException("Нельзя активировать карту с истекшим сроком действия");
        }

        card.setStatus(CardStatus.ACTIVE);
        Card savedCard = cardRepository.save(card);

        log.info("Карта {} успешно активирована", cardId);
        return convertToDto(savedCard);
    }


    @Transactional
    public void deleteCard(Long cardId) {
        log.info("Удаление карты ID: {}", cardId);
        Card card = getCardEntity(cardId);

        if (card.getBalance().compareTo(java.math.BigDecimal.ZERO) != 0) {
            throw new ValidationException("Нельзя удалить карту с ненулевым балансом. Текущий баланс: " + card.getBalance());
        }

        cardRepository.delete(card);
        log.info("Карта {} успешно удалена", cardId);
    }


    @Transactional
    public void updateExpiredCardsStatus() {
        log.info("Обновление статусов просроченных карт");

        LocalDate currentDate = LocalDate.now();

        cardRepository.findAll().stream()
                .filter(card -> card.getStatus() == CardStatus.ACTIVE)
                .filter(card -> card.getExpiryDate().isBefore(currentDate.atStartOfDay()))
                .forEach(card -> {
                    card.setStatus(CardStatus.EXPIRED);
                    cardRepository.save(card);
                    log.info("Карта {} помечена как просроченная", card.getId());
                });
    }


    public Card getCardEntity(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Карта с ID " + cardId + " не найдена"));
    }


    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16) {
            throw new IllegalArgumentException("Номер карты должен содержать 16 цифр");
        }
        return "**** **** **** " + cardNumber.substring(12);
    }


    private CardDto convertToDto(Card card) {
        CardDto dto = new CardDto();
        dto.setId(card.getId());
        dto.setMaskedNumber(card.getMaskedNumber());
        dto.setOwnerName(card.getOwner().getFirstName() + " " + card.getOwner().getLastName());
        dto.setExpiryDate(LocalDate.from(card.getExpiryDate()));
        dto.setStatus(card.getStatus());
        dto.setBalance(card.getBalance());
        dto.setCreatedAt(card.getCreatedAt());
        return dto;
    }
}