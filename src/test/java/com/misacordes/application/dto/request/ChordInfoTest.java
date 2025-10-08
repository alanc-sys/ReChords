package com.misacordes.application.dto.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChordInfoTest {

    @Test
    void testChordInfo_AllArgsConstructor() {
        // Act
        ChordInfo chord = new ChordInfo(0, "C", 1L);

        // Assert
        assertEquals(0, chord.getStart());
        assertEquals("C", chord.getName());
        assertEquals(1L, chord.getChordId());
    }

    @Test
    void testChordInfo_NoArgsConstructor() {
        // Act
        ChordInfo chord = new ChordInfo();

        // Assert
        assertNull(chord.getStart());
        assertNull(chord.getName());
        assertNull(chord.getChordId());
    }

    @Test
    void testChordInfo_Setters() {
        // Arrange
        ChordInfo chord = new ChordInfo();

        // Act
        chord.setStart(5);
        chord.setName("Am");
        chord.setChordId(3L);

        // Assert
        assertEquals(5, chord.getStart());
        assertEquals("Am", chord.getName());
        assertEquals(3L, chord.getChordId());
    }

    @Test
    void testChordInfo_WithNullChordId() {
        // Act
        ChordInfo chord = new ChordInfo(0, "C", null);

        // Assert
        assertEquals(0, chord.getStart());
        assertEquals("C", chord.getName());
        assertNull(chord.getChordId());
    }
}
