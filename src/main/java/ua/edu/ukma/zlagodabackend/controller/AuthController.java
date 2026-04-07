package ua.edu.ukma.zlagodabackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ua.edu.ukma.zlagodabackend.dto.login.LoginRequest;
import ua.edu.ukma.zlagodabackend.dto.login.LoginResponse;
import ua.edu.ukma.zlagodabackend.security.EmployeeDetails;
import ua.edu.ukma.zlagodabackend.security.JwtUtils;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@RequestBody LoginRequest loginRequest) {
        
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                loginRequest.idEmployee(), 
                loginRequest.password()
        );
        Authentication authentication = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        EmployeeDetails userDetails = (EmployeeDetails) authentication.getPrincipal();
        
        String role = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList())
                .get(0);

        return ResponseEntity.ok(new LoginResponse(jwt, role, userDetails.getUsername()));
    }
}