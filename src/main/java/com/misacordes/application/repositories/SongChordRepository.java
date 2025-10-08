package com.misacordes.application.repositories;

import com.misacordes.application.entities.SongChord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongChordRepository extends JpaRepository<SongChord, Long> {
    
    /**
     * Obtener todos los acordes de una canción ordenados por línea y posición
     */
    List<SongChord> findBySongIdOrderByLineNumberAscPositionStartAsc(Long songId);
    
    /**
     * Eliminar todos los acordes de una canción
     */
    void deleteBySongId(Long songId);
    
    /**
     * Contar acordes de una canción
     */
    long countBySongId(Long songId);
    
    /**
     * Obtener acordes por nombre en una canción específica
     */
    List<SongChord> findBySongIdAndChordName(Long songId, String chordName);
}
