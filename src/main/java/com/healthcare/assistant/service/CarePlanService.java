package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.CarePlan;

public interface CarePlanService {
    CarePlan generateCarePlan(Long patientId);
}