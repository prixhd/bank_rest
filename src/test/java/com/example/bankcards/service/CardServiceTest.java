package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ValidationException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardNumberGenerator;
import com.example.bankcards.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserService userService;

    @Mock
    private CardNumberGenerator cardNumberGenerator;

    @Mock
    private EncryptionUtil encryptionUtil;

    @InjectMocks
    private CardService cardService;

    private User testUser;
    private CreateCardRequest createCardRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(Role.USER);

        createCardRequest = new CreateCardRequest();
        createCardRequest.setUserId(1L);
        createCardRequest.setInitialBalance(BigDecimal.valueOf(1000));
    }

    @Test
    void createCard_Success() {
        String cardNumber = "1234567890123456";
        String encryptedNumber = "encrypted123";

        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(cardRepository.countActiveCardsByOwner(testUser)).thenReturn(0L);
        when(cardNumberGenerator.generateCardNumber()).thenReturn(cardNumber);
        when(encryptionUtil.encrypt(cardNumber)).thenReturn(encryptedNumber);
        when(cardRepository.findByCardNumber(encryptedNumber)).thenReturn(Optional.empty());

        Card savedCard = new Card();
        savedCard.setId(1L);
        savedCard.setCardNumber(encryptedNumber);
        savedCard.setMaskedNumber("**** **** **** 3456");
        savedCard.setOwner(testUser);
        savedCard.setStatus(CardStatus.ACTIVE);
        savedCard.setBalance(BigDecimal.valueOf(1000));
        savedCard.setCreatedAt(LocalDateTime.now());

        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);

        CardDto result = cardService.createCard(createCardRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("**** **** **** 3456", result.getMaskedNumber());
        assertEquals(CardStatus.ACTIVE, result.getStatus());
        assertEquals(BigDecimal.valueOf(1000), result.getBalance());

        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void createCard_ExceedsLimit() {
        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(cardRepository.countActiveCardsByOwner(testUser)).thenReturn(5L);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> cardService.createCard(createCardRequest)
        );

        assertEquals("Превышен лимит активных карт (максимум 5)", exception.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void blockCard_Success() {
        Card card = new Card();
        card.setId(1L);
        card.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        CardDto result = cardService.blockCard(1L);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void blockCard_AlreadyBlocked() {
        Card card = new Card();
        card.setId(1L);
        card.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> cardService.blockCard(1L)
        );

        assertEquals("Карта уже заблокирована", exception.getMessage());
    }
}