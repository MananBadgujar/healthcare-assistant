package com.healthcare.assistant.security;

import com.healthcare.assistant.entity.User;
import com.healthcare.assistant.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository repository;

    public CustomUserDetailsService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> userOptional = repository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    getSimpleRoles(user.getRole()));
        }
        throw new UsernameNotFoundException("User not found: " + email);
    }

    private Collection<? extends SimpleGrantedAuthority> getSimpleRoles(String role) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.trim().toUpperCase()));
    }
}