package com.misacordes.application.repositories;

import com.misacordes.application.entities.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    
    // Obtener todas las playlists de un usuario
    List<Playlist> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Obtener playlists públicas de un usuario
    List<Playlist> findByUserIdAndIsPublicTrueOrderByCreatedAtDesc(Long userId);
    
    // Obtener playlists por defecto de un usuario
    List<Playlist> findByUserIdAndIsDefaultTrueOrderByNameAsc(Long userId);
    
    // Obtener una playlist específica de un usuario
    Optional<Playlist> findByIdAndUserId(Long id, Long userId);
    
    // Verificar si existe una playlist con un nombre para un usuario
    boolean existsByUserIdAndName(Long userId, String name);
    
    // Obtener playlists públicas de todos los usuarios (para explorar)
    @Query("SELECT p FROM Playlist p WHERE p.isPublic = true AND p.user.id != :userId ORDER BY p.createdAt DESC")
    List<Playlist> findPublicPlaylistsExcludingUser(@Param("userId") Long userId);
    
    // Buscar playlists públicas por nombre
    @Query("SELECT p FROM Playlist p WHERE p.isPublic = true AND LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY p.createdAt DESC")
    List<Playlist> findPublicPlaylistsByNameContaining(@Param("query") String query);
    
    // Contar canciones en una playlist
    @Query("SELECT COUNT(ps) FROM PlaylistSong ps WHERE ps.playlist.id = :playlistId")
    Long countSongsInPlaylist(@Param("playlistId") Long playlistId);
}
