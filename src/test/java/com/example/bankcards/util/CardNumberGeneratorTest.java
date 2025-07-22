package com.example.bankcards.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class CardNumberGeneratorTest {

    private final CardNumberGenerator generator = new CardNumberGenerator();

    @Test
    void generateCardNumber_ShouldReturnValidLength() {
        String cardNumber = generator.generateCardNumber();

        assertEquals(16, cardNumber.length());
        assertTrue(cardNumber.startsWith("4"));
    }

    @Test
    void generateCardNumber_ShouldPassLuhnCheck() {
        String cardNumber = generator.generateCardNumber();

        assertTrue(generator.isValidCardNumber(cardNumber));
    }

    @Test
    void isValidCardNumber_ValidCard() {
        String validCard = "4532015112830366";

        assertTrue(generator.isValidCardNumber(validCard));
    }

    @Test
    void isValidCardNumber_InvalidCard() {
        String invalidCard = "1234567890123456";

        assertFalse(generator.isValidCardNumber(invalidCard));
    }

    @Test
    void isValidCardNumber_WrongLength() {
        String wrongLength = "123456789012345";

        assertFalse(generator.isValidCardNumber(wrongLength));
    }

    @Test
    void isValidCardNumber_Null() {
        assertFalse(generator.isValidCardNumber(null));
    }
}