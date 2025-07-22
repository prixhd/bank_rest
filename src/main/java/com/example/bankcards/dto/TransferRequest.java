package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    @NotNull(message = "ID карты отправителя должен быть указан")
    private Long fromCardId;

    @NotNull(message = "ID карты получателя должен быть указан")
    private Long toCardId;

    @NotNull(message = "Сумма перевода должна быть указана")
    @Positive(message = "Сумма перевода должна быть положительной")
    private BigDecimal amount;

    @Size(max = 500, message = "Описание не должно превышать 500 символов")
    private String description;
}
