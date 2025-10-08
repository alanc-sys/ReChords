package com.misacordes.application.dto.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChordPositionTest {

    @Test
    void testChordPosition_AllArgsConstructor() {
        // Arrange & Act
        ChordPosition chordPosition = new ChordPosition("C", 0, 1, 0);

        // Assert
        assertEquals("C", chordPosition.getChordName());
        assertEquals(0, chordPosition.getStartPos());
        assertEquals(1, chordPosition.getEndPos());
        assertEquals(0, chordPosition.getLineNumber());
    }

    @Test
    void testChordPosition_NoArgsConstructor() {
        // Arrange & Act
        ChordPosition chordPosition = new ChordPosition();

        // Assert
        assertNull(chordPosition.getChordName());
        assertNull(chordPosition.getStartPos());
        assertNull(chordPosition.getEndPos());
        assertNull(chordPosition.getLineNumber());
    }

    @Test
    void testChordPosition_Setters() {
        // Arrange
        ChordPosition chordPosition = new ChordPosition();

        // Act
        chordPosition.setChordName("Am");
        chordPosition.setStartPos(5);
        chordPosition.setEndPos(7);
        chordPosition.setLineNumber(1);

        // Assert
        assertEquals("Am", chordPosition.getChordName());
        assertEquals(5, chordPosition.getStartPos());
        assertEquals(7, chordPosition.getEndPos());
        assertEquals(1, chordPosition.getLineNumber());
    }

    @Test
    void testChordPosition_Equals() {
        // Arrange
        ChordPosition chordPosition1 = new ChordPosition("C", 0, 1, 0);
        ChordPosition chordPosition2 = new ChordPosition("C", 0, 1, 0);
        ChordPosition chordPosition3 = new ChordPosition("Am", 0, 1, 0);

        // Act & Assert
        assertEquals(chordPosition1, chordPosition2);
        assertNotEquals(chordPosition1, chordPosition3);
    }

    @Test
    void testChordPosition_HashCode() {
        // Arrange
        ChordPosition chordPosition1 = new ChordPosition("C", 0, 1, 0);
        ChordPosition chordPosition2 = new ChordPosition("C", 0, 1, 0);

        // Act & Assert
        assertEquals(chordPosition1.hashCode(), chordPosition2.hashCode());
    }

    @Test
    void testChordPosition_ToString() {
        // Arrange
        ChordPosition chordPosition = new ChordPosition("C", 0, 1, 0);

        // Act
        String toString = chordPosition.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("C"));
        assertTrue(toString.contains("0"));
        assertTrue(toString.contains("1"));
    }
}
