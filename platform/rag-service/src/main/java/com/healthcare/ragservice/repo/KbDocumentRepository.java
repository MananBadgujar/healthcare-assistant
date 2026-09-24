package com.healthcare.ragservice.repo;

import com.healthcare.ragservice.entity.KbDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KbDocumentRepository extends JpaRepository<KbDocument, Long> {
}
