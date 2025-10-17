package com.misacordes.application.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequest {
    
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "El nombre de usuario solo puede contener letras, números, guiones y guiones bajos")
    String username;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener al menos 8 caracteres")
    String password;
    
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    String firstname;
    
    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    String lastname;
    
    @Size(max = 100, message = "El país no puede exceder 100 caracteres")
    String country;

}
