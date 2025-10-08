package com.misacordes.application.entities;

import com.misacordes.application.utils.SongStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "songs")
public class Song {

        @Id
        @GeneratedValue
        private long id;
        @Column(nullable = false)
        private String title;
        private String artist;
        private String album;
        private Integer year;

        @ManyToOne
        @JoinColumn(name = "created_by", nullable = false)
        private User createdBy;

        @Lob
        @Column(name="lyrics_data", columnDefinition = "TEXT")
        private String lyricsData;

        @Lob
        @Column(name="chords_map", columnDefinition = "TEXT")
        private String chordsMap; // JSON con letra y posiciones de acordes

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private SongStatus status;

        @Column(name = "is_public", nullable = false)
        private Boolean isPublic;

        @Column(name = "rejection_reason", length = 500)
        private String rejectionReason;

        @Column(name = "updated_at")
        private LocalDateTime updatedAt;

        @Column(name = "published_at")
        private LocalDateTime publishedAt;

        @Column(name="created_at")
        private LocalDateTime createdAt;

        protected void onCreate() {
                createdAt = LocalDateTime.now();
                updatedAt = LocalDateTime.now();
                if (status == null) {
                        status = SongStatus.DRAFT;
                }
                if (isPublic == null) {
                        isPublic = false;
                }
        }
        @PreUpdate
        protected void onUpdate() {
                updatedAt = LocalDateTime.now();
        }
}
