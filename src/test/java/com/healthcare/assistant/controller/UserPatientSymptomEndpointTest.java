package com.healthcare.assistant.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.healthcare.assistant.dto.SymptomIntakeRequest;
import com.healthcare.assistant.dto.TriageResponse;
import com.healthcare.assistant.dto.PatientContext;
import com.healthcare.assistant.entity.Patient;
import com.healthcare.assistant.entity.User;
import com.healthcare.assistant.service.PatientContextService;
import com.healthcare.assistant.service.PatientService;
import com.healthcare.assistant.service.TriageService;
import com.healthcare.assistant.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Phase 1 endpoint coverage: remaining User, Patient and SymptomTriage APIs.
 */
@WebMvcTest({UserController.class, PatientController.class, SymptomTriageController.class})
@WithMockUser(username = "alice@example.com", roles = "USER")
class UserPatientSymptomEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private PatientService patientService;

    @MockBean
    private TriageService triageService;

    @MockBean
    private PatientContextService patientContextService;

    private User currentUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("alice@example.com");
        user.setName("Alice");
        return user;
    }

    @BeforeEach
    void setup() {
        PatientContext patientContext = new PatientContext();
        patientContext.setId(1L);
        when(patientContextService.getCurrentPatientContext()).thenReturn(patientContext);
    }

    @Test
    void getUserById_returnsOk() throws Exception {
        when(userService.getUserByEmail("alice@example.com")).thenReturn(currentUser());
        User user = currentUser();
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void getMyProfile_returnsOk() throws Exception {
        when(userService.getUserByEmail("alice@example.com")).thenReturn(currentUser());
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void updateUser_returnsOk() throws Exception {
        when(userService.getUserByEmail("alice@example.com")).thenReturn(currentUser());
        User updated = currentUser();
        updated.setName("Alice Updated");
        when(userService.updateUser(anyLong(), any())).thenReturn(updated);
        mockMvc.perform(patch("/api/v1/users/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Alice Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Updated"));
    }

    @Test
    void getAllUsers_returnsOk() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.singletonList(currentUser()));
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("alice@example.com"));
    }

    @Test
    void deleteUser_returnsNoContent() throws Exception {
        when(userService.getUserByEmail("alice@example.com")).thenReturn(currentUser());
        mockMvc.perform(delete("/api/v1/users/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void updatePatient_returnsNoContent() throws Exception {
        Patient patient = new Patient("Alice", "Smith", "1992-05-10", "FEMALE");
        when(patientService.updatePatient(anyLong(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(patient));
        mockMvc.perform(patch("/api/v1/patients/1/update").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Alice\",\"lastName\":\"Smith\",\"dateOfBirth\":\"1992-05-10\",\"gender\":\"FEMALE\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void symptomIntake_returnsOk() throws Exception {
        when(triageService.intakeSymptom(any(SymptomIntakeRequest.class))).thenReturn("intake-1");
        mockMvc.perform(post("/api/v1/symptoms/intake").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"symptoms\":[\"headache\"],\"duration\":\"2 days\",\"severity\":\"moderate\",\"age\":30}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("intake-1"));
    }

    @Test
    void runTriage_returnsOk() throws Exception {
        TriageResponse response = new TriageResponse();
        when(triageService.getTriageResult("intake-1")).thenReturn(response);
        mockMvc.perform(post("/api/v1/symptoms/triage/run").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"intakeId\":\"intake-1\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void getTriage_returnsOk() throws Exception {
        TriageResponse response = new TriageResponse();
        when(triageService.getTriageResult("intake-1")).thenReturn(response);
        mockMvc.perform(get("/api/v1/symptoms/triage/intake-1"))
                .andExpect(status().isOk());
    }

    @Test
    void submitTriageFeedback_returnsOk() throws Exception {
        mockMvc.perform(post("/api/v1/symptoms/triage/feedback").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"intakeId\":\"intake-1\",\"feedback\":\"helpful\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void getTriageResponseShape() throws Exception {
        TriageResponse response = new TriageResponse();
        response.setSeverity("moderate");
        when(triageService.getTriageResult("intake-9")).thenReturn(response);
        mockMvc.perform(get("/api/v1/symptoms/triage/intake-9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.severity").value("moderate"));
    }

    @Test
    void listResponseShapeForUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
