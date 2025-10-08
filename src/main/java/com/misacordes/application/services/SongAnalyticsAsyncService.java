package com.misacordes.application.services;

import com.misacordes.application.entities.Song;
import com.misacordes.application.repositories.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("songAnalyticsAsyncService")
@RequiredArgsConstructor
@Slf4j
public class SongAnalyticsAsyncService {

    private final SongAnalyticsService songAnalyticsService;
    private final SongRepository songRepository;

    /**
     * Procesar analítica de una canción de forma asíncrona
     */
    @Async("songAnalyticsExecutor")
    public void processSongAnalyticsAsync(Long songId) {
        try {
            log.info("Iniciando procesamiento asíncrono de analítica para canción ID: {}", songId);
            
            Optional<Song> songOpt = songRepository.findById(songId);
            if (songOpt.isEmpty()) {
                log.warn("Canción con ID {} no encontrada para procesamiento asíncrono", songId);
                return;
            }
            
            Song song = songOpt.get();
            
            // Procesar analítica
            var analytics = songAnalyticsService.analyzeSongChords(song);
            
            // Aquí podrías guardar la analítica en una tabla separada si lo deseas
            // o realizar otras operaciones de procesamiento
            
            log.info("Analítica procesada para canción '{}': {} acordes únicos, {} total acordes", 
                song.getTitle(), 
                analytics.getUniqueChords(), 
                analytics.getTotalChords());
            
            // Ejemplo: validar acordes contra el catálogo
            validateChordsAgainstCatalog(song);
            
        } catch (Exception e) {
            log.error("Error en procesamiento asíncrono de analítica para canción ID {}: {}", 
                songId, e.getMessage(), e);
        }
    }

    /**
     * Validar que los acordes de la canción existan en el catálogo
     */
    private void validateChordsAgainstCatalog(Song song) {
        try {
            var uniqueChords = songAnalyticsService.extractUniqueChords(song);
            
            log.info("Validando {} acordes únicos para canción '{}'", 
                uniqueChords.size(), song.getTitle());
            
            // Aquí podrías implementar validación contra ChordCatalog
            // y generar reportes de acordes no encontrados
            
            for (String chordName : uniqueChords) {
                // Validación básica - podrías expandir esto
                if (chordName == null || chordName.trim().isEmpty()) {
                    log.warn("Acorde vacío encontrado en canción '{}'", song.getTitle());
                }
            }
            
        } catch (Exception e) {
            log.error("Error validando acordes para canción '{}': {}", 
                song.getTitle(), e.getMessage());
        }
    }

    /**
     * Procesar analítica para múltiples canciones de forma asíncrona
     */
    @Async("songAnalyticsExecutor")
    public void processMultipleSongsAnalyticsAsync() {
        try {
            log.info("Iniciando procesamiento masivo de analítica");
            
            var songs = songRepository.findAll();
            int processed = 0;
            
            for (Song song : songs) {
                if (song.getChordsMap() != null && !song.getChordsMap().trim().isEmpty()) {
                    processSongAnalyticsAsync(song.getId());
                    processed++;
                }
            }
            
            log.info("Procesamiento masivo completado: {} canciones procesadas", processed);
            
        } catch (Exception e) {
            log.error("Error en procesamiento masivo de analítica: {}", e.getMessage(), e);
        }
    }
}
