package com.misacordes.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChordInfo {
    private Integer start;        // Posición inicial en la línea (0-based)
    private String name;         // Nombre del acorde (C, Am, F, etc.)
    private Long chordId;        // ID del acorde en el catálogo (opcional)
}
