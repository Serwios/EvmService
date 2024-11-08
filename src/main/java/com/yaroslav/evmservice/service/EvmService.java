package com.yaroslav.evmservice.service;

import com.yaroslav.evmservice.entity.Transaction;
import com.yaroslav.evmservice.repository.TransactionRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.reactivex.schedulers.Schedulers;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EvmService {

    @Value("${web3j.client-address}")
    private String clientUrl;

    @Value("${transaction.batch-size:100}")
    private int batchSize;

    private final TransactionRepository transactionRepository;
    private final BlockCacheService blockCacheService;
    private final MeterRegistry meterRegistry;

    private Web3j web3j;

    @PostConstruct
    public void init() {
        web3j = Web3j.build(new HttpService(clientUrl));
        processBlocksFromLastCheckpoint();
    }

    @PreDestroy
    public void shutdown() {
        if (web3j != null) {
            web3j.shutdown();
        }
    }

    private void processBlocksFromLastCheckpoint() {
        BigInteger startBlock = Optional.ofNullable(blockCacheService.getLastProcessedBlock())
                .orElseGet(this::retrieveStartingBlockNumber)
                .add(BigInteger.ONE);

        subscribeToBlocks(startBlock);
    }

    private BigInteger retrieveStartingBlockNumber() {
        try {
            EthBlockNumber ethBlockNumber = web3j.ethBlockNumber().send();
            return ethBlockNumber.getBlockNumber().subtract(BigInteger.TEN);
        } catch (Exception e) {
            log.error("Failed to retrieve the starting block number, defaulting to 0", e);
            return BigInteger.ZERO;
        }
    }

    private void subscribeToBlocks(BigInteger startBlock) {
        web3j.replayPastAndFutureBlocksFlowable(DefaultBlockParameter.valueOf(startBlock), true)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(this::processBlockSafely, error -> {
                    log.error("Error in block subscription", error);
                    meterRegistry.counter("evm.block.subscription.errors").increment();
                });
    }

    private void processBlockSafely(EthBlock ethBlock) {
        try {
            processBlock(ethBlock.getBlock());
        } catch (Exception e) {
            log.error("Error processing block {}", ethBlock.getBlock().getNumber(), e);
            meterRegistry.counter("evm.block.processing.errors").increment();
        }
    }

    private void processBlock(EthBlock.Block block) {
        BigInteger blockNumber = block.getNumber();
        log.info("Processing block {}", blockNumber);

        List<EthBlock.TransactionResult> transactions = block.getTransactions();
        Timestamp blockTimestamp = new Timestamp(block.getTimestamp().longValueExact() * 1000);

        processTransactions(transactions, blockTimestamp);

        blockCacheService.saveLastProcessedBlock(blockNumber);
        meterRegistry.counter("evm.blocks.processed.count").increment();
    }

    private void processTransactions(List<EthBlock.TransactionResult> transactions, Timestamp blockTimestamp) {
        List<Transaction> transactionEntities = new ArrayList<>();

        for (EthBlock.TransactionResult txResult : transactions) {
            org.web3j.protocol.core.methods.response.Transaction tx = (org.web3j.protocol.core.methods.response.Transaction) txResult.get();
            transactionEntities.add(mapToEntity(tx, blockTimestamp));

            if (transactionEntities.size() >= batchSize) {
                saveTransactionBatch(transactionEntities);
                transactionEntities.clear();
            }
        }

        if (!transactionEntities.isEmpty()) {
            saveTransactionBatch(transactionEntities);
        }
    }

    public void saveTransactionBatch(List<Transaction> transactionEntities) {
        Timer.Sample batchSaveTimer = Timer.start(meterRegistry);
        try {
            transactionRepository.saveAll(transactionEntities);
            log.info("Saved batch of {} transactions", transactionEntities.size());
            meterRegistry.counter("evm.transactions.processed.count").increment(transactionEntities.size());
        } catch (Exception e) {
            log.error("Error saving transaction batch", e);
            meterRegistry.counter("evm.transaction.saving.errors").increment();
        } finally {
            batchSaveTimer.stop(meterRegistry.timer("evm.transaction.saving.time"));
        }
    }

    private Transaction mapToEntity(org.web3j.protocol.core.methods.response.Transaction tx, Timestamp blockTimestamp) {
        return Transaction.builder()
                .hash(tx.getHash())
                .fromAddress(tx.getFrom())
                .toAddress(tx.getTo())
                .value(tx.getValue())
                .gas(tx.getGas())
                .gasPrice(tx.getGasPrice())
                .blockNumber(tx.getBlockNumber())
                .timestamp(blockTimestamp)
                .inputData(tx.getInput())
                .build();
    }
}