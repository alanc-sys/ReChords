package com.misacordes.application.dto.response;

import com.misacordes.application.dto.request.ChordPosition;
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
public class SongResponse {
    private Long id;
    private String title;
    private String artist;
    private String album;
    private Integer year;
    private String  lyrics;
    private SongStatus status;
    private Boolean isPublic;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;

    // Info del creador (solo nombre, no datos sensibles)
    private CreatorInfo createdBy;
    
    // 🆕 Posiciones de acordes en la letra
    private List<ChordPosition> chordPositions;

    @Data
    @Builder
    public static class CreatorInfo {
        private Long id;
        private String username;
        private String firstname;
    }
}
