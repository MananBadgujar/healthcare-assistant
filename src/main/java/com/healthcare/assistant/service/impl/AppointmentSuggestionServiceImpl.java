package com.healthcare.assistant.service.impl;

import com.healthcare.assistant.dto.PatientContext;
import com.healthcare.assistant.entity.Appointment;
import com.healthcare.assistant.service.AppointmentService;
import com.healthcare.assistant.service.PatientContextService;
import com.healthcare.assistant.service.AppointmentSuggestionService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementation of {@link AppointmentSuggestionService} that generates
 * AI‑enhanced appointment suggestions using existing {@link AppointmentService}
 * business rules.
 */
@Service
public class AppointmentSuggestionServiceImpl implements AppointmentSuggestionService {

    private final AppointmentService appointmentService;
    private final PatientContextService patientContextService;

    public AppointmentSuggestionServiceImpl(AppointmentService appointmentService,
                                            PatientContextService patientContextService) {
        this.appointmentService = appointmentService;
        this.patientContextService = patientContextService;
    }

    @Override
    public Appointment generateSuggestion() {
        // Obtain current patient context; may throw if unauthenticated
        PatientContext patientContext = patientContextService.getCurrentPatientContext();
        Long patientId = patientContext.getId();

        // Generate appointment suggestions for the current patient using existing logic
        List<Appointment> suggestions = appointmentService.suggestAppointments(patientId, LocalDate.now());

        // Return the first suggestion if any are available; otherwise return a placeholder
        return suggestions.isEmpty() ? new Appointment() : suggestions.get(0);
    }
}