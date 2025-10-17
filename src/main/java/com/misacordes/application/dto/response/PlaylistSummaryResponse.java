package com.misacordes.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistSummaryResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean isDefault;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long songCount;
    
    // Info del creador (solo para playlists públicas)
    private CreatorInfo createdBy;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatorInfo {
        private Long id;
        private String username;
        private String firstname;
    }
}
