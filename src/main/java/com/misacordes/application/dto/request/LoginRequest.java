package com.misacordes.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = "El nombre de usuario es obligatorio")
    String username;
    
    @NotBlank(message = "La contraseña es obligatoria")
    String password;
}
