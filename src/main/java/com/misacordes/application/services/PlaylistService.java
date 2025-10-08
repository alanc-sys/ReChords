package com.misacordes.application.services;

import com.misacordes.application.dto.request.AddSongToPlaylistRequest;
import com.misacordes.application.dto.request.CreatePlaylistRequest;
import com.misacordes.application.dto.request.UpdatePlaylistRequest;
import com.misacordes.application.dto.response.PlaylistResponse;
import com.misacordes.application.dto.response.PlaylistSummaryResponse;
import com.misacordes.application.dto.response.SongWithChordsResponse;
import com.misacordes.application.entities.Playlist;
import com.misacordes.application.entities.PlaylistSong;
import com.misacordes.application.entities.Song;
import com.misacordes.application.entities.User;
import com.misacordes.application.repositories.PlaylistRepository;
import com.misacordes.application.repositories.PlaylistSongRepository;
import com.misacordes.application.repositories.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final SongRepository songRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    /**
     * Crear una nueva playlist
     */
    public PlaylistResponse createPlaylist(CreatePlaylistRequest request) {
        User currentUser = getCurrentUser();
        
        // Verificar que no exista una playlist con el mismo nombre
        if (playlistRepository.existsByUserIdAndName(currentUser.getId(), request.getName())) {
            throw new RuntimeException("Ya tienes una playlist con el nombre: " + request.getName());
        }
        
        Playlist playlist = Playlist.builder()
                .name(request.getName())
                .description(request.getDescription())
                .user(currentUser)
                .isDefault(false)
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : false)
                .build();
        
        Playlist savedPlaylist = playlistRepository.save(playlist);
        return mapToPlaylistResponse(savedPlaylist);
    }

    /**
     * Obtener todas las playlists del usuario actual
     */
    @Transactional(readOnly = true)
    public List<PlaylistSummaryResponse> getMyPlaylists() {
        User currentUser = getCurrentUser();
        List<Playlist> playlists = playlistRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
        
        return playlists.stream()
                .map(this::mapToPlaylistSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener una playlist específica con sus canciones
     */
    @Transactional(readOnly = true)
    public PlaylistResponse getPlaylistById(Long playlistId) {
        User currentUser = getCurrentUser();
        Playlist playlist = playlistRepository.findByIdAndUserId(playlistId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Playlist no encontrada"));
        
        return mapToPlaylistResponseWithSongs(playlist);
    }

    /**
     * Actualizar una playlist
     */
    public PlaylistResponse updatePlaylist(Long playlistId, UpdatePlaylistRequest request) {
        User currentUser = getCurrentUser();
        Playlist playlist = playlistRepository.findByIdAndUserId(playlistId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Playlist no encontrada"));
        
        // No permitir editar playlists por defecto
        if (playlist.getIsDefault()) {
            throw new RuntimeException("No puedes editar playlists por defecto");
        }
        
        // Verificar nombre único si se está cambiando
        if (request.getName() != null && !request.getName().equals(playlist.getName())) {
            if (playlistRepository.existsByUserIdAndName(currentUser.getId(), request.getName())) {
                throw new RuntimeException("Ya tienes una playlist con el nombre: " + request.getName());
            }
        }
        
        if (request.getName() != null) playlist.setName(request.getName());
        if (request.getDescription() != null) playlist.setDescription(request.getDescription());
        if (request.getIsPublic() != null) playlist.setIsPublic(request.getIsPublic());
        
        Playlist updatedPlaylist = playlistRepository.save(playlist);
        return mapToPlaylistResponse(updatedPlaylist);
    }

    /**
     * Eliminar una playlist
     */
    public void deletePlaylist(Long playlistId) {
        User currentUser = getCurrentUser();
        Playlist playlist = playlistRepository.findByIdAndUserId(playlistId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Playlist no encontrada"));
        
        // No permitir eliminar playlists por defecto
        if (playlist.getIsDefault()) {
            throw new RuntimeException("No puedes eliminar playlists por defecto");
        }
        
        // Eliminar todas las canciones de la playlist
        playlistSongRepository.deleteByPlaylistId(playlistId);
        
        // Eliminar la playlist
        playlistRepository.delete(playlist);
    }

    /**
     * Añadir una canción a una playlist
     */
    public PlaylistResponse addSongToPlaylist(Long playlistId, AddSongToPlaylistRequest request) {
        User currentUser = getCurrentUser();
        Playlist playlist = playlistRepository.findByIdAndUserId(playlistId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Playlist no encontrada"));
        
        Song song = songRepository.findById(request.getSongId())
                .orElseThrow(() -> new RuntimeException("Canción no encontrada"));
        
        // Verificar que la canción sea pública o del usuario
        if (!song.getIsPublic() && song.getCreatedBy().getId() != currentUser.getId()) {
            throw new RuntimeException("No puedes añadir esta canción a tu playlist");
        }
        
        // Verificar que la canción no esté ya en la playlist
        if (playlistSongRepository.findByPlaylistIdAndSongId(playlistId, request.getSongId()).isPresent()) {
            throw new RuntimeException("La canción ya está en esta playlist");
        }
        
        // Determinar el índice de orden
        Integer orderIndex = request.getOrderIndex();
        if (orderIndex == null) {
            orderIndex = playlistSongRepository.getNextOrderIndex(playlistId);
        }
        
        PlaylistSong playlistSong = PlaylistSong.builder()
                .playlist(playlist)
                .song(song)
                .orderIndex(orderIndex)
                .build();
        
        playlistSongRepository.save(playlistSong);
        
        return mapToPlaylistResponseWithSongs(playlist);
    }

    /**
     * Eliminar una canción de una playlist
     */
    public PlaylistResponse removeSongFromPlaylist(Long playlistId, Long songId) {
        User currentUser = getCurrentUser();
        Playlist playlist = playlistRepository.findByIdAndUserId(playlistId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Playlist no encontrada"));
        
        PlaylistSong playlistSong = playlistSongRepository.findByPlaylistIdAndSongId(playlistId, songId)
                .orElseThrow(() -> new RuntimeException("La canción no está en esta playlist"));
        
        // Actualizar índices de orden
        playlistSongRepository.updateOrderIndexesAfterRemoval(playlistId, playlistSong.getOrderIndex());
        
        // Eliminar la relación
        playlistSongRepository.delete(playlistSong);
        
        return mapToPlaylistResponseWithSongs(playlist);
    }

    /**
     * Obtener playlists públicas para explorar
     */
    @Transactional(readOnly = true)
    public List<PlaylistSummaryResponse> getPublicPlaylists() {
        User currentUser = getCurrentUser();
        List<Playlist> playlists = playlistRepository.findPublicPlaylistsExcludingUser(currentUser.getId());
        
        return playlists.stream()
                .map(this::mapToPlaylistSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Buscar playlists públicas por nombre
     */
    @Transactional(readOnly = true)
    public List<PlaylistSummaryResponse> searchPublicPlaylists(String query) {
        List<Playlist> playlists = playlistRepository.findPublicPlaylistsByNameContaining(query);
        
        return playlists.stream()
                .map(this::mapToPlaylistSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Crear playlists por defecto para un usuario nuevo
     */
    public void createDefaultPlaylistsForUser(User user) {
        // Playlist "Favoritas"
        Playlist favorites = Playlist.builder()
                .name("Favoritas")
                .description("Mis canciones favoritas")
                .user(user)
                .isDefault(true)
                .isPublic(false)
                .build();
        playlistRepository.save(favorites);
        
        // Playlist "Mis Creaciones"
        Playlist myCreations = Playlist.builder()
                .name("Mis Creaciones")
                .description("Canciones que he creado")
                .user(user)
                .isDefault(true)
                .isPublic(false)
                .build();
        playlistRepository.save(myCreations);
    }

    // ========== MÉTODOS PRIVADOS DE MAPEO ==========

    private PlaylistResponse mapToPlaylistResponse(Playlist playlist) {
        Long songCount = playlistRepository.countSongsInPlaylist(playlist.getId());
        
        return PlaylistResponse.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .description(playlist.getDescription())
                .isDefault(playlist.getIsDefault())
                .isPublic(playlist.getIsPublic())
                .createdAt(playlist.getCreatedAt())
                .updatedAt(playlist.getUpdatedAt())
                .songCount(songCount)
                .createdBy(PlaylistResponse.CreatorInfo.builder()
                        .id(playlist.getUser().getId())
                        .username(playlist.getUser().getUsername())
                        .firstname(playlist.getUser().getFirstname())
                        .build())
                .build();
    }

    private PlaylistResponse mapToPlaylistResponseWithSongs(Playlist playlist) {
        List<PlaylistSong> playlistSongs = playlistSongRepository.findByPlaylistIdWithSongDetails(playlist.getId());
        
        List<SongWithChordsResponse> songs = playlistSongs.stream()
                .map(ps -> mapToSongWithChordsResponse(ps.getSong()))
                .collect(Collectors.toList());
        
        return PlaylistResponse.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .description(playlist.getDescription())
                .isDefault(playlist.getIsDefault())
                .isPublic(playlist.getIsPublic())
                .createdAt(playlist.getCreatedAt())
                .updatedAt(playlist.getUpdatedAt())
                .songCount((long) songs.size())
                .songs(songs)
                .createdBy(PlaylistResponse.CreatorInfo.builder()
                        .id(playlist.getUser().getId())
                        .username(playlist.getUser().getUsername())
                        .firstname(playlist.getUser().getFirstname())
                        .build())
                .build();
    }

    private PlaylistSummaryResponse mapToPlaylistSummaryResponse(Playlist playlist) {
        Long songCount = playlistRepository.countSongsInPlaylist(playlist.getId());
        
        return PlaylistSummaryResponse.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .description(playlist.getDescription())
                .isDefault(playlist.getIsDefault())
                .isPublic(playlist.getIsPublic())
                .createdAt(playlist.getCreatedAt())
                .updatedAt(playlist.getUpdatedAt())
                .songCount(songCount)
                .createdBy(PlaylistSummaryResponse.CreatorInfo.builder()
                        .id(playlist.getUser().getId())
                        .username(playlist.getUser().getUsername())
                        .firstname(playlist.getUser().getFirstname())
                        .build())
                .build();
    }

    private SongWithChordsResponse mapToSongWithChordsResponse(Song song) {
        // Aquí necesitarías implementar el mapeo completo
        // Por ahora retorno una versión simplificada
        return SongWithChordsResponse.builder()
                .id(song.getId())
                .title(song.getTitle())
                .artist(song.getArtist())
                .album(song.getAlbum())
                .year(song.getYear())
                .status(song.getStatus())
                .isPublic(song.getIsPublic())
                .createdAt(song.getCreatedAt())
                .publishedAt(song.getPublishedAt())
                .createdBy(SongWithChordsResponse.CreatorInfo.builder()
                        .id(song.getCreatedBy().getId())
                        .username(song.getCreatedBy().getUsername())
                        .firstname(song.getCreatedBy().getFirstname())
                        .build())
                .build();
    }
}
