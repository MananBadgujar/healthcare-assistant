package com.healthcare.assistant.repository;

import com.healthcare.assistant.entity.EducationConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EducationConversationRepository extends JpaRepository<EducationConversation, Long> {

    Optional<EducationConversation> findByExternalConversationId(String externalConversationId);

    List<EducationConversation> findByPrincipalNameOrderByUpdatedAtDesc(String principalName);
}
