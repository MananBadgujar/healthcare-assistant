package com.healthcare.assistant.service.impl;

import com.healthcare.assistant.entity.CarePlan;
import com.healthcare.assistant.repository.CarePlanRepository;
import com.healthcare.assistant.service.CarePlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CarePlanServiceImpl implements CarePlanService {

    private final CarePlanRepository repository;

    @Autowired
    public CarePlanServiceImpl(CarePlanRepository repository) {
        this.repository = repository;
    }

    @Override
    public CarePlan generateCarePlan(Long patientId) {
        CarePlan plan = new CarePlan(patientId, "Sample care plan content", "2025-01-01T00:00:00");
        return repository.save(plan);
    }
}