package com.healthcare.assistant.security.Keys;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.SignatureAlgorithm;

public class JwtKeyUtil {

    // Base64 में encode किया गया आपका secret key (यह एक उदाहरण है, अपने प्रोडक्शन के लिए अलग और सुरक्षित key इस्तेमाल करें)
    private static final String BASE64_SECRET_KEY = "q8D3f5+9KGJqJx3mqzSeHru7a+ZL5KWgxLkqdFYM0wI="; // example base64 key

    public static Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(BASE64_SECRET_KEY);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    public static void main(String[] args) {
        byte[] key = new byte[32];  // 256 bit = 32 bytes
        new SecureRandom().nextBytes(key);
        String base64Key = Base64.getEncoder().encodeToString(key);
        System.out.println("Base64 Secret Key: " + base64Key);
    }
}
