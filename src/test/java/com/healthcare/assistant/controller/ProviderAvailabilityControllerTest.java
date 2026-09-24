package com.healthcare.assistant.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.assistant.dto.AvailabilityRequest;
import com.healthcare.assistant.entity.Provider;
import com.healthcare.assistant.service.ProviderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;
import java.util.*;

import java.util.List;

@WebMvcTest(ProviderAvailabilityController.class)
@WithMockUser
class ProviderAvailabilityControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private ProviderService providerService;

	@Test
	public void setAvailability_returnsNoContent() throws Exception {
		// Arrange: a provider with id 1 exists
		Provider mockProvider = new Provider();
		mockProvider.setId(1L);
		when(providerService.getProviderById(1L)).thenReturn(Optional.of(mockProvider));

		mockMvc.perform(post("/api/v1/providers/{id}/availability", 1L)
				.with(SecurityMockMvcRequestPostProcessors.csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AvailabilityRequest(true))))
				.andExpect(status().isNoContent());

		verify(providerService).updateAvailability(eq(1L), eq(true));
	}

	@Test
	public void getAvailability_returnsOk() throws Exception {
		when(providerService.isAvailable(1L)).thenReturn(true);

		mockMvc.perform(get("/api/v1/providers/{id}/availability", 1L)).andExpect(status().isOk())
				.andExpect(content().string("true"));

		verify(providerService).isAvailable(eq(1L));
	}

	@Test
	public void setAvailability_invalidProvider_returnsNotFound() throws Exception {
		mockMvc.perform(post("/api/v1/providers/{id}/availability", 999L)
				.with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AvailabilityRequest(true))))
				.andExpect(status().isNotFound());

		verify(providerService, never()).updateAvailability(eq(999L), anyBoolean());
	}

	@Test
	public void setAvailability_invalidJson_returnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/v1/providers/{id}/availability", 1L)
				.with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
	}

	@Test
	public void getAvailability_returnsFalseWhenNotAvailable() throws Exception {
		when(providerService.isAvailable(1L)).thenReturn(false);

		mockMvc.perform(get("/api/v1/providers/{id}/availability", 1L)).andExpect(status().isOk())
				.andExpect(content().string("false"));

		verify(providerService).isAvailable(eq(1L));
	}
}