package com.misacordes.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SongWithChordsRequest {
    private String title;
    private String artist;
    private String album;
    private Integer year;
    private String key;          // Tonalidad de la canción (C, Am, etc.)
    private Integer tempo;       // BPM de la canción
    private List<LineWithChords> lyrics; // Líneas con texto y acordes
}
