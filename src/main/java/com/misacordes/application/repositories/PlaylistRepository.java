package com.misacordes.application.repositories;

import com.misacordes.application.entities.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    
    List<Playlist> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Playlist> findByUserIdAndIsPublicTrueOrderByCreatedAtDesc(Long userId);

    List<Playlist> findByUserIdAndIsDefaultTrueOrderByNameAsc(Long userId);
    
    Optional<Playlist> findByIdAndUserId(Long id, Long userId);
    
    boolean existsByUserIdAndName(Long userId, String name);
    
    @Query("SELECT p FROM Playlist p WHERE p.isPublic = true AND p.user.id != :userId ORDER BY p.createdAt DESC")
    List<Playlist> findPublicPlaylistsExcludingUser(@Param("userId") Long userId);
    
    @Query("SELECT p FROM Playlist p WHERE p.isPublic = true AND LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY p.createdAt DESC")
    List<Playlist> findPublicPlaylistsByNameContaining(@Param("query") String query);
    
    @Query("SELECT COUNT(ps) FROM PlaylistSong ps WHERE ps.playlist.id = :playlistId")
    Long countSongsInPlaylist(@Param("playlistId") Long playlistId);
}
