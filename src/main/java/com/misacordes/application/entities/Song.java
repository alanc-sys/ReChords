package com.misacordes.application.entities;

import com.misacordes.application.utils.SongStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
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
        
        @Column(name = "song_key", length = 10)
        private String key;  // Tonalidad: C, Am, etc.
        
        @Column
        private Integer tempo;  // BPM
        
        @Column(name = "youtube_url", length = 500)
        private String youtubeUrl;
        
        @Column(name = "spotify_url", length = 500)
        private String spotifyUrl;
        
        @Column(name = "cover_image_url", length = 500)
        private String coverImageUrl;  // URL de imagen subida
        
        @Column(name = "cover_color", length = 7)
        private String coverColor;  // Color hex para portada generada (#FF5733)

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

        @PrePersist
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
        

        public String getYoutubeVideoId() {
                if (youtubeUrl == null || youtubeUrl.isEmpty()) {
                        return null;
                }
                
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                        "(?:youtube\\.com/watch\\?v=|youtu\\.be/|youtube\\.com/embed/)([a-zA-Z0-9_-]{11})"
                );
                java.util.regex.Matcher matcher = pattern.matcher(youtubeUrl);
                
                return matcher.find() ? matcher.group(1) : null;
        }

        public String getSpotifyTrackId() {
                if (spotifyUrl == null || spotifyUrl.isEmpty()) {
                        return null;
                }
                
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                        "spotify\\.com/track/([a-zA-Z0-9]{22})"
                );
                java.util.regex.Matcher matcher = pattern.matcher(spotifyUrl);
                
                return matcher.find() ? matcher.group(1) : null;
        }
}
