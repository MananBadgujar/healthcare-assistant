package com.healthcare.assistant.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.assistant.entity.CarePlan;
import com.healthcare.assistant.service.CarePlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import java.util.*;


@WebMvcTest(CarePlanController.class)
@WithMockUser
class CarePlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CarePlanService carePlanService;

    @Test
    public void generateCarePlan_returnsOk() throws Exception {
        Long patientId = 1L;
        CarePlan mockPlan = new CarePlan();
        when(carePlanService.generateCarePlan(patientId)).thenReturn(mockPlan);

        mockMvc.perform(post("/api/v1/careplan/generate")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientId)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void getCarePlan_returnsOk() throws Exception {
        Long patientId = 1L;
        CarePlan mockPlan = new CarePlan();
        when(carePlanService.generateCarePlan(patientId)).thenReturn(mockPlan);

        mockMvc.perform(get("/api/v1/careplan/patient/{patientId}", patientId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void updateCarePlan_returnsOk() throws Exception {
        mockMvc.perform(patch("/api/v1/careplan/{id}", 1L)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("newContent"))
                .andExpect(status().isOk());
    }
}