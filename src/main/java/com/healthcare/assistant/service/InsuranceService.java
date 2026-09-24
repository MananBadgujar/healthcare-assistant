package com.healthcare.assistant.service;

import com.healthcare.assistant.entity.Insurance;
import java.util.List;

public interface InsuranceService {
    Insurance createInsurance(Insurance insurance);
    Insurance getInsurance(Long id);
    List<Insurance> getInsurancesByPatientId(Long patientId);
    Insurance updateInsuranceStatus(Long id, String status);
}