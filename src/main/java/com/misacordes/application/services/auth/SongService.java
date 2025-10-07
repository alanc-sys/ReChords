package com.misacordes.application.services.auth;

import com.misacordes.application.dto.request.SongRequest;
import com.misacordes.application.dto.response.SongResponse;
import com.misacordes.application.entities.Song;
import com.misacordes.application.entities.User;
import com.misacordes.application.repositories.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class SongService {

    private final SongRepository songRepository;

    private User getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    public SongResponse createSong(SongRequest request){
        User currentUser = getCurrentUser();
        Song song = Song.builder()
                .title(request.getTitle())
                .artist(request.getArtist())
                .album(request.getAlbum())
                .year(request.getYear())
                .user(currentUser)
                .build();

        Song savedSong = songRepository.save(song);

        return mapToResponse(savedSong);

    }
    public List<SongResponse> getAllSongs(){
        User currentUser = getCurrentUser();
        List<Song> songs = songRepository.findByUserId(currentUser.getId());
        return songs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteSong(Long id) {
        User currentUser = getCurrentUser();
        Song song = songRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() ->new RuntimeException("Song not found"));

        songRepository.delete(song);
    }

    public SongResponse songDetail(Long id){
        User currentUser = getCurrentUser();

        Song song = songRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Song not found"));

        return mapToResponse(song);
    }

    private SongResponse mapToResponse(Song song){
        return SongResponse.builder()
                .id(song.getId())
                .title(song.getTitle())
                .artist(song.getArtist())
                .album(song.getAlbum())
                .year(song.getYear())
                .createdAt(song.getCreatedAt())
                .build();

    }


}
