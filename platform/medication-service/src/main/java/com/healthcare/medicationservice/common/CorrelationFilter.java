package com.healthcare.medicationservice.common;

import com.healthcare.contracts.Correlation;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
public class CorrelationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String cid = Correlation.ensure(req.getHeader(Correlation.HEADER));
        MDC.put("correlationId", cid);
        MDC.put("service", "medication-service");
        res.setHeader(Correlation.HEADER, cid);
        req.setAttribute(Correlation.HEADER, cid);
        try {
            chain.doFilter(req, res);
        } finally {
            MDC.clear();
        }
    }
}
