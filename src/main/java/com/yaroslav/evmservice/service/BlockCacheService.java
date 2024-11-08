package com.yaroslav.evmservice.service;

import java.math.BigInteger;

public interface BlockCacheService {
    void saveLastProcessedBlock(BigInteger blockNumber);
    BigInteger getLastProcessedBlock();
}
