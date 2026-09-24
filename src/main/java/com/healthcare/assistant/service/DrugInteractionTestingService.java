package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.DrugInteractionTesting;
import com.healthcare.assistant.entity.Medication;
import com.healthcare.assistant.entity.enums.TestResult;
import com.healthcare.assistant.entity.enums.TestType;
import com.healthcare.assistant.entity.enums.ValidationStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DrugInteractionTestingService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Validate drug interactions between two medications against established
     * clinical criteria and test results.
     *
     * @param medicationA first medication
     * @param medicationB second medication
     * @param tests       list of drug interaction testing records
     * @return validation result with pass/fail/marginal determination
     */
    @Transactional
    public DrugInteractionTesting validateInteraction(Medication medicationA,
                                                      Medication medicationB,
                                                      List<DrugInteractionTesting> tests) {
        DrugInteractionTesting result = new DrugInteractionTesting(
                medicationA, medicationB, TestType.CLINICAL, LocalDateTime.now(),
                TestResult.MARGINAL, "", "PROVIDER", ValidationStatus.PENDING, "");

        List<String> validationErrors = new ArrayList<>();

        // Therapeutic duplication check
        if (hasTherapeuticDuplication(medicationA, medicationB)) {
            validationErrors.add("Therapeutic duplication detected: concurrent use of same drug class");
        }

        // Metabolic interaction check (CYP450 system)
        if (hasMetabolicInteraction(medicationA, medicationB)) {
            validationErrors.add("Metabolic interaction detected: CYP450 enzyme system interaction");
        }

        // Administration interaction check (route, timing, food)
        if (hasAdministrationInteraction(medicationA, medicationB)) {
            validationErrors.add("Administration interaction detected: route or timing conflict");
        }

        // Allergy check
        if (hasAllergyCheck(medicationA, medicationB)) {
            validationErrors.add("Allergy interaction detected: potential allergen conflict");
        }

        // Evaluate test results
        boolean hasPassingTest = tests != null && tests.stream()
                .anyMatch(t -> TestResult.PASS.equals(t.getTestResult()));

        boolean hasFailingTest = tests != null && tests.stream()
                .anyMatch(t -> TestResult.FAIL.equals(t.getTestResult()));

        if (!validationErrors.isEmpty()) {
            result.setTestResult(TestResult.FAIL);
            result.setTestDetails("Clinical criteria checks failed: " +
                    String.join("; ", validationErrors));
            result.setValidationStatus(ValidationStatus.REJECTED);
        } else if (hasFailingTest) {
            result.setTestResult(TestResult.FAIL);
            result.setTestDetails("Test results indicate failure with clinical concerns");
            result.setValidationStatus(ValidationStatus.REJECTED);
        } else if (hasPassingTest) {
            result.setTestResult(TestResult.PASS);
            result.setTestDetails("All clinical criteria passed; test results positive");
            result.setValidationStatus(ValidationStatus.APPROVED);
        } else {
            result.setTestResult(TestResult.MARGINAL);
            result.setTestDetails("No definitive pass/fail; marginal result pending further review");
            result.setValidationStatus(ValidationStatus.PENDING);
        }

        result.setDetectedAt(LocalDateTime.now());
        result.setUpdatedAt(LocalDateTime.now());

        return result;
    }

    /**
     * Check for therapeutic duplication (same drug class, same indication).
     */
    private boolean hasTherapeuticDuplication(Medication medicationA, Medication medicationB) {
        String nameA = medicationA.getName() == null ? "" : medicationA.getName().toLowerCase();
        String nameB = medicationB.getName() == null ? "" : medicationB.getName().toLowerCase();

        // Check for anticoagulant duplication
        boolean aIsAnticoagulant = nameA.contains("warfarin") || nameA.contains("heparin")
                || nameA.contains("enoxaparin") || nameA.contains("dalteparin")
                || nameA.contains("fondaparinux");
        boolean bIsAnticoagulant = nameB.contains("warfarin") || nameB.contains("heparin")
                || nameB.contains("enoxaparin") || nameB.contains("dalteparin")
                || nameB.contains("fondaparinux");

        if (aIsAnticoagulant && bIsAnticoagulant) {
            return true;
        }

        // Check for NSAID duplication
        boolean aIsNsaid = nameA.contains("ibuprofen") || nameA.contains("naproxen")
                || nameA.contains("diclofenac") || nameA.contains("indomethacin")
                || nameA.contains("ketorolac") || nameA.contains("meloxicam");
        boolean bIsNsaid = nameB.contains("ibuprofen") || nameB.contains("naproxen")
                || nameB.contains("diclofenac") || nameB.contains("indomethacin")
                || nameB.contains("ketorolac") || nameB.contains("meloxicam");

        if (aIsNsaid && bIsNsaid) {
            return true;
        }

        return false;
    }

    /**
     * Check for metabolic interactions via CYP450 enzyme system.
     */
    private boolean hasMetabolicInteraction(Medication medicationA, Medication medicationB) {
        String nameA = medicationA.getName() == null ? "" : medicationA.getName().toLowerCase();
        String nameB = medicationB.getName() == null ? "" : medicationB.getName().toLowerCase();

        // Check for CYP2C9 inhibitor + warfarin
        boolean aIsCyp2C9Inhibitor = nameA.contains("fluconazole");
        boolean bIsWarfarin = nameB.contains("warfarin");
        boolean bIsCyp2C9Inhibitor = nameB.contains("fluconazole");
        boolean aIsWarfarin = nameA.contains("warfarin");

        if ((aIsCyp2C9Inhibitor && bIsWarfarin) || (bIsCyp2C9Inhibitor && aIsWarfarin)) {
            return true;
        }

        // Check for CYP2C9 inducer + warfarin
        boolean aIsCyp2C9Inducer = nameA.contains("rifampin") || nameA.contains("carbamazepine")
                || nameA.contains("phenobarbital") || nameA.contains("phenytoin");
        boolean bIsCyp2C9Inducer = nameB.contains("rifampin") || nameB.contains("carbamazepine")
                || nameB.contains("phenobarbital") || nameB.contains("phenytoin");

        if ((aIsCyp2C9Inducer && bIsWarfarin) || (bIsCyp2C9Inducer && aIsWarfarin)) {
            return true;
        }

        return false;
    }

    /**
     * Check for administration interactions (route, timing, food).
     */
    private boolean hasAdministrationInteraction(Medication medicationA, Medication medicationB) {
        String nameA = medicationA.getName() == null ? "" : medicationA.getName().toLowerCase();
        String nameB = medicationB.getName() == null ? "" : medicationB.getName().toLowerCase();

        // Warfarin + aspirin administration interaction
        boolean aIsWarfarin = nameA.contains("warfarin");
        boolean bIsAspirin = nameB.contains("aspirin") || nameB.contains("acetylsalicylic");
        boolean aIsAspirin = nameA.contains("aspirin") || nameA.contains("acetylsalicylic");
        boolean bIsWarfarin = nameB.contains("warfarin");

        if ((aIsWarfarin && bIsAspirin) || (aIsAspirin && bIsWarfarin)) {
            return true;
        }

        // Antacid + fluoroquinolone absorption interference
        boolean aIsAntacid = nameA.contains("antacid");
        boolean bIsCiprofloxacin = nameB.contains("ciprofloxacin");
        boolean aIsCiprofloxacin = nameA.contains("ciprofloxacin");
        boolean bIsAntacid = nameB.contains("antacid");

        if ((aIsAntacid && bIsCiprofloxacin) || (aIsCiprofloxacin && bIsAntacid)) {
            return true;
        }

        return false;
    }

    /**
     * Check for drug-allergy interactions.
     */
    private boolean hasAllergyCheck(Medication medicationA, Medication medicationB) {
        String nameA = medicationA.getName() == null ? "" : medicationA.getName().toLowerCase();
        String nameB = medicationB.getName() == null ? "" : medicationB.getName().toLowerCase();

        // Check for penicillin allergy + cephalosporin cross-reactivity
        boolean aHasPenicillinAllergy = nameA.contains("penicillin") || nameA.contains("penicillins");
        boolean bIsCephalosporin = nameB.contains("cef") || nameB.contains("cefaclor")
                || nameB.contains("cefpodoxime") || nameB.contains("ceftriaxone")
                || nameB.contains("cefuroxime");

        boolean bHasPenicillinAllergy = nameB.contains("penicillin") || nameB.contains("penicillins");
        boolean aIsCephalosporin = nameA.contains("cef") || nameA.contains("cefaclor")
                || nameA.contains("cefpodoxime") || nameA.contains("ceftriaxone")
                || nameA.contains("cefuroxime");

        if ((aHasPenicillinAllergy && bIsCephalosporin) || (bHasPenicillinAllergy && aIsCephalosporin)) {
            return true;
        }

        // Check for shellfish allergy + iodinated contrast
        boolean aHasShellfishAllergy = nameA.contains("shellfish") || nameA.contains("shrimp")
                || nameA.contains("crab") || nameA.contains("mollusk");
        boolean bIsIodinatedContrast = nameB.contains("contrast") || nameB.contains("iohexol")
                || nameB.contains("iopamidol") || nameB.contains("iodine");

        boolean bHasShellfishAllergy = nameB.contains("shellfish") || nameB.contains("shrimp")
                || nameB.contains("crab") || nameB.contains("mollusk");
        boolean aIsIodinatedContrast = nameA.contains("contrast") || nameA.contains("iohexol")
                || nameA.contains("iopamidol") || nameA.contains("iodine");

        if ((aHasShellfishAllergy && bIsIodinatedContrast) || (bHasShellfishAllergy && aIsIodinatedContrast)) {
            return true;
        }

        return false;
    }
}