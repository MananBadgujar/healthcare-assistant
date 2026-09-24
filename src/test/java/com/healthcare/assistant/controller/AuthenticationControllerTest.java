package com.healthcare.assistant.controller;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.healthcare.assistant.dto.ChangePasswordRequest;
import com.healthcare.assistant.dto.LoginRequest;
import com.healthcare.assistant.dto.LoginResponse;
import com.healthcare.assistant.dto.RegisterRequest;
import com.healthcare.assistant.entity.User;
import com.healthcare.assistant.security.JwtTokenStore;
import com.healthcare.assistant.security.JwtTokenUtil;
import com.healthcare.assistant.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

@WithMockUser(roles = "USER")
@WebMvcTest(AuthenticationController.class)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @MockBean
    private JwtTokenStore tokenStore;

    @Test
    public void register_returnsOk() throws Exception {
        when(userService.registerNewUser(any(RegisterRequest.class))).thenReturn(new User());
        when(jwtTokenUtil.generateToken(anyString())).thenReturn("dummyAccess");
        when(jwtTokenUtil.generateRefreshToken(anyString())).thenReturn("dummyRefresh");
        mockMvc.perform(post("/api/v1/auth/register").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"pwd\",\"name\":\"Test User\"}"))
                .andExpect(status().isOk());
    }

@Test
     public void login_returnsOk() throws Exception {
User user = new User();
          user.setEmail("test@example.com");
          user.setPassword("pwd");
          when(userService.getUserByEmail(anyString())).thenReturn(user);
         when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
         when(jwtTokenUtil.generateToken(anyString())).thenReturn("accessToken");
         when(jwtTokenUtil.generateRefreshToken(anyString())).thenReturn("refreshToken");
         when(tokenStore.containsToken("refreshToken")).thenReturn(true);
         mockMvc.perform(post("/api/v1/auth/login").with(csrf())
                 .contentType(MediaType.APPLICATION_JSON)
                 .content("{\"email\":\"test@example.com\",\"password\":\"pwd\"}"))
                 .andExpect(status().isOk())
                 .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
     }

    @Test
    public void refresh_returnsOk() throws Exception {
        when(tokenStore.containsToken(anyString())).thenReturn(true);
        when(jwtTokenUtil.generateToken(anyString())).thenReturn("newAccess");
        mockMvc.perform(post("/api/v1/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"dummyRefresh\"}"))
                .andExpect(status().isOk());
    }

@Test
    public void logout_returnsOk() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    public void changePassword_returnsOk() throws Exception {
        when(userService.changePassword(eq("test@example.com"), anyString(), anyString())).thenReturn(true);
        mockMvc.perform(post("/api/v1/auth/change-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"oldPassword\":\"pwd\",\"newPassword\":\"newPwd\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Password changed successfully"));
    }
}
