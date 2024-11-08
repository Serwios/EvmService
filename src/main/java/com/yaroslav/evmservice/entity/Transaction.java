package com.yaroslav.evmservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.sql.Timestamp;

@Entity
@Table(
        name = "transactions"
)
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class Transaction {
    @Id
    private String hash;

    @Column(name = "from_address", nullable = false)
    private String fromAddress;

    @Column(name = "to_address")
    private String toAddress;

    @Column(precision = 38, scale = 0)
    private BigInteger value;

    @Column(precision = 38, scale = 0)
    private BigInteger gas;

    @Column(name = "gas_price", precision = 38, scale = 0)
    private BigInteger gasPrice;

    @Column(name = "block_number", nullable = false)
    private BigInteger blockNumber;

    @Column(nullable = false)
    private Timestamp timestamp;

    @Column(name = "input_data", columnDefinition = "TEXT")
    private String inputData;
}