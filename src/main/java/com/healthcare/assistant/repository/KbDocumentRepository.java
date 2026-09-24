package com.healthcare.assistant.repository;

import com.healthcare.assistant.entity.KbDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface KbDocumentRepository extends JpaRepository<KbDocument, Long> {

    Optional<KbDocument> findByExternalIdAndVersion(String externalId, String version);

    List<KbDocument> findByExternalIdOrderByVersionDesc(String externalId);

    Optional<KbDocument> findFirstByExternalIdAndActiveTrueOrderByIdDesc(String externalId);

    List<KbDocument> findByActiveTrue();

    @Query("select d from KbDocument d where d.active = true and d.active = true")
    List<KbDocument> findAllActive();
}
