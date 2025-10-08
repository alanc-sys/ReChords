package com.misacordes.application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.misacordes.application.dto.request.LineWithChords;
import com.misacordes.application.dto.request.SongWithChordsRequest;
import com.misacordes.application.dto.response.SongAnalyticsResponse;
import com.misacordes.application.entities.Song;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service("songAnalyticsService")
@RequiredArgsConstructor
@Slf4j
public class SongAnalyticsService {

    private final ObjectMapper objectMapper;

    public SongAnalyticsResponse analyzeSongChords(Song song) {
        try {
            if (song.getChordsMap() == null || song.getChordsMap().trim().isEmpty()) {
                return createEmptyAnalytics(song);
            }

            SongWithChordsRequest songData = objectMapper.readValue(
                song.getChordsMap(), 
                new TypeReference<SongWithChordsRequest>() {}
            );

            return analyzeSongData(song, songData);

        } catch (JsonProcessingException e) {
            log.error("Error parsing chords map for song {}: {}", song.getId(), e.getMessage());
            return createEmptyAnalytics(song);
        }
    }

    private SongAnalyticsResponse analyzeSongData(Song song, SongWithChordsRequest songData) {
        List<LineWithChords> lyrics = songData.getLyrics();

        int totalChords = lyrics.stream()
            .mapToInt(line -> line.getChords() != null ? line.getChords().size() : 0)
            .sum();
        
        int totalLines = lyrics.size();

        Map<String, Integer> chordFrequency = new HashMap<>();
        for (LineWithChords line : lyrics) {
            if (line.getChords() != null) {
                for (var chord : line.getChords()) {
                    chordFrequency.merge(chord.getName(), 1, Integer::sum);
                }
            }
        }

        String mostUsedChord = chordFrequency.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        
        Integer mostUsedChordCount = mostUsedChord != null ? chordFrequency.get(mostUsedChord) : 0;

        List<SongAnalyticsResponse.ChordUsage> chordUsage = chordFrequency.entrySet().stream()
            .map(entry -> SongAnalyticsResponse.ChordUsage.builder()
                .chordName(entry.getKey())
                .count(entry.getValue())
                .percentage(totalChords > 0 ? (entry.getValue() * 100.0) / totalChords : 0.0)
                .build())
            .sorted((a, b) -> b.getCount().compareTo(a.getCount()))
            .collect(Collectors.toList());

        double averageChordsPerLine = totalLines > 0 ? (double) totalChords / totalLines : 0.0;
        
        int maxChordsInLine = lyrics.stream()
            .mapToInt(line -> line.getChords() != null ? line.getChords().size() : 0)
            .max()
            .orElse(0);
        
        return SongAnalyticsResponse.builder()
            .songId(song.getId())
            .title(song.getTitle())
            .totalChords(totalChords)
            .totalLines(totalLines)
            .uniqueChords(chordFrequency.size())
            .mostUsedChord(mostUsedChord)
            .mostUsedChordCount(mostUsedChordCount)
            .chordUsage(chordUsage)
            .chordFrequency(chordFrequency)
            .averageChordsPerLine(averageChordsPerLine)
            .maxChordsInLine(maxChordsInLine)
            .key(songData.getKey())
            .tempo(songData.getTempo())
            .build();
    }

    private SongAnalyticsResponse createEmptyAnalytics(Song song) {
        return SongAnalyticsResponse.builder()
            .songId(song.getId())
            .title(song.getTitle())
            .totalChords(0)
            .totalLines(0)
            .uniqueChords(0)
            .mostUsedChord(null)
            .mostUsedChordCount(0)
            .chordUsage(Collections.emptyList())
            .chordFrequency(Collections.emptyMap())
            .averageChordsPerLine(0.0)
            .maxChordsInLine(0)
            .key(null)
            .tempo(null)
            .build();
    }

    public boolean validateChordsMap(String chordsMapJson) {
        try {
            if (chordsMapJson == null || chordsMapJson.trim().isEmpty()) {
                return false;
            }
            
            SongWithChordsRequest songData = objectMapper.readValue(
                chordsMapJson, 
                new TypeReference<SongWithChordsRequest>() {}
            );

            if (songData.getLyrics() == null || songData.getLyrics().isEmpty()) {
                return false;
            }

            return songData.getLyrics().stream()
                .allMatch(line -> line.getText() != null && !line.getText().trim().isEmpty());
                
        } catch (JsonProcessingException e) {
            log.error("Invalid chords map JSON: {}", e.getMessage());
            return false;
        }
    }

    public Set<String> extractUniqueChords(Song song) {
        try {
            if (song.getChordsMap() == null || song.getChordsMap().trim().isEmpty()) {
                return Collections.emptySet();
            }

            SongWithChordsRequest songData = objectMapper.readValue(
                song.getChordsMap(), 
                new TypeReference<SongWithChordsRequest>() {}
            );

            return songData.getLyrics().stream()
                .filter(line -> line.getChords() != null)
                .flatMap(line -> line.getChords().stream())
                .map(chord -> chord.getName())
                .collect(Collectors.toSet());

        } catch (JsonProcessingException e) {
            log.error("Error extracting chords from song {}: {}", song.getId(), e.getMessage());
            return Collections.emptySet();
        }
    }
}
