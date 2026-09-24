package com.healthcare.assistant.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.healthcare.assistant.dto.PatientContext;
import com.healthcare.assistant.entity.Appointment;
import com.healthcare.assistant.service.AppointmentService;
import com.healthcare.assistant.service.PatientContextService;
import java.util.Collections;
import java.util.Optional;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AppointmentController.class)
@WithMockUser(roles = "USER")
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private PatientContextService patientContextService;

    @Test
    public void createAppointment_returnsSavedAppointment() throws Exception {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        when(appointmentService.saveAppointment(any(Appointment.class))).thenReturn(appointment);

        mockMvc.perform(post("/api/v1/appointments")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"patientId\":1,\"providerId\":1,\"appointmentDateTime\":\"2025-01-01T10:00:00\",\"reason\":\"Check-up\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void listAllAppointments_returnsEmptyList() throws Exception {
        when(appointmentService.getAllAppointments()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/appointments").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("[]"));
    }

    @Test
    public void getAppointmentById_returnsAppointment() throws Exception {
        PatientContext pc = new PatientContext(1L, "John", "Doe", "Male", "1980-01-01");
        when(patientContextService.getCurrentPatientContext()).thenReturn(pc);

        Appointment appt = new Appointment();
        appt.setId(1L);
        appt.setPatient(new com.healthcare.assistant.entity.Patient());
        appt.getPatient().setId(1L);
        when(appointmentService.getAppointmentById(1L)).thenReturn(Optional.of(appt));
        mockMvc.perform(get("/api/v1/appointments/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void updateAppointment_returnsUpdatedAppointment() throws Exception {
        PatientContext pc = new PatientContext(1L, "John", "Doe", "Male", "1980-01-01");
        when(patientContextService.getCurrentPatientContext()).thenReturn(pc);

        Appointment existing = new Appointment();
        existing.setId(1L);
        existing.setPatient(new com.healthcare.assistant.entity.Patient());
        existing.getPatient().setId(1L);

        Appointment updated = new Appointment();
        updated.setId(1L);
        updated.setPatient(new com.healthcare.assistant.entity.Patient());
        updated.getPatient().setId(1L);

        when(patientContextService.getCurrentPatientContext()).thenReturn(pc);
        when(appointmentService.getAppointmentById(1L)).thenReturn(Optional.of(existing));
        when(appointmentService.saveAppointment(any(Appointment.class))).thenReturn(updated);
        String json = "{\"id\":1,\"patientId\":2,\"providerId\":2,\"appointmentDateTime\":\"2025-02-01T10:00:00\",\"reason\":\"Follow-up\"}";
        mockMvc.perform(put("/api/v1/appointments/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void deleteAppointment_returnsNoContent() throws Exception {
        PatientContext pc = new PatientContext(1L, "John", "Doe", "Male", "1980-01-01");
        when(patientContextService.getCurrentPatientContext()).thenReturn(pc);

        Appointment appt = new Appointment();
        appt.setId(1L);
        appt.setPatient(new com.healthcare.assistant.entity.Patient());
        appt.getPatient().setId(1L);
        when(appointmentService.getAppointmentById(1L)).thenReturn(Optional.of(appt));
        mockMvc.perform(delete("/api/v1/appointments/1").with(csrf()))
                .andExpect(status().isNoContent());
    }
}