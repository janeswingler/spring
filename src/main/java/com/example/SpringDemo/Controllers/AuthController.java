package com.example.SpringDemo.Controllers;

import com.example.SpringDemo.Models.User;
import com.example.SpringDemo.Repositories.UserRepository;
import com.example.SpringDemo.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager, UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String rawPassword = request.get("password");
        String firstName = request.get("firstName");
        String lastName = request.get("lastName");
        String languagesToLearn = request.get("languagesToLearn");
        String proficiencyLevel = request.get("proficiencyLevel");

        if (userRepository.findByUsername(username) != null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Username already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);

        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setLanguagesToLearn(languagesToLearn);
        user.setProficiencyLevel(proficiencyLevel);
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully");

        return ResponseEntity.ok(response);
    }


//    @PostMapping("/register")
//    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> request) {
//        String username = request.get("username");
//        String rawPassword = request.get("password");
//        if ( userRepository.findByUsername(username) != null) {
//            Map<String, String> response = new HashMap<>();
//            response.put("message", "Username already exists");
//            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
//        }
//
//        String encodedPassword = passwordEncoder.encode(rawPassword);
//
//        User user = new User();
//        user.setUsername(username);
//        user.setPassword(encodedPassword);
//        userRepository.save(user);
//
//        Map<String, String> response = new HashMap<>();
//        response.put("message", "User registered successfully");
//
//        return ResponseEntity.ok(response);
//    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwtToken = JwtUtil.generateToken(userDetails);
            User user = userRepository.findByUsername(username);

            Map<String, String> response = new HashMap<>();
            response.put("message", "User logged in successfully");
            response.put("token", jwtToken);
            response.put("userId", user.getId().toString());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        // For JWT, logout can be handled client-side by deleting the token
        Map<String, String> response = new HashMap<>();
        response.put("message", "User logged out successfully");
        return ResponseEntity.ok(response);
    }
}
