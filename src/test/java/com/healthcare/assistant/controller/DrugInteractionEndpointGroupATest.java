package com.healthcare.assistant.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.healthcare.assistant.entity.Contraindication;
import com.healthcare.assistant.entity.DosageInteraction;
import com.healthcare.assistant.entity.DrugAllergyCrossReactivity;
import com.healthcare.assistant.entity.DrugInteractionAggregation;
import com.healthcare.assistant.entity.DrugInteractionAlert;
import com.healthcare.assistant.entity.DrugInteractionAudit;
import com.healthcare.assistant.entity.DrugInteractionConsent;
import com.healthcare.assistant.entity.DrugInteractionContraindication;
import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.entity.Patient;
import com.healthcare.assistant.repository.MedicationRepository;
import com.healthcare.assistant.repository.PatientRepository;
import com.healthcare.assistant.service.ContraindicationService;
import com.healthcare.assistant.service.DosageInteractionService;
import com.healthcare.assistant.service.DrugAllergyCrossReactivityService;
import com.healthcare.assistant.service.DrugInteractionAggregationService;
import com.healthcare.assistant.service.DrugInteractionAlertService;
import com.healthcare.assistant.service.DrugInteractionAuditService;
import com.healthcare.assistant.service.DrugInteractionConsentService;
import com.healthcare.assistant.service.DrugInteractionContraindicationService;

/**
 * Phase 4b-1 endpoint coverage: first half of the /api/cds/* family.
 * All endpoints require PROVIDER role (mirrors production @PreAuthorize).
 */
@WebMvcTest({ContraindicationController.class, DosageInteractionController.class,
        DrugAllergyCrossReactivityController.class, DrugInteractionAggregationController.class,
        DrugInteractionAlertController.class, DrugInteractionAuditController.class,
        DrugInteractionConsentController.class, DrugInteractionContraindicationController.class})
@WithMockUser(roles = "PROVIDER")
class DrugInteractionEndpointGroupATest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContraindicationService contraindicationService;

    @MockBean
    private DosageInteractionService dosageInteractionService;

    @MockBean
    private DrugAllergyCrossReactivityService crossReactivityService;

    @MockBean
    private DrugInteractionAggregationService aggregationService;

    @MockBean
    private DrugInteractionAlertService alertService;

    @MockBean
    private DrugInteractionAuditService auditService;

    @MockBean
    private DrugInteractionConsentService consentService;

    @MockBean
    private DrugInteractionContraindicationService diContraindicationService;

    @MockBean
    private MedicationRepository medicationRepository;

    @MockBean
    private PatientRepository patientRepository;

    @TestConfiguration
    @EnableMethodSecurity(prePostEnabled = true)
    static class MethodSecurityConfig {
    }

    private Medication medication(long id) {
        Medication medication = new Medication();
        medication.setId(id);
        return medication;
    }

    // ---------- Contraindication (3) ----------

    @Test
    void detectContraindications_returnsOk() throws Exception {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(new Patient()));
        when(contraindicationService.detectContraindications(any(), any(), any(), anyString()))
                .thenReturn(Collections.singletonList(new Contraindication()));
        mockMvc.perform(post("/api/cds/contraindications/detect").with(csrf())
                .param("patientId", "1")
                .param("medicationIds", "1")
                .param("conditionIds", "1")
                .param("allergies", "none"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getContraindicationById_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cds/contraindications/1"))
                .andExpect(status().isOk());
    }

    @Test
    void listRecentContraindications_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cds/contraindications/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ---------- DosageInteraction (3) ----------

    @Test
    void checkDosage_returnsOk() throws Exception {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication(1L)));
        when(medicationRepository.findById(2L)).thenReturn(Optional.of(medication(2L)));
        mockMvc.perform(post("/api/cds/dosage-interactions/check-dosage").with(csrf())
                .param("medicationAId", "1")
                .param("medicationBId", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void checkDosage_unknownMedication_returnsBadRequest() throws Exception {
        when(medicationRepository.findById(9L)).thenReturn(Optional.empty());
        mockMvc.perform(post("/api/cds/dosage-interactions/check-dosage").with(csrf())
                .param("medicationAId", "9")
                .param("medicationBId", "2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDosageInteractionById_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/cds/dosage-interactions/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listRecentDosageInteractions_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cds/dosage-interactions/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ---------- DrugAllergyCrossReactivity (3) ----------

    @Test
    void checkCrossReactivity_returnsOk() throws Exception {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication(1L)));
        when(crossReactivityService.checkCrossReactivity(any(), anyString(), anyList()))
                .thenReturn(Collections.singletonList(new DrugAllergyCrossReactivity()));
        mockMvc.perform(post("/api/cds/drug-allergy-cross-reactivities/check-cross-reactivity/1").with(csrf())
                .param("medicationId", "1")
                .param("allergy", "penicillin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getCrossReactivityById_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cds/drug-allergy-cross-reactivities/1"))
                .andExpect(status().isOk());
    }

    @Test
    void listRecentCrossReactivities_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cds/drug-allergy-cross-reactivities/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ---------- DrugInteractionAggregation (3) ----------

    @Test
    void aggregateInteractions_returnsOk() throws Exception {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication(1L)));
        when(medicationRepository.findById(2L)).thenReturn(Optional.of(medication(2L)));
        when(aggregationService.aggregateInteractions(any(), any(), anyList()))
                .thenReturn(new DrugInteractionAggregation());
        mockMvc.perform(post("/api/cds/interaction-aggregations/aggregate").with(csrf())
                .param("medicationAId", "1")
                .param("medicationBId", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void getAggregationById_returnsOk() throws Exception {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication(1L)));
        mockMvc.perform(get("/api/cds/interaction-aggregations/1"))
                .andExpect(status().isOk());
    }

    @Test
    void listRecentAggregations_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cds/interaction-aggregations/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ---------- DrugInteractionAlert (3) ----------

    @Test
    void prioritizeAlerts_returnsOk() throws Exception {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication(1L)));
        when(medicationRepository.findById(2L)).thenReturn(Optional.of(medication(2L)));
        when(alertService.prioritizeAlerts(any(), any(), anyList()))
                .thenReturn(new DrugInteractionAlert());
        mockMvc.perform(post("/api/cds/drug-interaction-alerts/prioritize").with(csrf())
                .param("medicationAId", "1")
                .param("medicationBId", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
                .andExpect(status().isOk());
    }

    @Test
    void getAlertById_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/cds/drug-interaction-alerts/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listRecentAlerts_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cds/drug-interaction-alerts/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ---------- DrugInteractionAudit (3) ----------

    @Test
    void auditInteraction_returnsOk() throws Exception {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication(1L)));
        when(medicationRepository.findById(2L)).thenReturn(Optional.of(medication(2L)));
        when(auditService.auditInteraction(any(), any(), anyList()))
                .thenReturn(Collections.singletonList(new DrugInteractionAudit()));
        mockMvc.perform(post("/api/cds/audit").with(csrf())
                .param("medicationAId", "1")
                .param("medicationBId", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void getAuditById_returnsOk() throws Exception {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication(1L)));
        mockMvc.perform(get("/api/cds/audit/1"))
                .andExpect(status().isOk());
    }

    @Test
    void listRecentAudits_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cds/audit/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ---------- DrugInteractionConsent (3) ----------

    @Test
    void checkConsent_returnsOk() throws Exception {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication(1L)));
        when(medicationRepository.findById(2L)).thenReturn(Optional.of(medication(2L)));
        when(consentService.checkConsent(any(), any(), anyList())).thenReturn(true);
        mockMvc.perform(post("/api/cds/consent/check-consent").with(csrf())
                .param("medicationAId", "1")
                .param("medicationBId", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void getConsentById_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cds/consent/1"))
                .andExpect(status().isOk());
    }

    @Test
    void listRecentConsents_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cds/consent/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ---------- DrugInteractionContraindication (3) ----------

    @Test
    void checkContraindication_returnsOk() throws Exception {
        when(medicationRepository.findByIdAndPatientId(1L, 1L))
                .thenReturn(Optional.of(medication(1L)));
        when(medicationRepository.findByIdAndPatientId(2L, 1L))
                .thenReturn(Optional.of(medication(2L)));
        when(diContraindicationService.checkContraindication(any(), any(), anyList()))
                .thenReturn(new DrugInteractionContraindication());
        mockMvc.perform(post("/api/cds/drug-interaction-contraindications/check-contraindication").with(csrf())
                .param("medicationAId", "1")
                .param("medicationBId", "2")
                .param("patientId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void getDiContraindicationById_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cds/drug-interaction-contraindications/1"))
                .andExpect(status().isOk());
    }

    @Test
    void listRecentDiContraindications_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cds/drug-interaction-contraindications/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void providerRoleEnforced_onTemplatePost() throws Exception {
        mockMvc.perform(post("/api/cds/drug-interaction-contraindications/check-contraindication").with(csrf())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("plain").roles("USER"))
                .param("medicationAId", "1")
                .param("medicationBId", "1")
                .param("patientId", "1"))
                .andExpect(status().isForbidden());
    }
}
