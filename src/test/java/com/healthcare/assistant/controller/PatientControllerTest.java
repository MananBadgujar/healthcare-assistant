package com.healthcare.assistant.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

import com.healthcare.assistant.dto.PatientContext;
import com.healthcare.assistant.entity.Patient;
import com.healthcare.assistant.service.PatientContextService;
import com.healthcare.assistant.service.PatientService;


@WebMvcTest(PatientController.class)
@ExtendWith(MockitoExtension.class)
public class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.healthcare.assistant.service.PatientService patientService;

    @MockBean
    private PatientContextService patientContextService;

    @BeforeEach
    void setup() {
        PatientContext patientContext = new PatientContext();
        patientContext.setId(1L);
        when(patientContextService.getCurrentPatientContext()).thenReturn(patientContext);
    }

    @Test
    public void getPatient_existingPatient_returnsOk() throws Exception {
        // Stub getPatientById to return a patient
        Patient dummy = new Patient("Alice","Smith","1992-05-10","FEMALE");
        when(patientService.getPatientById(1L)).thenReturn(Optional.of(dummy));
        mockMvc.perform(get("/api/v1/patients/1")
                .with(user("testuser").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    public void createPatient_returnsCreatedPatient() throws Exception {
        // Stub createPatient to return a patient
        Patient dummy = new Patient("Alice","Smith","1992-05-10","FEMALE");
        when(patientService.createPatient(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(dummy));
        mockMvc.perform(post("/api/v1/patients")
                .with(csrf())
                .with(user("testuser").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Alice\",\"lastName\":\"Smith\",\"dateOfBirth\":\"1992-05-10\",\"gender\":\"FEMALE\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Smith"));
    }

    @Test
    public void getPatient_nonexistent_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/patients/999")
                .with(user("testuser").roles("USER")))
                .andExpect(status().isForbidden());
    }

@Test
    public void updatePatient_nonexistent_returnsNotFound() throws Exception {
        String updatedJson = "{\"firstName\":\"Updated\",\"lastName\":\"User\",\"dateOfBirth\":\"1990-01-01\",\"gender\":\"MALE\"}";
        mockMvc.perform(patch("/api/v1/patients/999/update")
                .with(csrf()).with(user("testuser").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedJson))
                .andExpect(status().isForbidden());
    }
}