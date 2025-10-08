package com.misacordes.application.dto.request;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LineWithChordsTest {

    @Test
    void testLineWithChords_AllArgsConstructor() {
        // Arrange
        List<ChordInfo> chords = Arrays.asList(
                new ChordInfo(0, "C", 1L),
                new ChordInfo(7, "G", 2L)
        );

        // Act
        LineWithChords line = new LineWithChords(0, "Amazing grace how sweet the sound", chords);

        // Assert
        assertEquals(0, line.getLineNumber());
        assertEquals("Amazing grace how sweet the sound", line.getText());
        assertEquals(2, line.getChords().size());
        assertEquals("C", line.getChords().get(0).getName());
        assertEquals("G", line.getChords().get(1).getName());
    }

    @Test
    void testLineWithChords_NoArgsConstructor() {
        // Act
        LineWithChords line = new LineWithChords();

        // Assert
        assertNull(line.getLineNumber());
        assertNull(line.getText());
        assertNull(line.getChords());
    }

    @Test
    void testLineWithChords_Setters() {
        // Arrange
        LineWithChords line = new LineWithChords();
        List<ChordInfo> chords = Arrays.asList(
                new ChordInfo(0, "C", 1L)
        );

        // Act
        line.setLineNumber(0);
        line.setText("Test line");
        line.setChords(chords);

        // Assert
        assertEquals(0, line.getLineNumber());
        assertEquals("Test line", line.getText());
        assertEquals(1, line.getChords().size());
        assertEquals("C", line.getChords().get(0).getName());
    }
}
