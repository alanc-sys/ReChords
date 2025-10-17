package com.misacordes.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineWithChords {
    private Integer lineNumber;   // Número de línea (0-based)
    private String text;         // Texto de la línea
    private List<ChordPositionInfo> chords; // Lista de acordes en esta línea
}
