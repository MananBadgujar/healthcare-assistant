package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.DrugInteractionDocumentation;
import com.healthcare.assistant.entity.Medication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Service
public class DrugInteractionDocumentationService {

    public DrugInteractionDocumentation documentInteraction(Medication medicationA,
                                                            Medication medicationB,
                                                            List<DrugInteractionDocumentation> documentations) {

        DrugInteractionDocumentation documentation = new DrugInteractionDocumentation(
                medicationA, medicationB,
                "PROVIDER", LocalDateTime.now(),
                "Documented interaction between " + medicationA.getName() + " and " + medicationB.getName(),
                "MODERATE",
                "Clinical reference guide",
                "DRUG_INTERACTION"
        );

        documentations.add(documentation);

        return documentation;
    }
}