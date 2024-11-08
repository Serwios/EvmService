package com.yaroslav.evmservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedTransactionInfoDto {
    private long totalTransactionCount;
    private long transactionsLast5Minutes;
    private long uniqueBlocksProcessed;
    private BigInteger totalValueTransferred;
    private BigInteger totalGasUsed;
    private BigInteger averageGasPrice;
}