package com.misacordes.application.controller.auth;

import com.misacordes.application.services.auth.AuthService;
import com.misacordes.application.services.RateLimitService;
import com.misacordes.application.dto.request.LoginRequest;
import com.misacordes.application.dto.request.RegisterRequest;
import com.misacordes.application.dto.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RateLimitService rateLimitService;

    @PostMapping(value = "login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, 
                                    HttpServletRequest httpRequest) {
        String ipAddress = getClientIP(httpRequest);
        
        // Verificar rate limiting
        if (!rateLimitService.isLoginAllowed(ipAddress)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Too Many Requests");
            error.put("message", "Demasiados intentos de login. Por favor espera un momento.");
            error.put("remainingAttempts", 0);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
        }
        
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request,
                                       HttpServletRequest httpRequest) {
        String ipAddress = getClientIP(httpRequest);
        
        // Verificar rate limiting
        if (!rateLimitService.isRegistrationAllowed(ipAddress)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Too Many Requests");
            error.put("message", "Demasiados intentos de registro. Por favor espera una hora.");
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
        }
        
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Obtiene la IP real del cliente, considerando proxies y load balancers
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

}
