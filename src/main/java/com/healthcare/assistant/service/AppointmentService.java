package com.healthcare.assistant.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.healthcare.assistant.entity.Appointment;
import com.healthcare.assistant.entity.Patient;
import com.healthcare.assistant.entity.Provider;
import com.healthcare.assistant.kafka.event.EventEnvelope;
import com.healthcare.assistant.kafka.producer.EventPublisher;
import com.healthcare.assistant.repository.AppointmentRepository;
import com.healthcare.assistant.repository.PatientRepository;
import com.healthcare.assistant.repository.ProviderRepository;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private EventPublisher eventPublisher;

    /**
     * Creates a new appointment after validating patient, provider, availability,
     * and ensuring no overlapping appointments.
     *
     * @return persisted appointment with generated ID
     */
    public Appointment saveAppointment(Appointment appointment) {
        // Validate patient exists
        validatePatientExists(appointment.getPatient().getId());

        // Validate provider exists
        validateProviderExists(appointment.getProvider().getId());

        // Validate provider is available
        validateProviderAvailability(appointment.getProvider().getId());

        // Validate no overlapping appointment for same provider at same time
        if (appointment.getId() == null) { // new appointment
            long overlappingCount = appointmentRepository.countByProviderIdAndAppointmentDateTime(
                    appointment.getProvider().getId(),
                    appointment.getAppointmentDateTime()
            );
            if (overlappingCount > 0) {
                throw new IllegalArgumentException("Provider is already booked at this date/time");
            }
        }

        // Set default status if not provided
        if (appointment.getStatus() == null || appointment.getStatus().trim().isEmpty()) {
            appointment.setStatus("SCHEDULED");
        }

        Appointment saved = appointmentRepository.save(appointment);

        // Publish AppointmentCreated event to Kafka
        eventPublisher.publish(
            new EventEnvelope(
                "appointment-created",
                "appointment",
                saved.getId().toString(),
                "appointment-service",
                Map.of("appointmentId", saved.getId().toString(), "status", saved.getStatus())
            ),
            "healthcare.appointment.events.created"
        );

        return saved;
    }

    /**
     * Validates that a patient with the given ID exists.
     * @throws IllegalArgumentException if patient does not exist
     */
    private void validatePatientExists(Long patientId) {
        if (!patientRepository.findById(patientId).isPresent()) {
            throw new IllegalArgumentException("Patient does not exist with id: " + patientId);
        }
    }

    /**
     * Validates that a provider with the given ID exists.
     * @throws IllegalArgumentException if provider does not exist
     */
    private void validateProviderExists(Long providerId) {
        if (!providerRepository.findById(providerId).isPresent()) {
            throw new IllegalArgumentException("Provider does not exist with id: " + providerId);
        }
    }

    /**
     * Validates that a provider is marked as available.
     * @throws IllegalArgumentException if provider is not available
     */
    private void validateProviderAvailability(Long providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Provider not found"));
        if (Boolean.FALSE.equals(provider.getAvailable())) {
            throw new IllegalArgumentException("Provider is not available");
        }
    }

    /**
     * Checks for overlapping appointments for the same provider at the same date-time.
     * @return count of overlapping appointments (0 = none)
     */
    public long countByProviderIdAndAppointmentDateTime(Long providerId, LocalDateTime dateTime) {
        return appointmentRepository.countByProviderIdAndAppointmentDateTime(providerId, dateTime);
    }

    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    @org.springframework.cache.annotation.Cacheable("appointments")
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public void deleteAppointment(Long id) {
        appointmentRepository.deleteById(id);
    }

    /**
     * Updates the status of an existing appointment.
     * Validates the status transition according to business rules.
     *
     * @param id       appointment ID
     * @param status   new status (e.g., CANCELLED, COMPLETED)
     * @throws IllegalArgumentException for invalid transitions or states
     */
    public void updateStatus(Long id, String status) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(id);
        if (optionalAppointment.isPresent()) {
            Appointment appointment = optionalAppointment.get();

            // Validate status is from allowed list
            if (status == null || !isStatusAllowed(status)) {
                throw new IllegalArgumentException("Status must be one of: " + String.join(", ", getAllowedStatuses()));
            }

            // Basic transition validation
            if ("CANCELLED".equals(status) && !"SCHEDULED".equals(appointment.getStatus())) {
                // Can only cancel a scheduled appointment
                throw new IllegalArgumentException("Only scheduled appointments can be cancelled");
            }

            appointment.setStatus(status);
            appointmentRepository.save(appointment);
        } else {
            throw new IllegalArgumentException("Appointment not found with id: " + id);
        }
    }

    private boolean isStatusAllowed(String status) {
        // Basic allowed statuses
        List<String> allowed = List.of("SCHEDULED", "COMPLETED", "CANCELLED", "RESCHEDULED");
        return allowed.stream().anyMatch(s -> s.equalsIgnoreCase(status));
    }

    private List<String> getAllowedStatuses() {
        return List.of("SCHEDULED", "COMPLETED", "CANCELLED", "RESCHEDULED");
    }

    /**
     * Suggests available appointment slots based on provider availability and
     * patient's preferred date.
     *
     * @param patientId   ID of the patient requesting suggestions
     * @param preferredDate date for which slots are suggested
     * @return list of suggested appointments with default details
     */
    public List<Appointment> suggestAppointments(Long patientId, LocalDate preferredDate) {
        validatePatientExists(patientId);

        // Get all providers who are available
        List<Provider> availableProviders = providerRepository.findAll()
                .stream()
                .filter(p -> Boolean.TRUE.equals(p.getAvailable()))
                .collect(Collectors.toList());

        List<Appointment> suggestions = new java.util.ArrayList<>();

        // Generate suggestions for each available provider on the preferred date
        for (Provider provider : availableProviders) {
            Appointment suggestion = new Appointment();
            // NOTE: In a real implementation, patient reference would be set appropriately
            suggestion.setProvider(provider);
            suggestion.setAppointmentDateTime(preferredDate.atStartOfDay());
            suggestion.setReason("Follow-up consultation");
            suggestion.setStatus("SCHEDULED");
            suggestions.add(suggestion);
        }

        return suggestions;
    }
}