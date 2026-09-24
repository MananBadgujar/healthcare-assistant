package com.healthcare.assistant.kafka.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class TopicConfig {

    public interface Patient {
        String CREATED = "healthcare.patient.events.created";
        String UPDATED = "healthcare.patient.events.updated";
    }

    public interface Appointment {
        String CREATED = "healthcare.appointment.events.created";
        String UPDATED = "healthcare.appointment.events.updated";
        String CANCELLED = "healthcare.appointment.events.cancelled";
        String CONFIRMED = "healthcare.appointment.events.confirmed";
        String COMPLETED = "healthcare.appointment.events.completed";
        String NO_SHOW = "healthcare.appointment.events.no-show";
        String STATUS_CHANGED = "healthcare.appointment.events.status-changed";
    }

    public interface Encounter {
        String COMPLETED = "healthcare.encounter.events.completed";
        String LAB_RESULT = "healthcare.encounter.events.lab-result";
        String CLINICAL_ALERT = "healthcare.encounter.events.clinical-alert";
        String CARE_PLAN_APPROVED = "healthcare.encounter.events.care-plan-approved";
        String MEDICATION_CHANGED = "healthcare.encounter.events.medication-changed";
        String TRIAGE_COMPLETED = "healthcare.encounter.events.triage-completed";
    }

    public interface Medication {
        String CREATED = "healthcare.medication.events.created";
        String REFILL_REQUESTED = "healthcare.medication.events.refill-requested";
    }

    public interface Billing {
        String PAYMENT_COMPLETED = "healthcare.billing.events.payment-completed";
        String CLAIM_SUBMITTED = "healthcare.billing.events.claim-submitted";
        String CLAIM_APPROVED = "healthcare.billing.events.claim-approved";
        String CLAIM_REJECTED = "healthcare.billing.events.claim-rejected";
        String CLAIM_STATUS_CHANGED = "healthcare.billing.events.claim-status-changed";
    }

    public interface Claim {
        String SUBMITTED = "healthcare.claim.events.submitted";
        String APPROVED = "healthcare.claim.events.approved";
        String REJECTED = "healthcare.claim.events.rejected";
        String STATUS_CHANGED = "healthcare.claim.events.status-changed";
    }

    public interface AI {
        String LAB_INTERPRETATION = "healthcare.ai.events.lab-interpretation";
        String CLINICAL_ALERT = "healthcare.ai.events.clinical-alert";
        String TRIAGE_COMPLETED = "healthcare.ai.events.triage-completed";
    }

    public interface Notification {
        String APPOINTMENT_REMININDER = "healthcare.notification.events.appointment-reminder";
        String APPOINTMENT_CANCELLATION = "healthcare.notification.events.appointment-cancellation";
        String PAYMENT_CONFIRMATION = "healthcare.notification.events.payment-confirmation";
        String LAB_RESULT_AVAILABLE = "healthcare.notification.events.lab-result-available";
    }

    public interface Inventory {
        String LOW_STOCK = "healthcare.inventory.events.low-stock";
        String NEAR_EXPIRY = "healthcare.inventory.events.near-expiry";
        String STOCK_RECEIVED = "healthcare.inventory.events.stock-received";
        String STOCK_CONSUMED = "healthcare.inventory.events.stock-consumed";
        String STOCK_TRASNFERRED = "healthcare.inventory.events.stock-transferred";
        String BATCH_NEAR_EXPIRY = "healthcare.inventory.events.batch-near-expiry";
        String ITEM_EXPIRED = "healthcare.inventory.events.item-expired";
        String REORDER_RECOMMENDED = "healthcare.inventory.events.reorder-recommended";
    }

    public interface Telehealth {
        String SESSION_COMPLETED = "healthcare.telehealth.events.session-completed";
        String SESSION_REMINDER = "healthcare.telehealth.events.session-reminder";
    }

    public interface Audit {
        String EVENT = "healthcare.audit.events";
    }
}