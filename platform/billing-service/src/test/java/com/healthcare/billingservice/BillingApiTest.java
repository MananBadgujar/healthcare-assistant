package com.healthcare.billingservice;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Date;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BillingApiTest {
    @Autowired MockMvc mvc;
    @Value("${jwt.secret}") String secret;

    String token(String user, String... roles) {
        var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.builder().setSubject(user).claim("roles", List.of(roles))
                .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + 600000))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    @Test
    public void invoiceLifecycle() throws Exception {
        String staff = token("staff", "STAFF");
        String resp = mvc.perform(post("/api/v1/billing/invoices").header("Authorization", "Bearer " + staff)
                .contentType(MediaType.APPLICATION_JSON).content("{\"patientId\":1,\"amount\":250.0}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = resp.split("\"id\":")[1].split("[,}]")[0].trim();
        mvc.perform(post("/api/v1/billing/invoices/" + id + "/pay").header("Authorization", "Bearer " + staff)
                .contentType(MediaType.APPLICATION_JSON).content("{\"outcome\":\"ok\"}"))
                .andExpect(status().isOk());
        // terminal state: further pay must fail
        mvc.perform(post("/api/v1/billing/invoices/" + id + "/pay").header("Authorization", "Bearer " + staff)
                .contentType(MediaType.APPLICATION_JSON).content("{\"outcome\":\"ok\"}"))
                .andExpect(status().isBadRequest());
        String patient = token("p", "PATIENT");
        mvc.perform(post("/api/v1/billing/invoices").header("Authorization", "Bearer " + patient)
                .contentType(MediaType.APPLICATION_JSON).content("{\"patientId\":1,\"amount\":10.0}"))
                .andExpect(status().isForbidden());
    }
}
