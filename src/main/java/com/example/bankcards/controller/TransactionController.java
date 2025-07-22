package com.example.bankcards.controller;

import com.example.bankcards.dto.TransactionDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.TransactionService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transaction Management", description = "API для управления транзакциями")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;


    @PostMapping("/transfer")
    @Operation(summary = "Перевод между картами", description = "Перевод средств между своими картами")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Перевод успешно выполнен"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации или недостаточно средств"),
            @ApiResponse(responseCode = "403", description = "Карта не принадлежит пользователю")
    })
    public ResponseEntity<TransactionDto> transferBetweenCards(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        log.info("Пользователь {} инициировал перевод", currentUser.getUsername());

        TransactionDto transaction = transactionService.transferBetweenCards(request, currentUser.getId());
        return ResponseEntity.ok(transaction);
    }


    @GetMapping("/card/{cardId}")
    @Operation(summary = "История транзакций карты", description = "Получение истории транзакций для конкретной карты")
    public ResponseEntity<Page<TransactionDto>> getCardTransactions(
            @Parameter(description = "ID карты") @PathVariable Long cardId,
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Поле сортировки") @RequestParam(defaultValue = "transactionDate") String sortBy,
            @Parameter(description = "Направление сортировки") @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TransactionDto> transactions = transactionService.getCardTransactions(cardId, currentUser.getId(), pageable);
        return ResponseEntity.ok(transactions);
    }


    @GetMapping("/my")
    @Operation(summary = "Мои транзакции", description = "Получение всех транзакций пользователя")
    public ResponseEntity<Page<TransactionDto>> getMyTransactions(
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Поле сортировки") @RequestParam(defaultValue = "transactionDate") String sortBy,
            @Parameter(description = "Направление сортировки") @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TransactionDto> transactions = transactionService.getUserTransactions(currentUser.getId(), pageable);
        return ResponseEntity.ok(transactions);
    }
}