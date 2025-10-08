package com.misacordes.application.repositories;

import com.misacordes.application.entities.Song;
import com.misacordes.application.utils.SongStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SongRepository extends JpaRepository <Song, Long> {

    List<Song> findByCreatedById(Long userId);

    List<Song> findByIsPublicTrueAndStatus(SongStatus status);

    List<Song> findByStatus(SongStatus status);

    Optional<Song> findByIdAndCreatedById(Long id, Long userId);

    Long countByStatus(SongStatus status);

    List<Song> findByIsPublicTrueAndStatusAndTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(
            SongStatus status, String title, String artist
    );
}