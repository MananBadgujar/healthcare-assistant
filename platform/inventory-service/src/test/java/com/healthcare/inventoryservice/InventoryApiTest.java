package com.healthcare.inventoryservice;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class InventoryApiTest {
    @Autowired MockMvc mvc;
    @Value("${jwt.secret}") String secret;

    String token(String user, String... roles) {
        var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.builder().setSubject(user).claim("roles", List.of(roles))
                .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + 600000))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    @Test
    public void stockAndAlerts() throws Exception {
        String staff = token("s", "STAFF");
        String resp = mvc.perform(post("/api/v1/inventory/items").header("Authorization", "Bearer " + staff)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sku\":\"AMOX-500\",\"name\":\"Amoxicillin\",\"quantity\":5,\"reorderThreshold\":10}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = resp.split("\"id\":")[1].split("[,}]")[0].trim();
        mvc.perform(get("/api/v1/inventory/reorder-analysis").header("Authorization", "Bearer " + staff))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].recommendation").value("REORDER"));
        mvc.perform(post("/api/v1/inventory/items/" + id + "/stock-out").header("Authorization", "Bearer " + staff)
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":99}"))
                .andExpect(status().isBadRequest());
    }
}
