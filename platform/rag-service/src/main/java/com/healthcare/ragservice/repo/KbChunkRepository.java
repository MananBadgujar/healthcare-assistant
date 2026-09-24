package com.healthcare.ragservice.repo;

import com.healthcare.ragservice.entity.KbChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface KbChunkRepository extends JpaRepository<KbChunk, Long> {
    List<KbChunk> findByDocumentId(Long documentId);
}
