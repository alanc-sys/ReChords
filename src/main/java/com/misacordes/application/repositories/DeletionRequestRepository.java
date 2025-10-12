package com.misacordes.application.repositories;

import com.misacordes.application.entities.DeletionRequest;
import com.misacordes.application.entities.DeletionStatus;
import com.misacordes.application.entities.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeletionRequestRepository extends JpaRepository<DeletionRequest, Long> {

    Optional<DeletionRequest> findBySongAndStatus(Song song, DeletionStatus status);
    
    Page<DeletionRequest> findByStatus(DeletionStatus status, Pageable pageable);
    
    boolean existsBySongAndStatus(Song song, DeletionStatus status);
    
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM DeletionRequest dr WHERE dr.song.id = :songId")
    void deleteBySongId(@org.springframework.data.repository.query.Param("songId") Long songId);
}


