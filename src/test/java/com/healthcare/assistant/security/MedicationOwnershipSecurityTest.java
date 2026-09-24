package com.healthcare.assistant.security;

import com.healthcare.assistant.dto.RegisterRequest;
import com.healthcare.assistant.entity.User;
import com.healthcare.assistant.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class MedicationOwnershipSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private com.healthcare.assistant.security.JwtTokenUtil jwtTokenUtil;

    private String createUserAndGetToken(String name, String email, String password, String role) {
        RegisterRequest request = new RegisterRequest();
        request.setName(name);
        request.setEmail(email);
        request.setPassword(password);
        request.setRole(role);
        User savedUser = userService.registerNewUser(request);
        return jwtTokenUtil.generateToken(savedUser.getEmail());
    }

    @Test
    void ownerCanAccessOwnUserData_returnsOk() throws Exception {
        // Create owner user and get token
        String ownerToken = createUserAndGetToken("Owner User", "owner@example.com", "ownerpass", "USER");
        // We need the owner's user ID to build the endpoint.
        // We can get it by fetching the user by email.
        User ownerUser = userService.getUserByEmail("owner@example.com");
        Long ownerUserId = ownerUser.getId();

        // Now, try to get the owner's user data with the owner's token -> should be 200
        mockMvc.perform(get("/api/v1/users/{userId}", ownerUserId)
                .header("Authorization", "Bearer " + ownerToken))
               .andExpect(status().isOk());
    }

    @Test
    void nonOwnerCannotAccessOtherUserData_returnsForbidden() throws Exception {
        // Create two users
        String user1Token = createUserAndGetToken("User One", "user1@example.com", "user1pass", "USER");
        String user2Token = createUserAndGetToken("User Two", "user2@example.com", "user2pass", "USER");

        // Get the user IDs
        User user1 = userService.getUserByEmail("user1@example.com");
        Long user1Id = user1.getId();

        User user2 = userService.getUserByEmail("user2@example.com");
        Long user2Id = user2.getId();

        // Try to get user1's data with user2's token -> should be 403 (forbidden)
        mockMvc.perform(get("/api/v1/users/{userId}", user1Id)
                .header("Authorization", "Bearer " + user2Token))
               .andExpect(status().isForbidden());

        // Try to get user2's data with user1's token -> should be 403 (forbidden)
        mockMvc.perform(get("/api/v1/users/{userId}", user2Id)
                .header("Authorization", "Bearer " + user1Token))
               .andExpect(status().isForbidden());

        // Each user can access their own data -> should be 200 (ok)
        mockMvc.perform(get("/api/v1/users/{userId}", user1Id)
                .header("Authorization", "Bearer " + user1Token))
               .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/users/{userId}", user2Id)
                .header("Authorization", "Bearer " + user2Token))
               .andExpect(status().isOk());
    }
}