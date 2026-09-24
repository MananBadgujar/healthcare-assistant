package com.healthcare.assistant.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.healthcare.assistant.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Lazy;
import com.healthcare.assistant.security.JwtAuthenticationEntryPoint;
import com.healthcare.assistant.security.JwtAccessDeniedHandler;
import com.healthcare.assistant.security.JwtTokenUtil;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @PostConstruct
    public void init() {
        System.out.println("[DEBUG] SecurityConfig initialized");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Lazy
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("[DEBUG] SecurityFilterChain bean created");
        http
            .csrf().disable()
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint())
                .accessDeniedHandler(jwtAccessDeniedHandler()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/api/v1/triage/**").permitAll()
.anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        System.out.println("[DEBUG] Creating JwtAuthenticationEntryPoint bean");
        return new JwtAuthenticationEntryPoint();
    }

    @Bean
    public JwtAccessDeniedHandler jwtAccessDeniedHandler() {
        return new JwtAccessDeniedHandler();
    }

    @Lazy @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil,
                                                           UserDetailsService userDetailsService) {
        System.out.println("[DEBUG] Creating JwtAuthenticationFilter bean");
        return new JwtAuthenticationFilter(jwtTokenUtil, userDetailsService);
    }
}