package com.misacordes.application.dto.response;

import com.misacordes.application.entities.DifficultyLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChordInfoTest {

    @Test
    void testChordInfo_AllArgsConstructor() {
        // Arrange & Act
        ChordInfo chordInfo = new ChordInfo(
                1L, "C", "Do mayor", "x32010", 
                DifficultyLevel.BEGINNER, true, 1
        );

        // Assert
        assertEquals(1L, chordInfo.getId());
        assertEquals("C", chordInfo.getName());
        assertEquals("Do mayor", chordInfo.getFullName());
        assertEquals("x32010", chordInfo.getFingerPositions());
        assertEquals(DifficultyLevel.BEGINNER, chordInfo.getDifficulty());
        assertTrue(chordInfo.getIsCommon());
        assertEquals(1, chordInfo.getDisplayOrder());
    }

    @Test
    void testChordInfo_NoArgsConstructor() {
        // Arrange & Act
        ChordInfo chordInfo = new ChordInfo();

        // Assert
        assertNull(chordInfo.getId());
        assertNull(chordInfo.getName());
        assertNull(chordInfo.getFullName());
        assertNull(chordInfo.getFingerPositions());
        assertNull(chordInfo.getDifficulty());
        assertNull(chordInfo.getIsCommon());
        assertNull(chordInfo.getDisplayOrder());
    }

    @Test
    void testChordInfo_Builder() {
        // Arrange & Act
        ChordInfo chordInfo = ChordInfo.builder()
                .id(1L)
                .name("Am")
                .fullName("La menor")
                .fingerPositions("x02210")
                .difficulty(DifficultyLevel.BEGINNER)
                .isCommon(true)
                .displayOrder(2)
                .build();

        // Assert
        assertEquals(1L, chordInfo.getId());
        assertEquals("Am", chordInfo.getName());
        assertEquals("La menor", chordInfo.getFullName());
        assertEquals("x02210", chordInfo.getFingerPositions());
        assertEquals(DifficultyLevel.BEGINNER, chordInfo.getDifficulty());
        assertTrue(chordInfo.getIsCommon());
        assertEquals(2, chordInfo.getDisplayOrder());
    }

    @Test
    void testChordInfo_Setters() {
        // Arrange
        ChordInfo chordInfo = new ChordInfo();

        // Act
        chordInfo.setId(2L);
        chordInfo.setName("F");
        chordInfo.setFullName("Fa mayor");
        chordInfo.setFingerPositions("133211");
        chordInfo.setDifficulty(DifficultyLevel.INTERMEDIATE);
        chordInfo.setIsCommon(false);
        chordInfo.setDisplayOrder(3);

        // Assert
        assertEquals(2L, chordInfo.getId());
        assertEquals("F", chordInfo.getName());
        assertEquals("Fa mayor", chordInfo.getFullName());
        assertEquals("133211", chordInfo.getFingerPositions());
        assertEquals(DifficultyLevel.INTERMEDIATE, chordInfo.getDifficulty());
        assertFalse(chordInfo.getIsCommon());
        assertEquals(3, chordInfo.getDisplayOrder());
    }

    @Test
    void testChordInfo_Equals() {
        // Arrange
        ChordInfo chordInfo1 = ChordInfo.builder()
                .id(1L)
                .name("C")
                .fullName("Do mayor")
                .difficulty(DifficultyLevel.BEGINNER)
                .isCommon(true)
                .displayOrder(1)
                .build();

        ChordInfo chordInfo2 = ChordInfo.builder()
                .id(1L)
                .name("C")
                .fullName("Do mayor")
                .difficulty(DifficultyLevel.BEGINNER)
                .isCommon(true)
                .displayOrder(1)
                .build();

        ChordInfo chordInfo3 = ChordInfo.builder()
                .id(2L)
                .name("Am")
                .fullName("La menor")
                .difficulty(DifficultyLevel.BEGINNER)
                .isCommon(true)
                .displayOrder(2)
                .build();

        // Act & Assert
        assertEquals(chordInfo1, chordInfo2);
        assertNotEquals(chordInfo1, chordInfo3);
    }

    @Test
    void testChordInfo_HashCode() {
        // Arrange
        ChordInfo chordInfo1 = ChordInfo.builder()
                .id(1L)
                .name("C")
                .fullName("Do mayor")
                .difficulty(DifficultyLevel.BEGINNER)
                .isCommon(true)
                .displayOrder(1)
                .build();

        ChordInfo chordInfo2 = ChordInfo.builder()
                .id(1L)
                .name("C")
                .fullName("Do mayor")
                .difficulty(DifficultyLevel.BEGINNER)
                .isCommon(true)
                .displayOrder(1)
                .build();

        // Act & Assert
        assertEquals(chordInfo1.hashCode(), chordInfo2.hashCode());
    }

    @Test
    void testChordInfo_ToString() {
        // Arrange
        ChordInfo chordInfo = ChordInfo.builder()
                .id(1L)
                .name("C")
                .fullName("Do mayor")
                .difficulty(DifficultyLevel.BEGINNER)
                .isCommon(true)
                .displayOrder(1)
                .build();

        // Act
        String toString = chordInfo.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("C"));
        assertTrue(toString.contains("Do mayor"));
        assertTrue(toString.contains("BEGINNER"));
    }
}
