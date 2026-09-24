package com.healthcare.assistant.controller;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.healthcare.assistant.config.SecurityConfig;
import com.healthcare.assistant.service.ProviderService;
import com.healthcare.assistant.entity.Provider;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Collections;
import java.util.Arrays;