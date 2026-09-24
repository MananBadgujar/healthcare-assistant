package com.healthcare.assistant.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.assistant.entity.Clinic;
import com.healthcare.assistant.service.ClinicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;
import java.util.List;

@WebMvcTest(ClinicController.class)
@WithMockUser
class ClinicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClinicService clinicService;

    @Test
    public void createClinic_validRequest_returnsCreated() throws Exception {
        Clinic clinic = new Clinic("City Hospital", "123 Main St", "555-1234", "info@city.com", "A teaching hospital");
        when(clinicService.createClinic(any(Clinic.class))).thenReturn(clinic);

        mockMvc.perform(post("/api/v1/clinics")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clinic)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(clinic.getName()));

        verify(clinicService).createClinic(any(Clinic.class));
    }

    @Test
    public void createClinic_missingName_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/clinics")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getClinic_existing_returnsOk() throws Exception {
        Clinic clinic = new Clinic("General Hospital", "200 Park Ave", "555-5678", "contact@genhosp.com", "General care");
        when(clinicService.getClinicById(1L)).thenReturn(Optional.of(clinic));

        mockMvc.perform(get("/api/v1/clinics/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(clinic.getName()));
    }

    @Test
    public void getClinic_notFound_returnsNotFound() throws Exception {
        when(clinicService.getClinicById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/clinics/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getAllClinics_returnsOk() throws Exception {
        when(clinicService.getAllClinics()).thenReturn(List.of(new Clinic("Hospital A", "1", "111", "a@a.com", ""),
                new Clinic("Hospital B", "2", "222", "b@b.com", "")));

        mockMvc.perform(get("/api/v1/clinics"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void updateClinic_existing_returnsOk() throws Exception {
        Clinic existing = new Clinic("Old Name", "100 Old Rd", "111-1111", "old@old.com", "Old desc");
        existing.setId(1L);
        Clinic updated = new Clinic("New Name", "200 New Rd", "222-2222", "new@new.com", "New desc");
        updated.setId(1L);
        when(clinicService.updateClinic(eq(1L), any(Clinic.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/clinics/1")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updated.getName()));
    }

    @Test
    public void deleteClinic_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/clinics/1")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

        verify(clinicService).deleteClinic(1L);
    }
}