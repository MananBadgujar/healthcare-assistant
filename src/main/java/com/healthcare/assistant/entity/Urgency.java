package com.healthcare.assistant.entity;

import lombok.Getter;

/**
 * Urgency level for triage records.
 */
@Getter
public enum Urgency {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}