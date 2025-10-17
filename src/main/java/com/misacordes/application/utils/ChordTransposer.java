package com.misacordes.application.utils;


import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChordTransposer {
    private static final List<String> NOTES_SHARP = List.of("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B");
    private static final List<String> NOTES_FLAT = List.of("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B");

    public static String transpose(String chordName, int semitones) {
        if (chordName == null || semitones == 0) return chordName;

        Pattern pattern = Pattern.compile("([A-G][b#]?)(.*)");
        Matcher matcher = pattern.matcher(chordName);

        if (!matcher.matches()) return chordName; // No es un acorde válido

        String rootNote = matcher.group(1);
        String quality = matcher.group(2);

        int rootIndex = NOTES_SHARP.indexOf(rootNote);
        if (rootIndex == -1) rootIndex = NOTES_FLAT.indexOf(rootNote);
        if (rootIndex == -1) return chordName; // Nota no reconocida

        int newIndex = (rootIndex + semitones % 12 + 12) % 12;

        String newRoot = NOTES_SHARP.get(newIndex);

        return newRoot + quality;
    }
}