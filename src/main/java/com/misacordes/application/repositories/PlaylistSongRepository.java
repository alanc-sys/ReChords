package com.misacordes.application.repositories;

import com.misacordes.application.entities.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {
    
    List<PlaylistSong> findByPlaylistIdOrderByOrderIndexAsc(Long playlistId);

    Optional<PlaylistSong> findByPlaylistIdAndSongId(Long playlistId, Long songId);
    
    @Modifying
    @Query("DELETE FROM PlaylistSong ps WHERE ps.playlist.id = :playlistId AND ps.song.id = :songId")
    void deleteByPlaylistIdAndSongId(@Param("playlistId") Long playlistId, @Param("songId") Long songId);

    @Modifying
    @Query("DELETE FROM PlaylistSong ps WHERE ps.playlist.id = :playlistId")
    void deleteByPlaylistId(@Param("playlistId") Long playlistId);
    
    @Query("SELECT COALESCE(MAX(ps.orderIndex), 0) + 1 FROM PlaylistSong ps WHERE ps.playlist.id = :playlistId")
    Integer getNextOrderIndex(@Param("playlistId") Long playlistId);
    
    @Modifying
    @Query("UPDATE PlaylistSong ps SET ps.orderIndex = ps.orderIndex - 1 WHERE ps.playlist.id = :playlistId AND ps.orderIndex > :removedIndex")
    void updateOrderIndexesAfterRemoval(@Param("playlistId") Long playlistId, @Param("removedIndex") Integer removedIndex);
    
    @Query("SELECT ps FROM PlaylistSong ps JOIN FETCH ps.song s JOIN FETCH s.createdBy WHERE ps.playlist.id = :playlistId ORDER BY ps.orderIndex ASC")
    List<PlaylistSong> findByPlaylistIdWithSongDetails(@Param("playlistId") Long playlistId);

    @Modifying
    @Query("DELETE FROM PlaylistSong ps WHERE ps.song.id = :songId")
    void deleteBySongId(@Param("songId") Long songId);
}
