package com.misacordes.application.dto.response;

import com.misacordes.application.dto.request.LineWithChords;
import com.misacordes.application.utils.SongStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongWithChordsResponse {
    private Long id;
    private String title;
    private String artist;
    private String album;
    private Integer year;
    private String key;          // Tonalidad de la canción
    private Integer tempo;       // BPM de la canción
    private SongStatus status;
    private Boolean isPublic;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private List<LineWithChords> lyrics; // Líneas con texto y acordes

    // Info del creador
    private CreatorInfo createdBy;

    @Data
    @Builder
    public static class CreatorInfo {
        private Long id;
        private String username;
        private String firstname;
    }
}
