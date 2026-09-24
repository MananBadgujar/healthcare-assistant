package com.healthcare.assistant.controller;

import com.healthcare.assistant.dto.MedicalHistoryRequest;
import com.healthcare.assistant.dto.PatientContext;
import com.healthcare.assistant.entity.Patient;
import com.healthcare.assistant.entity.Record;
import com.healthcare.assistant.repository.PatientRepository;
import com.healthcare.assistant.repository.RecordRepository;
import com.healthcare.assistant.service.PatientContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Optional;

class RecordHistoryControllerPostTest {

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientContextService patientContextService;

    @InjectMocks
    private RecordHistoryController controller;

    private Patient dummyPatient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dummyPatient = new Patient();
        dummyPatient.setId(1L);
        dummyPatient.setFirstName("John");
        dummyPatient.setLastName("Doe");
        dummyPatient.setEmail("john.doe@example.com");
    }

    @Test
    void contextWithAuthenticatedPatient_ShouldCreateRecord_Return201() {
        // Arrange
        Long patientId = 1L;
        MedicalHistoryRequest request = new MedicalHistoryRequest("Follow-up", "Feeling better.");
        Record savedRecord = new Record();
        savedRecord.setId(100L);
        savedRecord.setPatient(dummyPatient);
        savedRecord.setType(request.getType());
        savedRecord.setContent(request.getContent());
        savedRecord.setCreatedAt(LocalDateTime.now());

        when(patientContextService.getCurrentPatientContext()).thenReturn(new PatientContext(1L, "John", "Doe", "M", "1990-01-01"));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(dummyPatient));
        when(recordRepository.save(any(Record.class))).thenReturn(savedRecord);

        // Act
        ResponseEntity<Record> response = controller.createMedicalHistory(patientId, request);

        // Assert
        assertEquals(201, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(request.getType(), response.getBody().getType());
        assertEquals(request.getContent(), response.getBody().getContent());
        verify(recordRepository).save(any(Record.class));
    }

    @Test
    void patientIdMismatch_ShouldReturn403() {
        // Arrange
        Long pathPatientId = 2L;
        MedicalHistoryRequest request = new MedicalHistoryRequest("Symptom", "Headache");
        when(patientContextService.getCurrentPatientContext()).thenReturn(new PatientContext(1L, "John", "Doe", "M", "1990-01-01"));
        Patient dummyOtherPatient = new Patient();
        dummyOtherPatient.setId(2L);
        dummyOtherPatient.setFirstName("Other");
        dummyOtherPatient.setLastName("Patient");
        dummyOtherPatient.setEmail("other.patient@example.com");
        when(patientRepository.findById(2L)).thenReturn(Optional.of(dummyOtherPatient));

        // Act
        ResponseEntity<Record> response = controller.createMedicalHistory(pathPatientId, request);

        // Assert
        assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    void patientNotFound_ShouldReturn404() {
        // Arrange
        Long patientId = 99L;
        MedicalHistoryRequest request = new MedicalHistoryRequest("Symptom", "Pain");
        when(patientContextService.getCurrentPatientContext()).thenReturn(new PatientContext(1L, "John", "Doe", "M", "1990-01-01"));
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Record> response = controller.createMedicalHistory(patientId, request);

        // Assert
        assertEquals(404, response.getStatusCodeValue());
        verify(recordRepository, never()).save(any(Record.class));
    }

    @Test
    void nullType_ShouldReturn400() {
        // Arrange
        Long patientId = 1L;
        MedicalHistoryRequest request = new MedicalHistoryRequest(null, "Some content");
        when(patientContextService.getCurrentPatientContext()).thenReturn(new PatientContext(1L, "John", "Doe", "M", "1990-01-01"));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(dummyPatient));

        // Act
        ResponseEntity<Record> response = controller.createMedicalHistory(patientId, request);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        verify(recordRepository, never()).save(any(Record.class));
    }

    @Test
    void blankType_ShouldReturn400() {
        // Arrange
        Long patientId = 1L;
        MedicalHistoryRequest request = new MedicalHistoryRequest("   ", "Some content");
        when(patientContextService.getCurrentPatientContext()).thenReturn(new PatientContext(1L, "John", "Doe", "M", "1990-01-01"));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(dummyPatient));

        // Act
        ResponseEntity<Record> response = controller.createMedicalHistory(patientId, request);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        verify(recordRepository, never()).save(any(Record.class));
    }

    @Test
    void typeExceedsMaxLength_ShouldReturn400() {
        // Arrange
        Long patientId = 1L;
        String longType = "A".repeat(101); // 101 chars exceeds max 100
        MedicalHistoryRequest request = new MedicalHistoryRequest(longType, "Content");
        when(patientContextService.getCurrentPatientContext()).thenReturn(new PatientContext(1L, "John", "Doe", "M", "1990-01-01"));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(dummyPatient));

        // Act
        ResponseEntity<Record> response = controller.createMedicalHistory(patientId, request);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        verify(recordRepository, never()).save(any(Record.class));
    }

    @Test
    void nullContent_ShouldReturn400() {
        // Arrange
        Long patientId = 1L;
        MedicalHistoryRequest request = new MedicalHistoryRequest("Type", null);
        when(patientContextService.getCurrentPatientContext()).thenReturn(new PatientContext(1L, "John", "Doe", "M", "1990-01-01"));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(dummyPatient));

        // Act
        ResponseEntity<Record> response = controller.createMedicalHistory(patientId, request);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        verify(recordRepository, never()).save(any(Record.class));
    }

    @Test
    void blankContent_ShouldReturn400() {
        // Arrange
        Long patientId = 1L;
        MedicalHistoryRequest request = new MedicalHistoryRequest("Type", "   ");
        when(patientContextService.getCurrentPatientContext()).thenReturn(new PatientContext(1L, "John", "Doe", "M", "1990-01-01"));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(dummyPatient));

        // Act
        ResponseEntity<Record> response = controller.createMedicalHistory(patientId, request);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        verify(recordRepository, never()).save(any(Record.class));
    }

    @Test
    void contentExceedsMaxLength_ShouldReturn400() {
        // Arrange
        Long patientId = 1L;
        String longContent = "A".repeat(2001); // exceeds 2000 limit
        MedicalHistoryRequest request = new MedicalHistoryRequest("Type", longContent);
        when(patientContextService.getCurrentPatientContext()).thenReturn(new PatientContext(1L, "John", "Doe", "M", "1990-01-01"));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(dummyPatient));

        // Act
        ResponseEntity<Record> response = controller.createMedicalHistory(patientId, request);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        verify(recordRepository, never()).save(any(Record.class));
    }

    @Test
    void successfulPersistence_ShouldSetCreatedAt_AndPatientRelationship() {
        // Arrange
        Long patientId = 1L;
        MedicalHistoryRequest request = new MedicalHistoryRequest("Follow-up", "Improved.");
        Record savedRecord = new Record();
        savedRecord.setId(100L);
        savedRecord.setPatient(dummyPatient);
        savedRecord.setType(request.getType());
        savedRecord.setContent(request.getContent());
        savedRecord.setCreatedAt(LocalDateTime.now());

        when(patientContextService.getCurrentPatientContext()).thenReturn(new PatientContext(1L, "John", "Doe", "M", "1990-01-01"));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(dummyPatient));
        when(recordRepository.save(any(Record.class))).thenReturn(savedRecord);

        // Act
        ResponseEntity<Record> response = controller.createMedicalHistory(patientId, request);

        // Assert
        assertEquals(201, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(request.getType(), response.getBody().getType());
        assertEquals(request.getContent(), response.getBody().getContent());
        assertNotNull(response.getBody().getCreatedAt());
        assertEquals(dummyPatient, response.getBody().getPatient());
        verify(recordRepository).save(any(Record.class));
    }
}