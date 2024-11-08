package com.yaroslav.evmservice.repository;


import com.yaroslav.evmservice.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    @Query("SELECT t FROM Transaction t WHERE " +
            "(:fromAddress IS NULL OR t.fromAddress = :fromAddress) AND " +
            "(:toAddress IS NULL OR t.toAddress = :toAddress) AND " +
            "(:blockNumber IS NULL OR t.blockNumber = :blockNumber)")
    Page<Transaction> searchTransactions(@Param("fromAddress") String fromAddress,
                                         @Param("toAddress") String toAddress,
                                         @Param("blockNumber") BigInteger blockNumber,
                                         Pageable pageable);

    @Query("SELECT MAX(e.blockNumber) FROM Transaction e")
    Optional<BigInteger> findMaxBlockNumber();

    @Query(value = "SELECT * FROM transactions t WHERE t.search_vector @@ plainto_tsquery(:query)",
            countQuery = "SELECT count(*) FROM transactions t WHERE t.search_vector @@ plainto_tsquery(:query)",
            nativeQuery = true)
    Page<Transaction> searchFullText(@Param("query") String query, Pageable pageable);

    long count();

    long countByTimestampAfter(Timestamp timestamp);

    @Query("SELECT COUNT(DISTINCT t.blockNumber) FROM Transaction t")
    long countDistinctByBlockNumber();

    @Query("SELECT COALESCE(SUM(t.value), 0) FROM Transaction t")
    BigInteger sumTransactionValue();

    @Query("SELECT COALESCE(SUM(t.gas), 0) FROM Transaction t")
    BigInteger sumGas();

    @Query("SELECT COALESCE(AVG(t.gasPrice), 0) FROM Transaction t")
    BigInteger calculateAverageGasPrice();
}
