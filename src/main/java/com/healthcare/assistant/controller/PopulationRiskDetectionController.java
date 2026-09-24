package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.PopulationHealthReport;
import com.healthcare.assistant.entity.PopulationRiskSegment;
import com.healthcare.assistant.service.PopulationRiskDetectionService;
import com.healthcare.assistant.service.PopulationHealthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/population-risk")
public class PopulationRiskDetectionController {

    private final PopulationRiskDetectionService riskDetectionService;
    private final PopulationHealthService populationHealthService;

    @Autowired
    public PopulationRiskDetectionController(PopulationRiskDetectionService riskDetectionService,
                                             PopulationHealthService populationHealthService) {
        this.riskDetectionService = riskDetectionService;
        this.populationHealthService = populationHealthService;
    }

    @PostMapping("/detect/{reportId}")
    public ResponseEntity<List<PopulationRiskSegment>> detectRisksByReport(@PathVariable Long reportId) {
        PopulationHealthReport report = populationHealthService.getReportById(reportId);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }
        List<PopulationRiskSegment> risks = riskDetectionService.detectRisksByReport(report);
        return ResponseEntity.ok(risks);
    }

    @PostMapping("/detect/emerging")
    public ResponseEntity<List<PopulationRiskSegment>> detectEmergingRisks(@RequestParam(name = "fromDate") LocalDateTime fromDate) {
        List<PopulationRiskSegment> risks = riskDetectionService.detectEmergingRisks(fromDate);
        return ResponseEntity.ok(risks);
    }

    @GetMapping("/category/{riskCategory}")
    public ResponseEntity<List<PopulationRiskSegment>> detectByRiskCategory(@PathVariable String riskCategory) {
        List<PopulationRiskSegment> risks = riskDetectionService.detectByRiskCategory(riskCategory);
        return ResponseEntity.ok(risks);
    }

    @PostMapping("/assess/{patientId}")
    public ResponseEntity<PopulationRiskSegment> assessPatientRisk(@PathVariable Long patientId,
                                                                 @RequestParam(name = "date") LocalDateTime assessmentDate) {
        PopulationRiskSegment risk = riskDetectionService.assessPatientRisk(patientId, assessmentDate);
        return ResponseEntity.ok(risk);
    }
}