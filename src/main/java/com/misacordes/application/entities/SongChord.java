package com.misacordes.application.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "song_chords")
public class SongChord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chord_id", nullable = false)
    private ChordCatalog chord;

    @Column(name = "position_start", nullable = false)
    private Integer positionStart;  // Posición inicial en la letra

    @Column(name = "position_end", nullable = false)
    private Integer positionEnd;    // Posición final en la letra

    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;     // Número de línea (0-based)

    @Column(name = "chord_name", nullable = false, length = 20)
    private String chordName;       // Nombre del acorde (C, Am, F, etc.)

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }
}
