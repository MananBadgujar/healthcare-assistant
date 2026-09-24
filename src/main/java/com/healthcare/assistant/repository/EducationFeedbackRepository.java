package com.healthcare.assistant.repository;

import com.healthcare.assistant.entity.EducationFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EducationFeedbackRepository extends JpaRepository<EducationFeedback, Long> {

    List<EducationFeedback> findByMessageId(Long messageId);

    List<EducationFeedback> findByPrincipalName(String principalName);
}
