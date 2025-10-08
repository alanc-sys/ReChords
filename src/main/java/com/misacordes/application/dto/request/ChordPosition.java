package com.misacordes.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChordPosition {
    
    private String chordName;     // "C", "Am", "F", etc.
    private Integer startPos;     // Posición inicial en el texto
    private Integer endPos;       // Posición final en el texto
    private Integer lineNumber;   // Línea donde está el acorde (0-based)
}
