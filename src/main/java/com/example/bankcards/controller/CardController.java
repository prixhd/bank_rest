package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Card Management", description = "API для управления банковскими картами")
@SecurityRequirement(name = "bearerAuth")
public class CardController {

    private final CardService cardService;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать новую карту", description = "Создание банковской карты для пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно создана"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации данных"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа")
    })
    public ResponseEntity<CardDto> createCard(@Valid @RequestBody CreateCardRequest request) {
        log.info("Запрос на создание карты для пользователя ID: {}", request.getUserId());
        CardDto card = cardService.createCard(request);
        return ResponseEntity.ok(card);
    }


    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить все карты", description = "Получение всех карт в системе")
    public ResponseEntity<Page<CardDto>> getAllCards(
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Поле сортировки") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Направление сортировки") @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CardDto> cards = cardService.getAllCards(pageable);
        return ResponseEntity.ok(cards);
    }


    @GetMapping("/my")
    @Operation(summary = "Получить мои карты", description = "Получение карт текущего пользователя")
    public ResponseEntity<Page<CardDto>> getMyCards(
            @Parameter(description = "Фильтр по статусу карты") @RequestParam(required = false) CardStatus status,
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Поле сортировки") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Направление сортировки") @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CardDto> cards = cardService.getUserCards(currentUser.getId(), status, pageable);
        return ResponseEntity.ok(cards);
    }


    @GetMapping("/my/{cardId}")
    @Operation(summary = "Получить мою карту по ID")
    public ResponseEntity<CardDto> getMyCard(
            @Parameter(description = "ID карты") @PathVariable Long cardId,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        CardDto card = cardService.getUserCardById(currentUser.getId(), cardId);
        return ResponseEntity.ok(card);
    }


    @PutMapping("/my/{cardId}/block")
    @Operation(summary = "Заблокировать мою карту", description = "Пользователь может заблокировать свою карту")
    public ResponseEntity<CardDto> blockMyCard(
            @Parameter(description = "ID карты") @PathVariable Long cardId,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();

        cardService.getUserCardById(currentUser.getId(), cardId);

        CardDto card = cardService.blockCard(cardId);
        return ResponseEntity.ok(card);
    }


    @PutMapping("/{cardId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Заблокировать карту", description = "Администратор может заблокировать любую карту")
    public ResponseEntity<CardDto> blockCard(
            @Parameter(description = "ID карты") @PathVariable Long cardId) {

        CardDto card = cardService.blockCard(cardId);
        return ResponseEntity.ok(card);
    }


    @PutMapping("/{cardId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Активировать карту", description = "Активация заблокированной карты")
    public ResponseEntity<CardDto> activateCard(
            @Parameter(description = "ID карты") @PathVariable Long cardId) {

        CardDto card = cardService.activateCard(cardId);
        return ResponseEntity.ok(card);
    }


    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить карту", description = "Полное удаление карты из системы")
    public ResponseEntity<Void> deleteCard(
            @Parameter(description = "ID карты") @PathVariable Long cardId) {

        cardService.deleteCard(cardId);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить карту по ID")
    public ResponseEntity<CardDto> getCardById(
            @Parameter(description = "ID карты") @PathVariable Long cardId) {

        CardDto card = cardService.getCardById(cardId);
        return ResponseEntity.ok(card);
    }
}