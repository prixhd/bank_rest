package com.example.bankcards.dto;

import com.example.bankcards.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "username cant be empty")
    @Size(min = 3, max = 50, message = "username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "password cant be empty")
    @Size(min = 6, message = "password must be min 6 characters")
    private String password;

    @NotBlank(message = "email cant be empty")
    @Email(message = "email entered incorrectly")
    private String email;

    @NotBlank(message = "firstname cant be empty")
    @Size(max = 100, message = "firstname shouldnt be more than 100 characters")
    private String firstName;

    @NotBlank(message = "lastname cant be empty")
    @Size(max = 100, message = "lastname shouldnt be more than 100 characters")
    private String lastName;

    @NotNull(message = "role should be specified")
    private Role role;

}
