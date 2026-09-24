package com.healthcare.assistant.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.healthcare.assistant.entity.Billing;
import com.healthcare.assistant.entity.Claim;
import com.healthcare.assistant.entity.Insurance;
import com.healthcare.assistant.entity.Patient;
import com.healthcare.assistant.entity.Payment;
import com.healthcare.assistant.entity.PreAuthorization;
import com.healthcare.assistant.repository.BillingRepository;
import com.healthcare.assistant.repository.InsuranceRepository;
import com.healthcare.assistant.repository.PatientRepository;
import com.healthcare.assistant.service.ClaimService;
import com.healthcare.assistant.service.InsuranceService;
import com.healthcare.assistant.service.PaymentService;
import com.healthcare.assistant.service.PreAuthorizationService;

/**
 * Phase 3b endpoint coverage: Claim, Insurance, Payment, PreAuthorization.
 */
@WebMvcTest({ClaimController.class, InsuranceController.class, PaymentController.class,
        PreAuthorizationController.class})
@WithMockUser(roles = "USER")
class ClaimBillingEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClaimService claimService;

    @MockBean
    private InsuranceService insuranceService;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private PreAuthorizationService preAuthorizationService;

    @MockBean
    private PatientRepository patientRepository;

    @MockBean
    private InsuranceRepository insuranceRepository;

    @MockBean
    private BillingRepository billingRepository;

    // ---------- Claim (6) ----------

    @Test
    void createClaim_returnsOk() throws Exception {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(new Patient()));
        when(insuranceRepository.findById(1L)).thenReturn(Optional.of(new Insurance()));
        when(billingRepository.findById(1L)).thenReturn(Optional.of(new Billing()));
        when(claimService.createClaim(any(Claim.class))).thenReturn(new Claim());
        mockMvc.perform(post("/api/v1/claims").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"patientId\":1,\"insuranceId\":1,\"description\":\"Visit\",\"invoiceId\":1,"
                        + "\"claimNumber\":\"C-1\",\"claimAmount\":100.00}"))
                .andExpect(status().isOk());
    }

    @Test
    void getClaim_returnsOk() throws Exception {
        when(claimService.getClaim(1L)).thenReturn(new Claim());
        mockMvc.perform(get("/api/v1/claims/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getClaimsByPatient_returnsOk() throws Exception {
        when(claimService.getClaimsByPatientId(1L)).thenReturn(Collections.singletonList(new Claim()));
        mockMvc.perform(get("/api/v1/claims/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getClaimsByInsurance_returnsOk() throws Exception {
        when(claimService.getClaimsByInsuranceId(1L)).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/claims/insurance/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void submitClaim_returnsOk() throws Exception {
        when(claimService.submitClaim(1L)).thenReturn(new Claim());
        mockMvc.perform(post("/api/v1/claims/1/submit").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void updateClaimStatus_returnsOk() throws Exception {
        when(claimService.updateClaimStatus(1L, "APPROVED")).thenReturn(new Claim());
        mockMvc.perform(patch("/api/v1/claims/1/status").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"APPROVED\",\"rejectionReason\":\"none\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateClaimStatus_validationFails_returnsBadRequest() throws Exception {
        mockMvc.perform(patch("/api/v1/claims/1/status").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"\",\"rejectionReason\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    // ---------- Insurance (4) ----------

    @Test
    void createInsurance_returnsOk() throws Exception {
        when(insuranceService.createInsurance(any(Insurance.class))).thenReturn(new Insurance());
        mockMvc.perform(post("/api/v1/insurance").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"providerName\":\"Acme\",\"policyNumber\":\"P-1\",\"memberId\":\"M-1\",\"planName\":\"Gold\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void createInsurance_invalid_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/insurance").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"providerName\":\"\",\"policyNumber\":\"\",\"memberId\":\"\",\"planName\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getInsurance_returnsOk() throws Exception {
        when(insuranceService.getInsurance(1L)).thenReturn(new Insurance());
        mockMvc.perform(get("/api/v1/insurance/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getInsurancesByPatient_returnsOk() throws Exception {
        when(insuranceService.getInsurancesByPatientId(1L))
                .thenReturn(Collections.singletonList(new Insurance()));
        mockMvc.perform(get("/api/v1/insurance/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void updateInsuranceStatus_returnsOk() throws Exception {
        when(insuranceService.updateInsuranceStatus(1L, "ACTIVE")).thenReturn(new Insurance());
        mockMvc.perform(put("/api/v1/insurance/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isOk());
    }

    // ---------- Payment (5) ----------

    @Test
    void createPayment_returnsOk() throws Exception {
        when(paymentService.createPayment(any(Payment.class))).thenReturn(new Payment());
        mockMvc.perform(post("/api/v1/payments").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"patientId\":1,\"amount\":50.00,\"invoiceId\":1}"))
                .andExpect(status().isOk());
    }

    @Test
    void getPayment_returnsOk() throws Exception {
        when(paymentService.getPayment(1L)).thenReturn(new Payment());
        mockMvc.perform(get("/api/v1/payments/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getPaymentsByInvoice_returnsOk() throws Exception {
        when(paymentService.getPaymentsByInvoiceId(1L))
                .thenReturn(Collections.singletonList(new Payment()));
        mockMvc.perform(get("/api/v1/payments/invoice/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void updatePaymentStatus_returnsOk() throws Exception {
        when(paymentService.updatePaymentStatus(1L, "COMPLETED")).thenReturn(new Payment());
        mockMvc.perform(patch("/api/v1/payments/1/status").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void refundPayment_returnsOk() throws Exception {
        when(paymentService.refundPayment(1L, new BigDecimal("10.50"))).thenReturn(new Payment());
        mockMvc.perform(post("/api/v1/payments/1/refund").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("10.50"))
                .andExpect(status().isOk());
    }

    // ---------- PreAuthorization (6) ----------

    @Test
    void createPreAuthorization_returnsOk() throws Exception {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(new Patient()));
        when(insuranceRepository.findById(1L)).thenReturn(Optional.of(new Insurance()));
        when(preAuthorizationService.createPreAuthorization(any(PreAuthorization.class)))
                .thenReturn(new PreAuthorization());
        mockMvc.perform(post("/api/v1/pre-authorizations").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"patientId\":1,\"insuranceId\":1,\"invoiceId\":1,"
                        + "\"medicalInformation\":\"Info\",\"serviceInformation\":\"Service\","
                        + "\"status\":\"PENDING\",\"requestedAmount\":200.00}"))
                .andExpect(status().isOk());
    }

    @Test
    void createPreAuthorization_invalidStatus_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/pre-authorizations").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"patientId\":1,\"insuranceId\":1,\"invoiceId\":1,"
                        + "\"medicalInformation\":\"Info\",\"serviceInformation\":\"Service\","
                        + "\"status\":\"BOGUS\",\"requestedAmount\":200.00}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPreAuthorization_returnsOk() throws Exception {
        when(preAuthorizationService.getPreAuthorization(1L)).thenReturn(new PreAuthorization());
        mockMvc.perform(get("/api/v1/pre-authorizations/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getPreAuthorizationsByPatient_returnsOk() throws Exception {
        when(preAuthorizationService.getPreAuthorizationsByPatientId(1L))
                .thenReturn(Collections.singletonList(new PreAuthorization()));
        mockMvc.perform(get("/api/v1/pre-authorizations/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getPreAuthorizationsByInsurance_returnsOk() throws Exception {
        when(preAuthorizationService.getPreAuthorizationsByInsuranceId(1L))
                .thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/pre-authorizations/insurance/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void submitPreAuthorization_returnsOk() throws Exception {
        when(preAuthorizationService.submitPreAuthorization(1L)).thenReturn(new PreAuthorization());
        mockMvc.perform(post("/api/v1/pre-authorizations/1/submit").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void updatePreAuthorizationStatus_returnsOk() throws Exception {
        when(preAuthorizationService.updatePreAuthorizationStatus(anyLong(), anyString()))
                .thenReturn(new PreAuthorization());
        mockMvc.perform(patch("/api/v1/pre-authorizations/1/status").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"APPROVED\"}"))
                .andExpect(status().isOk());
    }
}
