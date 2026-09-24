package com.healthcare.assistant.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
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
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.healthcare.assistant.entity.DrugInteraction;
import com.healthcare.assistant.entity.DrugInteractionDocumentation;
import com.healthcare.assistant.entity.DrugInteractionFollowup;
import com.healthcare.assistant.entity.DrugInteractionResolution;
import com.healthcare.assistant.entity.DrugInteractionRisk;
import com.healthcare.assistant.entity.DrugInteractionSeverityScore;
import com.healthcare.assistant.entity.DrugInteractionTesting;
import com.healthcare.assistant.entity.MaxDailyDose;
import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.entity.Patient;
import com.healthcare.assistant.entity.PolypharmacyRisk;
import com.healthcare.assistant.repository.MedicationRepository;
import com.healthcare.assistant.repository.PatientRepository;
import com.healthcare.assistant.service.DrugInteractionDocumentationService;
import com.healthcare.assistant.service.DrugInteractionFollowupService;
import com.healthcare.assistant.service.DrugInteractionResolutionService;
import com.healthcare.assistant.service.DrugInteractionRiskService;
import com.healthcare.assistant.service.DrugInteractionService;
import com.healthcare.assistant.service.DrugInteractionSeverityScoreService;
import com.healthcare.assistant.service.DrugInteractionTestingService;
import com.healthcare.assistant.service.MaxDailyDoseService;
import com.healthcare.assistant.service.PolypharmacyRiskService;

/**
 * Phase 4b-2 endpoint coverage: second half of the /api/cds/* family.
 */
@WebMvcTest({DrugInteractionController.class, DrugInteractionDocumentationController.class,
        DrugInteractionFollowupController.class, DrugInteractionResolutionController.class,
        DrugInteractionRiskController.class, DrugInteractionSeverityScoreController.class,
        DrugInteractionTestingController.class, MaxDailyDoseController.class,
        PolypharmacyRiskController.class})
@WithMockUser(roles = "PROVIDER")
class DrugInteractionEndpointGroupBTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DrugInteractionService interactionService;

    @MockBean
    private DrugInteractionDocumentationService documentationService;

    @MockBean
    private DrugInteractionFollowupService followupService;

    @MockBean
    private DrugInteractionResolutionService resolutionService;

    @MockBean
    private DrugInteractionRiskService riskService;

    @MockBean
    private DrugInteractionSeverityScoreService severityScoreService;

    @MockBean
    private DrugInteractionTestingService testingService;

    @MockBean
    private MaxDailyDoseService maxDailyDoseService;

    @MockBean
    private PolypharmacyRiskService polypharmacyRiskService;

    @MockBean
    private MedicationRepository medicationRepository;

    @MockBean
    private PatientRepository patientRepository;

    private Medication medication(long id) {
        Medication medication = new Medication();
        medication.setId(id);
        return medication;
    }

    // ---------- DrugInteraction (5) ----------

    @Test
    void checkInteraction_returnsOk() throws Exception {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication(1L)));
        when(medicationRepository.findById(2L)).thenReturn(Optional.of(medication(2L)));
        when(interactionService.checkInteractions(any(), any()))
                .thenReturn(Collections.singletonList(new DrugInteraction()));
        mockMvc.perform(get("/api/cds/interactions")
                .param("medicationAId", "1")
                .param("medicationBId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void checkInteraction_unknownMedication_returnsBadRequest() throws Exception {
        when(medicationRepository.findById(9L)).thenReturn(Optional.empty());
        when(medicationRepository.findById(2L)).thenReturn(Optional.of(medication(2L)));
        mockMvc.perform(get("/api/cds/interactions")
                .param("medicationAId", "9")
                .param("medicationBId", "2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkDrugAllergyInteraction_returnsOk() throws Exception {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication(1L)));
        when(interactionService.checkDrugAllergyInteraction(any(), anyString()))
                .thenReturn(Collections.singletonList(new DrugInteraction()));
        mockMvc.perform(get("/api/cds/interactions/allergy")
                .param("medicationId", "1")
                .param("allergy", "sulfa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getMedicationById_returnsOk() throws Exception {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication(1L)));
        mockMvc.perform(get("/api/cds/interactions/1"))
                .andExpect(status().isOk());
    }

    @Test
    void listRecentInteractions_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cds/interactions/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void checkInteractionsMultiple_returnsOk() throws Exception {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication(1L)));
        when(medicationRepository.findById(2L)).thenReturn(Optional.of(medication(2L)));
        when(interactionService.checkInteractions(any(), any()))
                .thenReturn(Collections.singletonList(new DrugInteraction()));
        mockMvc.perform(post("/api/cds/interactions/check-multiple").with(csrf())
                .param("medicationId", "1")
                .param("otherMedicationIds", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ---------- DrugInteractionDocumentation (3) ----------

    @Test
    void documentInteraction_returnsOk() throws Exception {
        when(medicationRepository.findByIdAndPatientId(1L, 1L))
                .thenReturn(Optional.of(medication(1L)));
        when(medicationRepository.findByIdAndPatientId(2L, 1L))
                .thenReturn(Optional.of(medication(2L)));
        Answer<DrugInteractionDocumentation> mutating = invocation -> {
            List<DrugInteractionDocumentation> docs = invocation.getArgument(2);
            docs.add(new DrugInteractionDocumentation());
            return docs.get(0);
        };
        when(documentationService.documentInteraction(any(), any(), anyList())).thenAnswer(mutating);
        mockMvc.perform(post("/api/cds/drug-interaction-documentation/document").with(csrf())
                .param("medicationAId", "1")
                .param("medicationBId", "2")
                .param("patientId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void getDocumentationById_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cds/drug-interaction-documentation/1"))
                .andExpect(status().isOk());
    }

    @Test
    void listRecentDocumentations_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cds/drug-interaction-documentation/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ---------- DrugInteractionFollowup (3) ----------

    @Test
    void checkFollowup_returnsOk() throws Exception {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication(1L)));
        when(medicationRepository.findById(2L)).thenReturn(Optional.of(medication(2L)));
        when(followupService.checkFollowup(any(), any(), anyList()))
                .thenReturn(Collections.singletonList(new DrugInteractionFollowup()));
        mockMvc.perform(post("/api/cds/followup/check-followup").with(csrf())
                .param("medicationAId", "1")
                .param("medicationBId", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void getFollowupById_returnsOk() throws Exception {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication(1L)));
        mockMvc.perform(get("/api/cds/followup/1"))
                .andExpect(status().isOk());
    }

    @Test
    void listRecentFollowups_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cds/followup/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ---------- DrugInteractionResolution (3) ----------

    @Test
    void recordResolution_returnsOk() throws Exception {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication(1L)));
        when(medicationRepository.findById(2L)).thenReturn(Optional.of(medication(2L)));
        when(resolutionService.resolveInteraction(any(), any(), any()))
                .thenReturn(new DrugInteractionResolution());
        mockMvc.perform(post("/api/cds/interaction-resolutions/resolve").with(csrf())
                .param("medicationAId", "1")
                .param("medicationBId", "2")
                .param("resolutionStatus", "RESOLVED")
                .param("resolutionCategory", "DOSE_ADJUSTED")
                .param("resolvedBy", "dr")
                .param("resolutionNotes", "ok")
                .param("actionTaken", "adjusted"))
                .andExpect(status().isOk());
    }

    @Test
    void getResolutionById_returnsOk() throws Exception {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication(1L)));
        mockMvc.perform(get("/api/cds/interaction-resolutions/1"))
                .andExpect(status().isOk());
    }

    @Test
    void listRecentResolutions_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cds/interaction-resolutions/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ---------- DrugInteractionRisk (3) ----------

    @Test
    void assessRisk_returnsOk() throws Exception {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication(1L)));
        when(medicationRepository.findById(2L)).thenReturn(Optional.of(medication(2L)));
        mockMvc.perform(post("/api/cds/drug-interaction-risks/assess-risk").with(csrf())
                .param("medicationAId", "1")
                .param("medicationBId", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void getRiskById_returnsOk() throws Exception {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication(1L)));
        mockMvc.perform(get("/api/cds/drug-interaction-risks/1"))
                .andExpect(status().isOk());
    }

    @Test
    void listRecentRisks_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cds/drug-interaction-risks/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ---------- DrugInteractionSeverityScore (3) ----------

    @Test
    void calculateSeverityScore_returnsOk() throws Exception {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication(1L)));
        when(medicationRepository.findById(2L)).thenReturn(Optional.of(medication(2L)));
        when(severityScoreService.calculateSeverityScore(any(), any(), anyList())).thenReturn(3);
        mockMvc.perform(post("/api/cds/severity-scores/calculate-score").with(csrf())
                .param("medicationAId", "1")
                .param("medicationBId", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void getSeverityScoreById_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cds/severity-scores/1"))
                .andExpect(status().isOk());
    }

    @Test
    void listRecentSeverityScores_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cds/severity-scores/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ---------- DrugInteractionTesting (3) ----------

    @Test
    void validateInteraction_returnsOk() throws Exception {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication(1L)));
        when(medicationRepository.findById(2L)).thenReturn(Optional.of(medication(2L)));
        when(testingService.validateInteraction(any(), any(), anyList()))
                .thenReturn(new DrugInteractionTesting());
        mockMvc.perform(post("/api/cds/drug-interaction-testing/validate").with(csrf())
                .param("medicationAId", "1")
                .param("medicationBId", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
                .andExpect(status().isOk());
    }

    @Test
    void getTestingById_returnsOk() throws Exception {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication(1L)));
        mockMvc.perform(get("/api/cds/drug-interaction-testing/1"))
                .andExpect(status().isOk());
    }

    @Test
    void listRecentTesting_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cds/drug-interaction-testing/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ---------- MaxDailyDose (3) ----------

    @Test
    void checkMaxDailyDose_returnsOk() throws Exception {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication(1L)));
        when(maxDailyDoseService.checkMaxDailyDose(any(), anyDouble(), anyList()))
                .thenReturn(Collections.singletonList(new MaxDailyDose()));
        mockMvc.perform(post("/api/cds/max-daily-dose/check-max-dose").with(csrf())
                .param("medicationId", "1")
                .param("currentDailyDose", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getMaxDailyDoseById_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cds/max-daily-dose/1"))
                .andExpect(status().isOk());
    }

    @Test
    void listRecentMaxDailyDose_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cds/max-daily-dose/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ---------- PolypharmacyRisk (3) ----------

    @Test
    void checkPolypharmacyRisk_returnsOk() throws Exception {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(new Patient()));
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication(1L)));
        when(medicationRepository.findById(2L)).thenReturn(Optional.of(medication(2L)));
        when(polypharmacyRiskService.checkPolypharmacyRisk(anyList(), any()))
                .thenReturn(new PolypharmacyRisk());
        mockMvc.perform(post("/api/cds/polypharmacy-risks/check-risk").with(csrf())
                .param("patientId", "1")
                .param("medicationIds", "1", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void getPolypharmacyRiskById_returnsOk() throws Exception {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication(1L)));
        mockMvc.perform(get("/api/cds/polypharmacy-risks/1"))
                .andExpect(status().isOk());
    }

    @Test
    void listRecentPolypharmacyRisks_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cds/polypharmacy-risks/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
