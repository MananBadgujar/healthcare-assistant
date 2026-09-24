package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.Contraindication;
import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.entity.Patient;
import com.healthcare.assistant.entity.enums.ContraindicationSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContraindicationServiceTest {

    private ContraindicationService contraindicationService;

    @BeforeEach
    void setUp() {
        contraindicationService = new ContraindicationService();
    }

    @Test
    void medicationConditionContraindicationAceInhibitorAngioedema() {
        Patient patient = new Patient();
        patient.setName("Test Patient");

        Medication captopril = new Medication();
        captopril.setName("Captopril");

        List<String> conditionNames = new ArrayList<>();
        conditionNames.add("History of angioedema");

        List<Contraindication> contraindications = contraindicationService.detectContraindications(
                patient, java.util.Collections.singletonList(captopril), conditionNames, "");

        assertFalse(contraindications.isEmpty(), "Expected contraindication for ACE inhibitor + angioedema");
        assertEquals(ContraindicationSeverity.CRITICAL, contraindications.get(0).getSeverity(),
                "Expected CRITICAL severity for ACE inhibitor in angioedema history");
        assertEquals("Captopril", contraindications.get(0).getMedicationName(),
                "Expected Captopril as the contraindicated medication");
        assertEquals("Test Patient", contraindications.get(0).getPatient().getName(),
                "Expected Test Patient as the patient");
        assertEquals("history of angioedema", contraindications.get(0).getConditionName(),
                "Expected condition name in contraindication");
    }

    @Test
    void medicationConditionContraindicationBetaBlockerAsthma() {
        Patient patient = new Patient();
        patient.setName("Test Patient");

        Medication propranolol = new Medication();
        propranolol.setName("Propranolol");

        List<String> conditionNames = new ArrayList<>();
        conditionNames.add("Asthma");

        List<Contraindication> contraindications = contraindicationService.detectContraindications(
                patient, java.util.Collections.singletonList(propranolol), conditionNames, "");

        assertFalse(contraindications.isEmpty(), "Expected contraindication for beta-blocker + asthma");
        assertEquals(ContraindicationSeverity.HIGH, contraindications.get(0).getSeverity(),
                "Expected HIGH severity for beta-blocker in asthma");
        assertEquals("Propranolol", contraindications.get(0).getMedicationName(),
                "Expected Propranolol as the contraindicated medication");
    }

    @Test
    void medicationAllergyContraindicationPenicillin() {
        Patient patient = new Patient();
        patient.setName("Test Patient");

        Medication penicillin = new Medication();
        penicillin.setName("Penicillin");

        List<Contraindication> contraindications = contraindicationService.detectContraindications(
                patient, java.util.Collections.singletonList(penicillin), java.util.Collections.EMPTY_LIST, "penicillin allergy");

        assertFalse(contraindications.isEmpty(), "Expected contraindication for penicillin + penicillin allergy");
        assertEquals(ContraindicationSeverity.CRITICAL, contraindications.get(0).getSeverity(),
                "Expected CRITICAL severity for penicillin with allergy");
        assertEquals("Penicillin", contraindications.get(0).getMedicationName(),
                "Expected Penicillin as the contraindicated medication");
    }

    @Test
    void noContraindicationNoRiskFactors() {
        Patient patient = new Patient();
        patient.setName("Test Patient");

        Medication metformin = new Medication();
        metformin.setName("Metformin");

        List<Contraindication> contraindications = contraindicationService.detectContraindications(
                patient, java.util.Collections.singletonList(metformin), java.util.Collections.EMPTY_LIST, "");

        assertTrue(contraindications.isEmpty(), "Expected no contraindication without risk factors");
    }

    @Test
    void severitySorting() {
        Patient patient = new Patient();
        patient.setName("Test Patient");

        Medication warfarin = new Medication();
        warfarin.setName("Warfarin");

        List<String> conditionNames = new ArrayList<>();
        conditionNames.add("Pregnancy");

        List<Contraindication> contraindications = contraindicationService.detectContraindications(
                patient, java.util.Collections.singletonList(warfarin), conditionNames, "");

        assertFalse(contraindications.isEmpty(), "Expected contraindication");
        assertEquals("Warfarin", contraindications.get(0).getMedicationName(),
                "Expected Warfarin as the contraindicated medication");
        assertEquals("pregnancy", contraindications.get(0).getConditionName(),
                "Expected Pregnancy as the condition name (lowercase, normalized)");
    }
}