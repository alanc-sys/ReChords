package com.misacordes.application.repositories;

import com.misacordes.application.entities.ProposedChord;
import com.misacordes.application.entities.ProposalStatus;
import com.misacordes.application.entities.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProposedChordRepository extends JpaRepository<ProposedChord, Long> {
    
    List<ProposedChord> findBySong(Song song);
    
    List<ProposedChord> findBySongId(Long songId);
    
    List<ProposedChord> findByStatus(ProposalStatus status);
    
    List<ProposedChord> findByStatusOrderByCreatedAtDesc(ProposalStatus status);

    @Query("DELETE FROM ProposedChord pc WHERE pc.song.id = :songId")
    void deleteBySongId(@org.springframework.data.repository.query.Param("songId") Long songId);
}

