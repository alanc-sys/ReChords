package com.misacordes.application.repositories;

import com.misacordes.application.entities.Song;
import com.misacordes.application.utils.SongStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface SongRepository extends JpaRepository <Song, Long> {


    Optional<Song> findByIdAndCreatedById(Long id, Long userId);

    Long countByStatus(SongStatus status);


    Page<Song> findByCreatedById(Long userId, Pageable pageable);

    Page<Song> findByIsPublicTrueAndStatus(SongStatus status, Pageable pageable);

    Page<Song> findByStatus(SongStatus status, Pageable pageable);

    Page<Song> findByIsPublicTrueAndStatusAndTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(
            SongStatus status, String title, String artist, Pageable pageable
    );

    Page<Song> findAll(Pageable pageable);
}