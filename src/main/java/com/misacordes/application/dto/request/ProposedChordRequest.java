package com.misacordes.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProposedChordRequest {
    private String name;              // Nombre del acorde (ej: "Cmaj9")
    private String fullName;          // Nombre completo (ej: "Do mayor novena")
    private String category;          // Categoría (MAJOR, MINOR, SEVENTH, etc.)
    private String fingerPositions;   // Posiciones de dedos (ej: "x32010") - OPCIONAL
    private String notes;             // Notas adicionales - OPCIONAL
}

