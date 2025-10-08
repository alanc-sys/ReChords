package com.misacordes.application.dto.response;

import com.misacordes.application.entities.DifficultyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChordInfo {
    
    private Long id;
    private String name;              // "C", "Am"
    private String fullName;          // "Do mayor", "La menor"
    private String fingerPositions;   // Posiciones de dedos
    private DifficultyLevel difficulty;
    private Boolean isCommon;         // Si es un acorde común
    private Integer displayOrder;     // Orden de visualización
}
