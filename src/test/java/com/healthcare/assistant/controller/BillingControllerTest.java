package com.healthcare.assistant.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.healthcare.assistant.dto.BillingRequest;
import com.healthcare.assistant.dto.BillingResponse;
import com.healthcare.assistant.entity.Billing;
import com.healthcare.assistant.service.BillingService;
import com.healthcare.assistant.service.PatientContextService;
import com.healthcare.assistant.dto.PatientContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

@WebMvcTest(BillingController.class)
@WithMockUser(roles = "USER")
class BillingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillingService billingService;

    @MockBean
    private PatientContextService patientContextService;

    @BeforeEach
    void setUp() {
        when(patientContextService.getCurrentPatientContext())
                .thenReturn(new PatientContext(1L, "Test", "Patient", "M", "1990-01-01"));
    }

    @Test
    public void createBillingInvoice_returnsInvoiceId() throws Exception {
        when(billingService.createInvoice(any())).thenReturn("invoice-123");
        mockMvc.perform(post("/api/v1/billing")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"patientId\":1,\"amount\":100.0,\"description\":\"Test service\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("invoice-123"));
    }

    @Test
    public void getBillingInvoice_byId_returnsOk() throws Exception {
        when(billingService.getInvoice("invoice-123")).thenReturn(new BillingResponse());
        mockMvc.perform(get("/api/v1/billing/invoice-123"))
                .andExpect(status().isOk());
    }

    @Test
    public void getInvoicesByPatientId_returnsOk() throws Exception {
        when(billingService.getInvoicesByPatientId(1L)).thenReturn(new ArrayList<>());
        mockMvc.perform(get("/api/v1/billing/patient/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void markInvoiceAsPaid_returnsOk() throws Exception {
        when(billingService.getInvoice("invoice-123")).thenReturn(new BillingResponse());
        mockMvc.perform(post("/api/v1/billing/invoice-123/pay")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Paid"));
    }

    @Test
    public void getInvoicesByProviderId_returnsOk() throws Exception {
        when(billingService.getInvoicesByProviderId(1L)).thenReturn(new ArrayList<>());
        mockMvc.perform(get("/api/v1/billing/provider/1"))
                .andExpect(status().isOk());
    }
}