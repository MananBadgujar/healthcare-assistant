package com.healthcare.assistant.repository;

import com.healthcare.assistant.entity.KbEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KbEmbeddingRepository extends JpaRepository<KbEmbedding, Long> {

    Optional<KbEmbedding> findByChunkId(Long chunkId);

    List<KbEmbedding> findByChunk_DocumentId(Long documentId);

    List<KbEmbedding> findAll();
}
