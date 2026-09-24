package com.healthcare.assistant.dto;

import lombok.Data;
import java.util.List;

@Data
public class AiResponse {
    private String intent;
    private List<String> symptoms;
    private String severity;
    private List<String> redFlags;
    private boolean emergency;
    private String triageLevel;
    private String clinicalGuidance;
    private String medicationSafety;
    private String followUp;
    private String disclaimer;
}