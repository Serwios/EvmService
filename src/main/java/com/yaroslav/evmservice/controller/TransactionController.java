package com.yaroslav.evmservice.controller;

import com.yaroslav.evmservice.dto.ProcessedTransactionInfoDto;
import com.yaroslav.evmservice.entity.Transaction;
import com.yaroslav.evmservice.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @Operation(summary = "Search transactions by criteria",
            description = "Returns a paginated list of transactions filtered by optional criteria: sender address, receiver address, or block number.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions found and returned",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content)
    })
    @GetMapping("/search")
    public Page<Transaction> searchTransactions(
            @Parameter(description = "Sender address of the transaction", example = "0x1234...")
            @RequestParam(required = false) String fromAddress,

            @Parameter(description = "Receiver address of the transaction", example = "0x5678...")
            @RequestParam(required = false) String toAddress,

            @Parameter(description = "Block number in which the transaction was included", example = "12345678")
            @RequestParam(required = false) BigInteger blockNumber,

            Pageable pageable) {
        return transactionService.searchTransactions(fromAddress, toAddress, blockNumber, pageable);
    }

    @Operation(summary = "Full-text search in transaction input data",
            description = "Performs a full-text search in transaction input data and returns a paginated list of matching transactions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameter", content = @Content)
    })
    @GetMapping("/fulltext")
    public Page<Transaction> fullTextSearch(
            @Parameter(description = "Query string for full-text search", example = "example query")
            @RequestParam String query,

            Pageable pageable) {
        return transactionService.searchFullText(query, pageable);
    }

    @Operation(summary = "Get all information about transactions",
            description = "Retrieves a summary of all transaction information, including processed statistics.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction information retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProcessedTransactionInfoDto.class)))
    })
    @GetMapping("/info")
    public ProcessedTransactionInfoDto getTransactionInfo() {
        return transactionService.getTransactionInfo();
    }
}
