package com.healthcare.assistant.repository;

import com.healthcare.assistant.entity.KbChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KbChunkRepository extends JpaRepository<KbChunk, Long> {

    Optional<KbChunk> findByChunkId(String chunkId);

    List<KbChunk> findByDocumentId(Long documentId);

    List<KbChunk> findByDocumentIdAndActiveTrue(Long documentId);

    List<KbChunk> findByActiveTrue();

    void deleteByDocumentId(Long documentId);
}
