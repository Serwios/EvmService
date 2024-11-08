package com.yaroslav.evmservice.repository;

import com.yaroslav.evmservice.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {
    Optional<Block> findByKey(String key);
}