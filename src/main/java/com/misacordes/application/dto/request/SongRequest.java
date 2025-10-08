package com.misacordes.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongRequest {
    private String title;
    private String artist;
    private String album;
    private Integer year;
    private String lyricsData;
    private List<ChordPosition> chords;  // 🆕 Posiciones de acordes
}
