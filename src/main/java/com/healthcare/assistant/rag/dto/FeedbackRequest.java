package com.healthcare.assistant.rag.dto;

import com.healthcare.assistant.rag.feedback.FeedbackCategory;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for {@code POST /api/v1/kb/feedback/{messageId}}.
 */
public class FeedbackRequest {

    @NotNull
    private FeedbackCategory category;
    private String comment;

    public FeedbackCategory getCategory() { return category; }
    public void setCategory(FeedbackCategory category) { this.category = category; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
