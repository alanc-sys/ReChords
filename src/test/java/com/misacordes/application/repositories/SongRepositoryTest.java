package com.misacordes.application.repositories;

import com.misacordes.application.entities.Role;
import com.misacordes.application.entities.Song;
import com.misacordes.application.entities.User;
import com.misacordes.application.utils.SongStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class SongRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SongRepository songRepository;

    private User testUser;
    private Song testSong1;
    private Song testSong2;
    private Song testSong3;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = User.builder()
                .username("testuser")
                .firstname("Test")
                .lastname("User")
                .password("password")
                .role(Role.USER)
                .build();
        testUser = entityManager.persistAndFlush(testUser);

        // Create test songs
        testSong1 = Song.builder()
                .title("Song One")
                .artist("Artist One")
                .album("Album One")
                .year(2024)
                .createdBy(testUser)
                .chordsMap("{\"title\":\"Song One\",\"lyrics\":[]}")
                .status(SongStatus.APPROVED)
                .isPublic(true)
                .publishedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        testSong2 = Song.builder()
                .title("Song Two")
                .artist("Artist Two")
                .album("Album Two")
                .year(2024)
                .createdBy(testUser)
                .chordsMap("{\"title\":\"Song Two\",\"lyrics\":[]}")
                .status(SongStatus.PENDING)
                .isPublic(false)
                .createdAt(LocalDateTime.now())
                .build();

        testSong3 = Song.builder()
                .title("Another Song")
                .artist("Artist One")
                .album("Album Three")
                .year(2024)
                .createdBy(testUser)
                .chordsMap("{\"title\":\"Another Song\",\"lyrics\":[]}")
                .status(SongStatus.DRAFT)
                .isPublic(false)
                .createdAt(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(testSong1);
        entityManager.persistAndFlush(testSong2);
        entityManager.persistAndFlush(testSong3);
        entityManager.clear();
    }

    @Test
    void findByCreatedById_ShouldReturnPaginatedSongs() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2);

        // Act
        Page<Song> result = songRepository.findByCreatedById(testUser.getId(), pageable);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().stream().allMatch(song -> song.getCreatedBy().getId() == testUser.getId()));
    }

    @Test
    void findByCreatedById_ShouldReturnSecondPage() {
        // Arrange
        Pageable pageable = PageRequest.of(1, 2);

        // Act
        Page<Song> result = songRepository.findByCreatedById(testUser.getId(), pageable);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertEquals(1, result.getContent().size());
        assertFalse(result.isFirst());
        assertTrue(result.isLast());
    }

    @Test
    void findByIsPublicTrueAndStatus_ShouldReturnPublicApprovedSongs() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Song> result = songRepository.findByIsPublicTrueAndStatus(SongStatus.APPROVED, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("Song One", result.getContent().get(0).getTitle());
        assertTrue(result.getContent().get(0).getIsPublic());
        assertEquals(SongStatus.APPROVED, result.getContent().get(0).getStatus());
    }

    @Test
    void findByStatus_ShouldReturnSongsByStatus() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Song> result = songRepository.findByStatus(SongStatus.PENDING, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("Song Two", result.getContent().get(0).getTitle());
        assertEquals(SongStatus.PENDING, result.getContent().get(0).getStatus());
    }

    @Test
    void findByIsPublicTrueAndStatusAndTitleContainingIgnoreCaseOrArtistContainingIgnoreCase_ShouldReturnMatchingSongs() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Song> result = songRepository.findByIsPublicTrueAndStatusAndTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(
                SongStatus.APPROVED, "One", "One", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("Song One", result.getContent().get(0).getTitle());
        assertTrue(result.getContent().get(0).getTitle().contains("One") || 
                  result.getContent().get(0).getArtist().contains("One"));
    }

    @Test
    void findByIsPublicTrueAndStatusAndTitleContainingIgnoreCaseOrArtistContainingIgnoreCase_ShouldBeCaseInsensitive() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Song> result = songRepository.findByIsPublicTrueAndStatusAndTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(
                SongStatus.APPROVED, "song", "song", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Song One", result.getContent().get(0).getTitle());
    }

    @Test
    void findAll_ShouldReturnAllSongsPaginated() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2);

        // Act
        Page<Song> result = songRepository.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void findByIdAndCreatedById_ShouldReturnSongWhenOwned() {
        // Act
        Optional<Song> result = songRepository.findByIdAndCreatedById(testSong1.getId(), testUser.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Song One", result.get().getTitle());
        assertEquals(testUser.getId(), result.get().getCreatedBy().getId());
    }

    @Test
    void findByIdAndCreatedById_ShouldReturnEmptyWhenNotOwned() {
        // Arrange
        User anotherUser = User.builder()
                .username("anotheruser")
                .firstname("Another")
                .lastname("User")
                .password("password")
                .role(Role.USER)
                .build();
        anotherUser = entityManager.persistAndFlush(anotherUser);

        // Act
        Optional<Song> result = songRepository.findByIdAndCreatedById(testSong1.getId(), anotherUser.getId());

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void countByStatus_ShouldReturnCorrectCount() {
        // Act
        Long count = songRepository.countByStatus(SongStatus.APPROVED);

        // Assert
        assertEquals(1L, count);
    }

    @Test
    void findByCreatedById_ShouldRespectSorting() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "title"));

        // Act
        Page<Song> result = songRepository.findByCreatedById(testUser.getId(), pageable);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals("Song Two", result.getContent().get(0).getTitle()); // Sorted by title DESC
        assertEquals("Song One", result.getContent().get(1).getTitle());
        assertEquals("Another Song", result.getContent().get(2).getTitle());
    }

    @Test
    void findByIsPublicTrueAndStatus_ShouldRespectSorting() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "artist"));

        // Act
        Page<Song> result = songRepository.findByIsPublicTrueAndStatus(SongStatus.APPROVED, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Song One", result.getContent().get(0).getTitle());
    }
}
