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
public class SongAnalyticsAsyncService extends BaseService {

    private final SongAnalyticsService songAnalyticsService;
    private final SongRepository songRepository;

    @Async("songAnalyticsExecutor")
    public void processSongAnalyticsAsync(Long songId) {
        // Verificar que el usuario esté autenticado
        verifyAuthenticated();
        
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

            
            log.info("Analítica procesada para canción '{}': {} acordes únicos, {} total acordes", 
                song.getTitle(), 
                analytics.getUniqueChords(), 
                analytics.getTotalChords());

            validateChordsAgainstCatalog(song);
            
        } catch (Exception e) {
            log.error("Error en procesamiento asíncrono de analítica para canción ID {}: {}", 
                songId, e.getMessage(), e);
        }
    }

    private void validateChordsAgainstCatalog(Song song) {
        try {
            var uniqueChords = songAnalyticsService.extractUniqueChords(song);
            
            log.info("Validando {} acordes únicos para canción '{}'", 
                uniqueChords.size(), song.getTitle());

            
            for (String chordName : uniqueChords) {
                if (chordName == null || chordName.trim().isEmpty()) {
                    log.warn("Acorde vacío encontrado en canción '{}'", song.getTitle());
                }
            }
            
        } catch (Exception e) {
            log.error("Error validando acordes para canción '{}': {}", 
                song.getTitle(), e.getMessage());
        }
    }

    @Async("songAnalyticsExecutor")
    public void processMultipleSongsAnalyticsAsync() {
        verifyAdmin();
        
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
