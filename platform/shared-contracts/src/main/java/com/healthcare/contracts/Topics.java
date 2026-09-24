package com.healthcare.contracts;

/** Canonical Kafka topic names for the platform. */
public final class Topics {
    private Topics() {}
    public static final String PATIENT_CREATED = "healthcare.patient.events.created";
    public static final String PATIENT_UPDATED = "healthcare.patient.events.updated";
    public static final String APPOINTMENT_CREATED = "healthcare.appointment.events.created";
    public static final String APPOINTMENT_CANCELLED = "healthcare.appointment.events.cancelled";
    public static final String APPOINTMENT_STATUS = "healthcare.appointment.events.status-changed";
    public static final String MEDICATION_CREATED = "healthcare.medication.events.created";
    public static final String MEDICATION_REFILL = "healthcare.medication.events.refill-requested";
    public static final String PAYMENT_COMPLETED = "healthcare.billing.events.payment-completed";
    public static final String CLAIM_SUBMITTED = "healthcare.claim.events.submitted";
    public static final String AI_TRIAGE_COMPLETED = "healthcare.ai.events.triage-completed";
    public static final String CLINICAL_ALERT = "healthcare.encounter.events.clinical-alert";
    public static final String LOW_STOCK = "healthcare.inventory.events.low-stock";
    public static final String NEAR_EXPIRY = "healthcare.inventory.events.near-expiry";
    public static final String AUDIT_EVENTS = "healthcare.audit.events";
}
