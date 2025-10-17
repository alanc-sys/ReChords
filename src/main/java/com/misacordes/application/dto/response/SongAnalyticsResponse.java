package com.misacordes.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongAnalyticsResponse {
    private Long songId;
    private String title;
    private Integer totalChords;        // Total de acordes en la canción
    private Integer totalLines;         // Total de líneas con texto
    private Integer uniqueChords;       // Número de acordes únicos
    private String mostUsedChord;       // Acorde más usado
    private Integer mostUsedChordCount; // Cantidad del acorde más usado
    private List<ChordUsage> chordUsage; // Lista de acordes con su frecuencia
    private Map<String, Integer> chordFrequency; // Mapa de acorde -> frecuencia
    private Double averageChordsPerLine; // Promedio de acordes por línea
    private Integer maxChordsInLine;     // Máximo acordes en una línea
    private String key;                  // Tonalidad detectada
    private Integer tempo;               // BPM detectado

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChordUsage {
        private String chordName;
        private Integer count;
        private Double percentage; // Porcentaje del total
    }
}
