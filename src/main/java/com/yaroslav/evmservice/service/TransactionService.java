package com.yaroslav.evmservice.service;


import com.yaroslav.evmservice.dto.ProcessedTransactionInfoDto;
import com.yaroslav.evmservice.entity.Transaction;
import com.yaroslav.evmservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.sql.Timestamp;


@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;

    public ProcessedTransactionInfoDto getTransactionInfo() {
        return new ProcessedTransactionInfoDto(
                getTotalTransactionCount(),
                getTransactionsCountLast5Minutes(),
                getUniqueBlocksProcessed(),
                getTotalValueTransferred(),
                getTotalGasUsed(),
                getAverageGasPrice()
        );
    }

    public Page<Transaction> searchTransactions(String fromAddress, String toAddress, BigInteger blockNumber, Pageable pageable) {
           return transactionRepository.searchTransactions(fromAddress, toAddress, blockNumber, pageable);
    }

    public Page<Transaction> searchFullText(String query, Pageable pageable) {
        return transactionRepository.searchFullText(query, pageable);
    }

    public long getTotalTransactionCount() {
        return transactionRepository.count();
    }

    private long getTransactionsCountLast5Minutes() {
        Timestamp fiveMinutesAgo = new Timestamp(System.currentTimeMillis() - 5 * 60 * 1000);
        return transactionRepository.countByTimestampAfter(fiveMinutesAgo);
    }

    private long getUniqueBlocksProcessed() {
        return transactionRepository.countDistinctByBlockNumber();
    }

    private BigInteger getTotalValueTransferred() {
        return transactionRepository.sumTransactionValue();
    }

    private BigInteger getTotalGasUsed() {
        return transactionRepository.sumGas();
    }

    private BigInteger getAverageGasPrice() {
        return transactionRepository.calculateAverageGasPrice();
    }
}
