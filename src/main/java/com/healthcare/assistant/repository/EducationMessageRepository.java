package com.healthcare.assistant.repository;

import com.healthcare.assistant.entity.EducationMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EducationMessageRepository extends JpaRepository<EducationMessage, Long> {

    List<EducationMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
}
