package com.healthcare.assistant.security;

import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;

 /**
   * Once-per-request JWT authentication filter.
   * <p>
   * Validates the Bearer token from the {@code Authorization} header and, when
   * valid, loads the user's authorities from {@link UserDetailsService} so the
   * authenticated principal carries the role(s) matching the persisted user (for
   * example {@code ROLE_ADMIN} or {@code ROLE_PROVIDER}). This lets
   * {@code @PreAuthorize} role checks succeed for JWT-based requests instead of
   * always receiving {@code ROLE_USER}.
   */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil, UserDetailsService userDetailsService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Public endpoints bypass JWT validation.
        String uri = request.getRequestURI();
        if (uri.equals("/api/v1/auth/register") ||
            uri.equals("/api/v1/auth/login") ||
            uri.equals("/api/v1/auth/refresh") ||
            uri.equals("/api/v1/auth/logout")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwtToken = authHeader.substring(7);
try {
             if (!jwtTokenUtil.validateToken(jwtToken)) {
                 // Token is malformed/invalid signature/etc.
                 SecurityContextHolder.clearContext();
                 response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                 return;
             }
             // Token is valid; proceed to extract username and load user details
             String username = jwtTokenUtil.getUsernameFromToken(jwtToken);
             UserDetails userDetails = loadUserDetailsSafely(username);
             if (userDetails == null) {
                 userDetails = new org.springframework.security.core.userdetails.User(
                         username, "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
             }
             UsernamePasswordAuthenticationToken authentication =
                     new UsernamePasswordAuthenticationToken(
                             userDetails.getUsername(), null,
                             userDetails.getAuthorities() != null ? userDetails.getAuthorities()
                                         : Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
             authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
             // DEBUG: authentication details
             System.out.println("DEBUG: authentication name=" + authentication.getName()
                     + " authenticated=" + authentication.isAuthenticated()
                     + " authorities=" + authentication.getAuthorities());
             SecurityContextHolder.getContext().setAuthentication(authentication);
             // DEBUG: SecurityContext authorities
             System.out.println("DEBUG: SecurityContext authorities="
                     + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
} catch (JwtException | IllegalArgumentException ex) {
                  // Invalid token - clear any stale authentication and respond with 401.
                  SecurityContextHolder.clearContext();
                  response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                  return;
              } catch (Exception ex) {
                  // Invalid token - clear any stale authentication and respond with 401.
                  SecurityContextHolder.clearContext();
                  response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                  return;
              } catch (Throwable ex) {
                  // Catch any other error (including Errors) and still return 401.
                  SecurityContextHolder.clearContext();
response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    return;
              }
        }
        filterChain.doFilter(request, response);
    }

    private UserDetails loadUserDetailsSafely(String username) {
        return userDetailsService.loadUserByUsername(username);
    }

    /**
       * Fallback constructor kept for backward compatibility with tests that wire
       * the filter without a UserDetailsService; in that case authorities are
       * restricted to {@code ROLE_USER} (matching previous behaviour).
       */
    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = username -> new org.springframework.security.core.userdetails.User(
                username, "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }
}