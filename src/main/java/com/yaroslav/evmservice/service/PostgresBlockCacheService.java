package com.yaroslav.evmservice.service;

import com.yaroslav.evmservice.entity.Block;
import com.yaroslav.evmservice.repository.BlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostgresBlockCacheService implements BlockCacheService {
    private final BlockRepository blockRepository;
    private static final String LAST_PROCESSED_BLOCK_KEY = "last-processed-block";

    @Override
    public void saveLastProcessedBlock(BigInteger blockNumber) {
        Optional<Block> blockOpt = blockRepository.findByKey(LAST_PROCESSED_BLOCK_KEY);
        Block block = blockOpt.orElseGet(() -> {
            Block newBlock = new Block();
            newBlock.setKey(LAST_PROCESSED_BLOCK_KEY);
            return newBlock;
        });
        block.setBlockNumber(blockNumber);
        blockRepository.save(block);
    }

    @Override
    public BigInteger getLastProcessedBlock() {
        return blockRepository.findByKey(LAST_PROCESSED_BLOCK_KEY)
                .map(Block::getBlockNumber)
                .orElse(null);
    }
}
