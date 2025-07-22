package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCardRequest {

    @NotNull(message = "user id should be listed")
    private Long userId;

    @Positive(message = "Начальный баланс должен быть положительным")
    private BigDecimal initialBalance = BigDecimal.ZERO;
}
