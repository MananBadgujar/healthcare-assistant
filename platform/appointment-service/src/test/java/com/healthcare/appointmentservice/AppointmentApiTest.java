package com.healthcare.appointmentservice;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import com.healthcare.appointmentservice.common.ServiceClients;
import java.util.Date;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AppointmentApiTest {
    @Autowired MockMvc mvc;
    @MockBean ServiceClients clients;
    @Value("${jwt.secret}") String secret;

    String token(String user, String... roles) {
        var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.builder().setSubject(user).claim("roles", List.of(roles))
                .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + 600000))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    @Test
    public void bookAndTransition() throws Exception {
        when(clients.get(anyString(), anyString(), any())).thenReturn(ResponseEntity.ok("{}"));
        String staff = token("s", "STAFF");
        String resp = mvc.perform(post("/api/v1/appointments").header("Authorization", "Bearer " + staff)
                .header("Idempotency-Key", "key-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"patientId\":1,\"providerId\":2,\"startTime\":\"2026-09-20T10:00\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = resp.split("\"id\":")[1].split("[,}]")[0].trim();
        // duplicate idempotency key returns same record
        mvc.perform(post("/api/v1/appointments").header("Authorization", "Bearer " + staff)
                .header("Idempotency-Key", "key-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"patientId\":1,\"providerId\":2,\"startTime\":\"2026-09-20T10:00\"}"))
                .andExpect(status().isCreated());
        mvc.perform(patch("/api/v1/appointments/" + id + "/status").header("Authorization", "Bearer " + staff)
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CONFIRMED\"}"))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/v1/appointments/" + id + "/status").header("Authorization", "Bearer " + staff)
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PENDING\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/appointments/" + id)).andExpect(status().isUnauthorized());
    }
}
