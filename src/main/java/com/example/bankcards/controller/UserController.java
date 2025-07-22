package com.example.bankcards.controller;

import com.example.bankcards.dto.CreateUserRequest;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "API для управления пользователями")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить список пользователей", description = "Получение всех пользователей с пагинацией")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа")
    })
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @Parameter(description = "Номер страницы (начиная с 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Поле для сортировки")
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Направление сортировки (asc, desc)")
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.debug("Запрос списка пользователей: page={}, size={}, sortBy={}, sortDir={}",
                page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @Operation(summary = "Получить пользователя по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа")
    })
    public ResponseEntity<UserDto> getUserById(
            @Parameter(description = "ID пользователя")
            @PathVariable Long id) {

        log.debug("Запрос пользователя по ID: {}", id);
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать нового пользователя", description = "Создание пользователя администратором")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации или пользователь уже существует"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа")
    })
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Создание нового пользователя: {}", request.getUsername());
        UserDto user = userService.createUser(request);
        return ResponseEntity.ok(user);
    }


    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Изменить статус пользователя", description = "Активация или деактивация пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус пользователя успешно изменен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа")
    })
    public ResponseEntity<UserDto> toggleUserStatus(
            @Parameter(description = "ID пользователя")
            @PathVariable Long id) {

        log.info("Изменение статуса пользователя ID: {}", id);
        UserDto user = userService.toggleUserStatus(id);
        return ResponseEntity.ok(user);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить пользователя", description = "Полное удаление пользователя из системы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно удален"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID пользователя")
            @PathVariable Long id) {

        log.info("Удаление пользователя ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}