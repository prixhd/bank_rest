// src/main/java/com/example/bankcards/service/TransactionService.java
package com.example.bankcards.service;

import com.example.bankcards.dto.TransactionDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.TransactionStatus;
import com.example.bankcards.exception.InsufficientBalanceException;
import com.example.bankcards.exception.ValidationException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardService cardService;
    private final CardRepository cardRepository;


    @Transactional
    public TransactionDto transferBetweenCards(TransferRequest request, Long userId) {
        log.info("Инициирован перевод от карты {} к карте {} на сумму {} для пользователя {}",
                request.getFromCardId(), request.getToCardId(), request.getAmount(), userId);

        Card fromCard = cardService.getCardEntity(request.getFromCardId());
        Card toCard = cardService.getCardEntity(request.getToCardId());

        validateCardOwnership(fromCard, userId);
        validateCardOwnership(toCard, userId);

        if (fromCard.getId().equals(toCard.getId())) {
            throw new ValidationException("Нельзя переводить средства на ту же карту");
        }

        validateCardStatus(fromCard, "отправителя");
        validateCardStatus(toCard, "получателя");

        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    String.format("Недостаточно средств на карте. Доступно: %s, требуется: %s",
                            fromCard.getBalance(), request.getAmount()));
        }

        fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
        toCard.setBalance(toCard.getBalance().add(request.getAmount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        Transaction transaction = new Transaction();
        transaction.setFromCard(fromCard);
        transaction.setToCard(toCard);
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setTransactionDate(LocalDateTime.now());

        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Перевод успешно выполнен. ID транзакции: {}", savedTransaction.getId());

        return convertToDto(savedTransaction);
    }


    public Page<TransactionDto> getCardTransactions(Long cardId, Long userId, Pageable pageable) {
        log.debug("Получение истории транзакций для карты {} пользователя {}", cardId, userId);

        Card card = cardService.getCardEntity(cardId);
        validateCardOwnership(card, userId);

        Page<Transaction> transactions = transactionRepository.findByFromCardOrToCard(card, pageable);
        return transactions.map(this::convertToDto);
    }


    public Page<TransactionDto> getUserTransactions(Long userId, Pageable pageable) {
        log.debug("Получение всех транзакций пользователя {}", userId);

        Page<Transaction> transactions = transactionRepository.findByUserId(userId, pageable);
        return transactions.map(this::convertToDto);
    }


    private void validateCardOwnership(Card card, Long userId) {
        if (!card.getOwner().getId().equals(userId)) {
            throw new ValidationException("Карта не принадлежит указанному пользователю");
        }
    }


    private void validateCardStatus(Card card, String cardType) {
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new ValidationException(
                    String.format("Карта %s должна быть активна для выполнения операции. Текущий статус: %s",
                            cardType, card.getStatus()));
        }
    }


    private TransactionDto convertToDto(Transaction transaction) {
        TransactionDto dto = new TransactionDto();
        dto.setId(transaction.getId());
        dto.setFromCardMasked(transaction.getFromCard().getMaskedNumber());
        dto.setToCardMasked(transaction.getToCard().getMaskedNumber());
        dto.setAmount(transaction.getAmount());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setDescription(transaction.getDescription());
        dto.setStatus(transaction.getStatus());
        return dto;
    }
}