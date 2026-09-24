package com.healthcare.assistant.controller;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InventoryStockLevelsController.class)
@WithMockUser
class InventoryStockLevelsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getStockLevels_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/stock-levels"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }
}
