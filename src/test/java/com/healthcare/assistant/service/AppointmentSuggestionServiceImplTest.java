package com.healthcare.assistant.service;

import com.healthcare.assistant.dto.PatientContext;
import com.healthcare.assistant.entity.Appointment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentSuggestionServiceImplTest {

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private PatientContextService patientContextService;

    @InjectMocks
    private com.healthcare.assistant.service.impl.AppointmentSuggestionServiceImpl appointmentSuggestionService;

    @BeforeEach
    void setUp() {
        // Ensure a clean security context before each test
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        // Ensure a clean security context after each test
        SecurityContextHolder.clearContext();
    }

    @Test
    void generateSuggestion_returnsFirstAppointment_whenSuggestionsExist() {
        // given
        PatientContext patientContext = new PatientContext();
        patientContext.setId(123L);
        when(patientContextService.getCurrentPatientContext()).thenReturn(patientContext);
        Long patientId = patientContext.getId();

        Appointment suggested = new Appointment();
        suggested.setId(1L);
        when(appointmentService.suggestAppointments(eq(patientId), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(suggested));

        // when
        Appointment result = appointmentSuggestionService.generateSuggestion();

        // then
        assertEquals(suggested, result);
        verify(appointmentService).suggestAppointments(eq(patientId), any(LocalDate.class));
    }

    @Test
    void generateSuggestion_returnsNonNullAppointment_whenNoSuggestions() {
        // given
        PatientContext patientContext = new PatientContext();
        patientContext.setId(123L);
        when(patientContextService.getCurrentPatientContext()).thenReturn(patientContext);
        when(appointmentService.suggestAppointments(anyLong(), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // when
        Appointment result = appointmentSuggestionService.generateSuggestion();

        // then
        assertNotNull(result);
        verify(appointmentService).suggestAppointments(anyLong(), any(LocalDate.class));
    }
}