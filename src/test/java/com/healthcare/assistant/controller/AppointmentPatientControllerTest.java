package com.healthcare.assistant.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Collections;



import org.springframework.beans.factory.annotation.Autowired;

@WebMvcTest(AppointmentPatientController.class)
@WithMockUser
class AppointmentPatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getAppointmentsByPatient_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/appointments/patient/1"))
               .andExpect(status().isOk())
               .andExpect(content().contentType("application/json"))
               .andExpect(content().string("[]"));
    }
}