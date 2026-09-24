package com.healthcare.assistant.service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.healthcare.assistant.dto.RegisterRequest;
import com.healthcare.assistant.entity.User;
import com.healthcare.assistant.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder; // अगर पासवर्ड एनकोड करना हो

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                             .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }



    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, Map<String, Object> updates) {
        User user = userRepository.findById(id).orElseThrow(NoSuchElementException::new);

        updates.forEach((key, value) -> {
            switch (key) {
                case "email":
                    user.setEmail((String) value);
                    break;
                case "name":
                    user.setName((String) value);
                    break;
                case "password":
                    user.setPassword(passwordEncoder.encode((String) value)); // ideally encode before saving
                    break;
                case "role":
                    user.setRole((String) value);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid field: " + key);
            }
        });

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }


    public User registerNewUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));  // Password encode यहीं करें
        user.setRole(request.getRole());

        return userRepository.save(user);
    }



    public boolean changePassword(String email, String oldPassword, String newPassword) {
        // email parameter pass karna zaruri hai method ko
        User user = userRepository.findByEmail(email)
                          .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false; // Old password mismatch
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }







}
