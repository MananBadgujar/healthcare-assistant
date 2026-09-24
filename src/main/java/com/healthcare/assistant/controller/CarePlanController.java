package com.healthcare.assistant.controller;

import com.healthcare.assistant.entity.CarePlan;
import com.healthcare.assistant.service.CarePlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/careplan")
public class CarePlanController {

    private final CarePlanService carePlanService;

    @Autowired
    public CarePlanController(CarePlanService carePlanService) {
        this.carePlanService = carePlanService;
    }

    @PostMapping("/generate")
    public ResponseEntity<CarePlan> generateCarePlan(@RequestBody Long patientId) {
        // TODO: implement generation logic
        CarePlan plan = carePlanService.generateCarePlan(patientId);
        return ResponseEntity.ok(plan);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<CarePlan> getCarePlan(@PathVariable Long patientId) {
        // TODO: implement retrieval logic
        CarePlan plan = carePlanService.generateCarePlan(patientId);
        return ResponseEntity.ok(plan);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateCarePlan(@PathVariable Long id,
                                                    @RequestBody String newContent) {
        // TODO: implement update logic
        return ResponseEntity.ok().build(); // placeholder
    }
}