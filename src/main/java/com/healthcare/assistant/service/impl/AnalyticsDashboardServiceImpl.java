package com.healthcare.assistant.service.impl;

import com.healthcare.assistant.entity.AnalyticsDashboardStats;
import com.healthcare.assistant.entity.Patient;
import com.healthcare.assistant.entity.Appointment;
import com.healthcare.assistant.entity.TelehealthSession;
import com.healthcare.assistant.entity.InventoryItem;
import com.healthcare.assistant.repository.PatientRepository;
import com.healthcare.assistant.repository.AppointmentRepository;
import com.healthcare.assistant.repository.TelehealthSessionRepository;
import com.healthcare.assistant.repository.InventoryItemRepository;
import com.healthcare.assistant.service.AnalyticsDashboardService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnalyticsDashboardServiceImpl implements AnalyticsDashboardService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TelehealthSessionRepository telehealthSessionRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public AnalyticsDashboardStats createStats(String statType, LocalDateTime start, LocalDateTime end) {
        AnalyticsDashboardStats stats = new AnalyticsDashboardStats(statType, start, end);
        Long count = 0L;

        if ("PATIENT_COUNT".equals(statType)) {
            count = patientRepository.count();
        } else if ("APPOINTMENT_COUNT".equals(statType)) {
            count = appointmentRepository.count();
        } else if ("TELEHEALTH_SESSIONS".equals(statType)) {
            count = telehealthSessionRepository.count();
        } else if ("INVENTORY_ALERTS".equals(statType)) {
            count = countInventoryAlerts();
        } else if ("MEDICATION_ADHERENCE".equals(statType)) {
            count = Math.round(calculateMedicationAdherence());
        }

        stats.setCountValue(count.intValue());
        return stats;
    }

    @Override
    public AnalyticsDashboardStats getStatsById(Long id) {
        return null; // Not implemented - use repository directly
    }

    @Override
    public List<AnalyticsDashboardStats> getStatsByType(String statType) {
        List<AnalyticsDashboardStats> all = java.util.Collections.emptyList();

        if ("PATIENT_COUNT".equals(statType)) {
            long patientCount = patientRepository.count();
            AnalyticsDashboardStats stats = new AnalyticsDashboardStats("PATIENT_COUNT", LocalDateTime.now().minusDays(30), LocalDateTime.now());
            stats.setCountValue((int) patientCount);
            all = java.util.Collections.singletonList(stats);
        } else if ("APPOINTMENT_COUNT".equals(statType)) {
            long appointmentCount = appointmentRepository.count();
            AnalyticsDashboardStats stats = new AnalyticsDashboardStats("APPOINTMENT_COUNT", LocalDateTime.now().minusDays(30), LocalDateTime.now());
            stats.setCountValue((int) appointmentCount);
            all = java.util.Collections.singletonList(stats);
        } else if ("TELEHEALTH_SESSIONS".equals(statType)) {
            long telehealthCount = telehealthSessionRepository.count();
            AnalyticsDashboardStats stats = new AnalyticsDashboardStats("TELEHEALTH_SESSIONS", LocalDateTime.now().minusDays(30), LocalDateTime.now());
            stats.setCountValue((int) telehealthCount);
            all = java.util.Collections.singletonList(stats);
        } else if ("INVENTORY_ALERTS".equals(statType)) {
            long inventoryAlerts = countInventoryAlerts();
            AnalyticsDashboardStats stats = new AnalyticsDashboardStats("INVENTORY_ALERTS", LocalDateTime.now().minusDays(30), LocalDateTime.now());
            stats.setCountValue((int) inventoryAlerts);
            all = java.util.Collections.singletonList(stats);
        } else if ("MEDICATION_ADHERENCE".equals(statType)) {
            double adherence = calculateMedicationAdherence();
            AnalyticsDashboardStats stats = new AnalyticsDashboardStats("MEDICATION_ADHERENCE", LocalDateTime.now().minusDays(30), LocalDateTime.now());
            stats.setCountValue((int) Math.round(adherence));
            all = java.util.Collections.singletonList(stats);
        }

        return all;
    }

    @Override
    public List<AnalyticsDashboardStats> getStatsByPeriod(LocalDateTime start, LocalDateTime end) {
        java.util.List<AnalyticsDashboardStats> all = new java.util.ArrayList<>();

        long patientCount = patientRepository.count();
        AnalyticsDashboardStats stats1 = new AnalyticsDashboardStats("PATIENT_COUNT", start, end);
        stats1.setCountValue((int) patientCount);
        all.add(stats1);

        long appointmentCount = appointmentRepository.count();
        AnalyticsDashboardStats stats2 = new AnalyticsDashboardStats("APPOINTMENT_COUNT", start, end);
        stats2.setCountValue((int) appointmentCount);
        all.add(stats2);

        long telehealthCount = telehealthSessionRepository.count();
        AnalyticsDashboardStats stats3 = new AnalyticsDashboardStats("TELEHEALTH_SESSIONS", start, end);
        stats3.setCountValue((int) telehealthCount);
        all.add(stats3);

        long inventoryAlerts = countInventoryAlerts();
        AnalyticsDashboardStats stats4 = new AnalyticsDashboardStats("INVENTORY_ALERTS", start, end);
        stats4.setCountValue((int) inventoryAlerts);
        all.add(stats4);

        double adherence = calculateMedicationAdherence();
        AnalyticsDashboardStats stats5 = new AnalyticsDashboardStats("MEDICATION_ADHERENCE", start, end);
        stats5.setCountValue((int) Math.round(adherence));
        all.add(stats5);

        return all;
    }

    @Override
    public List<AnalyticsDashboardStats> getAllStats() {
        java.util.List<AnalyticsDashboardStats> all = new java.util.ArrayList<>();

        long patientCount = patientRepository.count();
        AnalyticsDashboardStats stats1 = new AnalyticsDashboardStats("PATIENT_COUNT", LocalDateTime.now().minusDays(30), LocalDateTime.now());
        stats1.setCountValue((int) patientCount);
        all.add(stats1);

        long appointmentCount = appointmentRepository.count();
        AnalyticsDashboardStats stats2 = new AnalyticsDashboardStats("APPOINTMENT_COUNT", LocalDateTime.now().minusDays(30), LocalDateTime.now());
        stats2.setCountValue((int) appointmentCount);
        all.add(stats2);

        long telehealthCount = telehealthSessionRepository.count();
        AnalyticsDashboardStats stats3 = new AnalyticsDashboardStats("TELEHEALTH_SESSIONS", LocalDateTime.now().minusDays(30), LocalDateTime.now());
        stats3.setCountValue((int) telehealthCount);
        all.add(stats3);

        long inventoryAlerts = countInventoryAlerts();
        AnalyticsDashboardStats stats4 = new AnalyticsDashboardStats("INVENTORY_ALERTS", LocalDateTime.now().minusDays(30), LocalDateTime.now());
        stats4.setCountValue((int) inventoryAlerts);
        all.add(stats4);

        double adherence = calculateMedicationAdherence();
        AnalyticsDashboardStats stats5 = new AnalyticsDashboardStats("MEDICATION_ADHERENCE", LocalDateTime.now().minusDays(30), LocalDateTime.now());
        stats5.setCountValue((int) Math.round(adherence));
        all.add(stats5);

        return all;
    }

    private long countInventoryAlerts() {
        // Count inventory items with low stock using JPQL
        jakarta.persistence.Query query = entityManager.createQuery(
                "SELECT COUNT(i) FROM InventoryItem i WHERE i.quantity < i.reorderLevel");
        return (long) query.getSingleResult();
    }

    private double calculateMedicationAdherence() {
        // Placeholder: return average adherence percentage
        // In a full implementation, this would query medication administration records
        return 87.5;
    }
}