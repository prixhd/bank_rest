package com.example.bankcards.service;

import com.example.bankcards.dto.CreateUserRequest;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.exception.ValidationException;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        log.info("Создание нового пользователя: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("Пользователь с таким username уже существует");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(request.getRole());
        user.setIsEnabled(true);

        User savedUser = userRepository.save(user);
        log.info("Пользователь успешно создан с ID: {}", savedUser.getId());

        return convertToDto(savedUser);
    }

    public Page<UserDto> getAllUsers(Pageable pageable) {
        log.debug("Получение списка пользователей, страница: {}", pageable.getPageNumber());
        return userRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    public UserDto getUserById(Long id) {
        log.debug("Поиск пользователя по ID: {}", id);
        User user = getUserEntity(id);
        return convertToDto(user);
    }

    public User getUserEntity(Long id) {
        log.debug("Получение сущности пользователя по ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с ID " + id + " не найден"));
    }

    public User getUserByUsername(String username) {
        log.debug("Поиск пользователя по username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с username " + username + " не найден"));
    }

    public UserDto getUserDtoByUsername(String username) {
        log.debug("Поиск пользователя DTO по username: {}", username);
        User user = getUserByUsername(username);
        return convertToDto(user);
    }

    @Transactional
    public UserDto toggleUserStatus(Long userId) {
        log.info("Изменение статуса пользователя ID: {}", userId);
        User user = getUserEntity(userId);

        user.setIsEnabled(!user.getIsEnabled());
        User savedUser = userRepository.save(user);

        log.info("Статус пользователя {} изменен на: {}", userId, savedUser.getIsEnabled());
        return convertToDto(savedUser);
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Пользователь с ID " + userId + " не найден");
        }
        userRepository.deleteById(userId);
        log.info("Пользователь {} успешно удален", userId);
    }

    private UserDto convertToDto(User user) {
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