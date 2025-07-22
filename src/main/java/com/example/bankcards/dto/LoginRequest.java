package com.example.bankcards.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "username cant be empty")
    private String username;

    @NotBlank(message = "password cant be empty")
    private String password;
}
