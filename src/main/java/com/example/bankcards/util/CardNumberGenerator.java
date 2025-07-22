package com.example.bankcards.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;


@Component
public class CardNumberGenerator {

    private final SecureRandom random = new SecureRandom();


    public String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder();

        cardNumber.append("4");

        for (int i = 0; i < 14; i++) {
            cardNumber.append(random.nextInt(10));
        }

        int checkDigit = calculateLuhnCheckDigit(cardNumber.toString());
        cardNumber.append(checkDigit);

        return cardNumber.toString();
    }


    private int calculateLuhnCheckDigit(String cardNumber) {
        int sum = 0;
        boolean doubleDigit = true;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));

            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit = digit / 10 + digit % 10;
                }
            }

            sum += digit;
            doubleDigit = !doubleDigit;
        }


        return (10 - (sum % 10)) % 10;
    }


    public boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16) {
            return false;
        }

        try {
            int sum = 0;
            boolean doubleDigit = false;

            for (int i = cardNumber.length() - 1; i >= 0; i--) {
                int digit = Character.getNumericValue(cardNumber.charAt(i));

                if (doubleDigit) {
                    digit *= 2;
                    if (digit > 9) {
                        digit = digit / 10 + digit % 10;
                    }
                }

                sum += digit;
                doubleDigit = !doubleDigit;
            }

            return sum % 10 == 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}