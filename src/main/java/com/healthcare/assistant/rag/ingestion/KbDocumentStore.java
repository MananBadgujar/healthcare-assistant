package com.healthcare.assistant.rag.ingestion;

import com.healthcare.assistant.entity.KbDocument;
import com.healthcare.assistant.repository.KbDocumentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Thin repository wrapper that isolates document persistence and version
 * activation logic from the ingestion service. In particular it centralises
 * the rule that only one version of a given {@code externalId} should be
 * active at a time so retrieval never answers with stale content.
 */
@Component
public class KbDocumentStore {

    private final KbDocumentRepository repository;

    public KbDocumentStore(KbDocumentRepository repository) {
        this.repository = repository;
    }

    /** Create or update by mutating a fresh entity via the supplied consumer. */
    public KbDocument save(Consumer<KbDocument> mutator) {
        KbDocument document = new KbDocument();
        mutator.accept(document);
        return repository.save(document);
    }

    /** Save an already-detached entity (used by update-in-place flows). */
    public KbDocument save(KbDocument document) {
        return repository.save(document);
    }

    public Optional<KbDocument> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<KbDocument> findByExternalIdAndVersion(String externalId, String version) {
        if (externalId == null || version == null) {
            return Optional.empty();
        }
        return repository.findByExternalIdAndVersion(externalId, version);
    }

    public List<KbDocument> findAll() {
        return repository.findAll();
    }

    public List<KbDocument> findAllActive() {
        return repository.findAllActive();
    }

    public Optional<KbDocument> findActiveByExternalId(String externalId) {
        return repository.findFirstByExternalIdAndActiveTrueOrderByIdDesc(externalId);
    }

    public List<KbDocument> findVersions(String externalId) {
        return repository.findByExternalIdOrderByVersionDesc(externalId);
    }

    /**
     * Activate a version and deactivate every other version of the same
     * externalId. The supplied callbacks allow the caller to refresh chunk
     * active flags consistently with the new active document.
     */
    public KbDocument activateVersion(String externalId, String version,
                                      Consumer<KbDocument> onActivate,
                                      Consumer<KbDocument> onDeactivate) {
        KbDocument target = repository.findByExternalIdAndVersion(externalId, version)
                .orElseThrow(() -> new ExtractionException(
                        "Document version not found: " + externalId + "/" + version));
        List<KbDocument> versions = repository.findByExternalIdOrderByVersionDesc(externalId);
        for (KbDocument doc : versions) {
            if (doc.getId().equals(target.getId())) {
                doc.setActive(true);
                repository.save(doc);
                onActivate.accept(doc);
            } else if (doc.isActive()) {
                doc.setActive(false);
                repository.save(doc);
                onDeactivate.accept(doc);
            }
        }
        return target;
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
