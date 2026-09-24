package com.healthcare.assistant.service;

import com.healthcare.assistant.dto.PatientContext;

/**
 * Service that provides access to the currently authenticated patient context.
 * The JWT authentication store contains a claim "patient_id" that maps to the patient's
 * internal ID. This service extracts that ID and builds a sanitized context DTO.
 */
public interface PatientContextService {

    /**
     * Returns the current patient context built from the authenticated user.
     * If no patient is authenticated, an exception may be thrown depending on implementation.
     *
     * @return PatientContext containing patient information
     */
    PatientContext getCurrentPatientContext();
}