package com.misacordes.application.dto.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChordPositionInfoTest {

    @Test
    void testChordInfo_AllArgsConstructor() {
        // Act
        ChordPositionInfo chord = new ChordPositionInfo(0, "C", 1L);

        // Assert
        assertEquals(0, chord.getStart());
        assertEquals("C", chord.getName());
        assertEquals(1L, chord.getChordId());
    }

    @Test
    void testChordInfo_NoArgsConstructor() {
        // Act
        ChordPositionInfo chord = new ChordPositionInfo();

        // Assert
        assertNull(chord.getStart());
        assertNull(chord.getName());
        assertNull(chord.getChordId());
    }

    @Test
    void testChordInfo_Setters() {
        // Arrange
        ChordPositionInfo chord = new ChordPositionInfo();

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
        ChordPositionInfo chord = new ChordPositionInfo(0, "C", null);

        // Assert
        assertEquals(0, chord.getStart());
        assertEquals("C", chord.getName());
        assertNull(chord.getChordId());
    }
}
