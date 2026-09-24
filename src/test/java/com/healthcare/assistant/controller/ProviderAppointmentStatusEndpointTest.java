package com.healthcare.assistant.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.healthcare.assistant.entity.Provider;
import com.healthcare.assistant.service.AppointmentService;
import com.healthcare.assistant.service.ProviderService;

/**
 * Phase 2 endpoint coverage: ProviderController (all 6) + AppointmentStatus.
 */
@WebMvcTest({ProviderController.class, AppointmentStatusController.class})
@WithMockUser(roles = "USER")
class ProviderAppointmentStatusEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProviderService providerService;

    @MockBean
    private AppointmentService appointmentService;

    private Provider provider() {
        Provider provider = new Provider();
        provider.setId(1L);
        provider.setLicenseNumber("LIC-1");
        provider.setSpecialty("Cardiology");
        return provider;
    }

    @Test
    void createProvider_returnsOk() throws Exception {
        when(providerService.saveProvider(any(Provider.class))).thenReturn(provider());
        mockMvc.perform(post("/api/v1/providers").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"licenseNumber\":\"LIC-1\",\"specialty\":\"Cardiology\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialty").value("Cardiology"));
    }

    @Test
    void getProviderById_returnsOk() throws Exception {
        when(providerService.getProviderById(1L)).thenReturn(Optional.of(provider()));
        mockMvc.perform(get("/api/v1/providers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licenseNumber").value("LIC-1"));
    }

    @Test
    void getProviderById_missing_returnsNotFound() throws Exception {
        when(providerService.getProviderById(999L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/providers/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllProviders_returnsOk() throws Exception {
        when(providerService.getAllProviders()).thenReturn(Collections.singletonList(provider()));
        mockMvc.perform(get("/api/v1/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].specialty").value("Cardiology"));
    }

    @Test
    void updateProvider_returnsOk() throws Exception {
        when(providerService.saveProvider(any(Provider.class))).thenReturn(provider());
        mockMvc.perform(put("/api/v1/providers/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"licenseNumber\":\"LIC-1\",\"specialty\":\"Cardiology\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteProvider_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/providers/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void searchProviders_returnsOk() throws Exception {
        when(providerService.findBySpecialty("Cardiology"))
                .thenReturn(Collections.singletonList(provider()));
        mockMvc.perform(get("/api/v1/providers/search").param("specialty", "Cardiology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].licenseNumber").value("LIC-1"));
    }

    @Test
    void searchProviders_empty_returnsOk() throws Exception {
        when(providerService.findBySpecialty("Unknown")).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/providers/search").param("specialty", "Unknown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void updateAppointmentStatus_returnsNoContent() throws Exception {
        mockMvc.perform(patch("/api/v1/appointments/1/status").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isNoContent());
    }
}
