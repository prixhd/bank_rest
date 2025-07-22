package com.example.bankcards.service;

import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ValidationException;
import com.example.bankcards.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public JwtResponse authenticate(LoginRequest loginRequest) {
        log.info("Попытка аутентификации пользователя: {}", loginRequest.getUsername());

        try {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    );

            Authentication authentication = authenticationManager.authenticate(authToken);

            User user = (User) authentication.getPrincipal();

            String token = jwtUtil.generateToken(user);

            UserDto userDto = convertToUserDto(user);

            log.info("Пользователь {} успешно аутентифицирован", loginRequest.getUsername());

            return new JwtResponse(token, userDto);

        } catch (AuthenticationException e) {
            log.error("Ошибка аутентификации для пользователя {}: {}",
                    loginRequest.getUsername(), e.getMessage());
            throw new ValidationException("Неверный логин или пароль");
        }
    }

    private UserDto convertToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setIsEnabled(user.getIsEnabled());
        return dto;
    }
}