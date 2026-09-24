package com.healthcare.assistant.rag.feedback;

import com.healthcare.assistant.entity.EducationFeedback;
import com.healthcare.assistant.entity.EducationMessage;
import com.healthcare.assistant.repository.EducationFeedbackRepository;
import com.healthcare.assistant.repository.EducationMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Submits and retrieves feedback against education conversation messages.
 * Feedback is associated with the authenticated principal submitting it so the
 * admin/provider workflow can later see who flagged a given answer.
 */
@Service
public class FeedbackService {

    private final EducationFeedbackRepository feedbackRepository;
    private final EducationMessageRepository messageRepository;

    public FeedbackService(EducationFeedbackRepository feedbackRepository,
                           EducationMessageRepository messageRepository) {
        this.feedbackRepository = feedbackRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public EducationFeedback submit(Long messageId, FeedbackCategory category,
                                    String comment, String principalName) {
        if (category == null) {
            throw new IllegalArgumentException("feedback category is required");
        }
        EducationMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("Message not found: " + messageId));
        EducationFeedback feedback = new EducationFeedback();
        feedback.setMessage(message);
        feedback.setCategory(category.name());
        feedback.setComment(comment);
        feedback.setPrincipalName(principalName);
        return feedbackRepository.save(feedback);
    }

    @Transactional(readOnly = true)
    public List<EducationFeedback> feedbackForMessage(Long messageId) {
        return feedbackRepository.findByMessageId(messageId);
    }

    @Transactional(readOnly = true)
    public List<EducationFeedback> feedbackByPrincipal(String principalName) {
        return feedbackRepository.findByPrincipalName(principalName);
    }
}
