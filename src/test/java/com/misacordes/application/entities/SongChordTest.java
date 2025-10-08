package com.misacordes.application.entities;

import com.misacordes.application.utils.SongStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SongChordTest {

    @Test
    void testSongChord_AllArgsConstructor() {
        // Arrange
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .firstname("Test")
                .lastname("User")
                .role(Role.USER)
                .build();

        Song song = Song.builder()
                .id(1L)
                .title("Test Song")
                .artist("Test Artist")
                .createdBy(user)
                .status(SongStatus.DRAFT)
                .isPublic(false)
                .build();

        ChordCatalog chord = ChordCatalog.builder()
                .id(1L)
                .name("C")
                .fullName("Do mayor")
                .category(ChordCategory.MAJOR)
                .difficultyLevel(DifficultyLevel.BEGINNER)
                .isCommon(true)
                .displayOrder(1)
                .build();

        LocalDateTime createdAt = LocalDateTime.now();

        // Act
        SongChord songChord = new SongChord(
                1L, song, chord, 0, 1, 0, "C", createdAt
        );

        // Assert
        assertEquals(1L, songChord.getId());
        assertEquals(song, songChord.getSong());
        assertEquals(chord, songChord.getChord());
        assertEquals(0, songChord.getPositionStart());
        assertEquals(1, songChord.getPositionEnd());
        assertEquals(0, songChord.getLineNumber());
        assertEquals("C", songChord.getChordName());
        assertEquals(createdAt, songChord.getCreatedAt());
    }

    @Test
    void testSongChord_NoArgsConstructor() {
        // Arrange & Act
        SongChord songChord = new SongChord();

        // Assert
        assertNull(songChord.getId());
        assertNull(songChord.getSong());
        assertNull(songChord.getChord());
        assertNull(songChord.getPositionStart());
        assertNull(songChord.getPositionEnd());
        assertNull(songChord.getLineNumber());
        assertNull(songChord.getChordName());
        assertNull(songChord.getCreatedAt());
    }

    @Test
    void testSongChord_Builder() {
        // Arrange
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .firstname("Test")
                .lastname("User")
                .role(Role.USER)
                .build();

        Song song = Song.builder()
                .id(1L)
                .title("Test Song")
                .artist("Test Artist")
                .createdBy(user)
                .status(SongStatus.DRAFT)
                .isPublic(false)
                .build();

        ChordCatalog chord = ChordCatalog.builder()
                .id(1L)
                .name("Am")
                .fullName("La menor")
                .category(ChordCategory.MINOR)
                .difficultyLevel(DifficultyLevel.BEGINNER)
                .isCommon(true)
                .displayOrder(2)
                .build();

        // Act
        SongChord songChord = SongChord.builder()
                .id(1L)
                .song(song)
                .chord(chord)
                .positionStart(5)
                .positionEnd(7)
                .lineNumber(1)
                .chordName("Am")
                .build();

        // Assert
        assertEquals(1L, songChord.getId());
        assertEquals(song, songChord.getSong());
        assertEquals(chord, songChord.getChord());
        assertEquals(5, songChord.getPositionStart());
        assertEquals(7, songChord.getPositionEnd());
        assertEquals(1, songChord.getLineNumber());
        assertEquals("Am", songChord.getChordName());
    }

    @Test
    void testSongChord_Setters() {
        // Arrange
        SongChord songChord = new SongChord();

        User user = User.builder()
                .id(1L)
                .username("testuser")
                .firstname("Test")
                .lastname("User")
                .role(Role.USER)
                .build();

        Song song = Song.builder()
                .id(1L)
                .title("Test Song")
                .artist("Test Artist")
                .createdBy(user)
                .status(SongStatus.DRAFT)
                .isPublic(false)
                .build();

        ChordCatalog chord = ChordCatalog.builder()
                .id(1L)
                .name("F")
                .fullName("Fa mayor")
                .category(ChordCategory.MAJOR)
                .difficultyLevel(DifficultyLevel.INTERMEDIATE)
                .isCommon(false)
                .displayOrder(3)
                .build();

        LocalDateTime createdAt = LocalDateTime.now();

        // Act
        songChord.setId(2L);
        songChord.setSong(song);
        songChord.setChord(chord);
        songChord.setPositionStart(10);
        songChord.setPositionEnd(12);
        songChord.setLineNumber(2);
        songChord.setChordName("F");
        songChord.setCreatedAt(createdAt);

        // Assert
        assertEquals(2L, songChord.getId());
        assertEquals(song, songChord.getSong());
        assertEquals(chord, songChord.getChord());
        assertEquals(10, songChord.getPositionStart());
        assertEquals(12, songChord.getPositionEnd());
        assertEquals(2, songChord.getLineNumber());
        assertEquals("F", songChord.getChordName());
        assertEquals(createdAt, songChord.getCreatedAt());
    }

    @Test
    void testSongChord_Equals() {
        // Arrange
        SongChord songChord1 = SongChord.builder()
                .id(1L)
                .chordName("C")
                .positionStart(0)
                .positionEnd(1)
                .lineNumber(0)
                .build();

        SongChord songChord2 = SongChord.builder()
                .id(1L)
                .chordName("C")
                .positionStart(0)
                .positionEnd(1)
                .lineNumber(0)
                .build();

        SongChord songChord3 = SongChord.builder()
                .id(2L)
                .chordName("Am")
                .positionStart(5)
                .positionEnd(7)
                .lineNumber(1)
                .build();

        // Act & Assert
        assertEquals(songChord1, songChord2);
        assertNotEquals(songChord1, songChord3);
    }

    @Test
    void testSongChord_HashCode() {
        // Arrange
        SongChord songChord1 = SongChord.builder()
                .id(1L)
                .chordName("C")
                .positionStart(0)
                .positionEnd(1)
                .lineNumber(0)
                .build();

        SongChord songChord2 = SongChord.builder()
                .id(1L)
                .chordName("C")
                .positionStart(0)
                .positionEnd(1)
                .lineNumber(0)
                .build();

        // Act & Assert
        assertEquals(songChord1.hashCode(), songChord2.hashCode());
    }

    @Test
    void testSongChord_ToString() {
        // Arrange
        SongChord songChord = SongChord.builder()
                .id(1L)
                .chordName("C")
                .positionStart(0)
                .positionEnd(1)
                .lineNumber(0)
                .build();

        // Act
        String toString = songChord.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("C"));
        assertTrue(toString.contains("0"));
        assertTrue(toString.contains("1"));
    }
}
