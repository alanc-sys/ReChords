package com.misacordes.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.misacordes.application.dto.request.SongWithChordsRequest;
import com.misacordes.application.dto.response.ChordInfo;
import com.misacordes.application.dto.response.PageResponse;
import com.misacordes.application.dto.response.SongWithChordsResponse;
import com.misacordes.application.dto.response.SongAnalyticsResponse;
import com.misacordes.application.services.SongImportService;
import com.misacordes.application.services.ChordService;
import com.misacordes.application.services.SongService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SongControllerTest {

    @Mock
    private SongService songService;

    @Mock
    private ChordService chordService;

    @Mock
    private SongImportService songImportService;

    @InjectMocks
    private SongController songController;

    private SongWithChordsRequest testRequest;
    private SongWithChordsResponse testResponse;
    private PageResponse<SongWithChordsResponse> testPageResponse;

    @BeforeEach
    void setUp() {
        // Setup test request
        testRequest = new SongWithChordsRequest();
        testRequest.setTitle("Test Song");
        testRequest.setArtist("Test Artist");
        testRequest.setAlbum("Test Album");
        testRequest.setYear(2024);

        // Setup test response
        testResponse = SongWithChordsResponse.builder()
                .id(1L)
                .title("Test Song")
                .artist("Test Artist")
                .album("Test Album")
                .year(2024)
                .build();

        // Setup test page response
        testPageResponse = PageResponse.<SongWithChordsResponse>builder()
                .content(Arrays.asList(testResponse))
                .totalElements(1L)
                .totalPages(1)
                .numberOfElements(1)
                .first(true)
                .last(true)
                .build();
    }

    @Test
    void createSong_ShouldReturnCreatedResponse() {
        // Arrange
        when(songService.createSongWithChords(testRequest)).thenReturn(testResponse);

        // Act
        ResponseEntity<SongWithChordsResponse> response = songController.createSong(testRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Song", response.getBody().getTitle());
        verify(songService).createSongWithChords(testRequest);
    }

    @Test
    void updateSong_ShouldReturnOkResponse() {
        // Arrange
        when(songService.updateSongWithChords(1L, testRequest)).thenReturn(testResponse);

        // Act
        ResponseEntity<SongWithChordsResponse> response = songController.updateSong(1L, testRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Song", response.getBody().getTitle());
        verify(songService).updateSongWithChords(1L, testRequest);
    }

    @Test
    void getSongById_ShouldReturnOkResponse() {
        // Arrange
        when(songService.getSongWithChordsById(1L)).thenReturn(testResponse);

        // Act
        ResponseEntity<SongWithChordsResponse> response = songController.getSongById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Song", response.getBody().getTitle());
        verify(songService).getSongWithChordsById(1L);
    }

    @Test
    void deleteSong_ShouldReturnNoContentResponse() {
        // Arrange
        doNothing().when(songService).deleteSong(1L);

        // Act
        ResponseEntity<Void> response = songController.deleteSong(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(songService).deleteSong(1L);
    }

    @Test
    void getMySongsPaginated_ShouldReturnPaginatedResponse() {
        // Arrange
        when(songService.getMySongsWithChordsPaginated(any(Pageable.class))).thenReturn(testPageResponse);

        // Act
        ResponseEntity<PageResponse<SongWithChordsResponse>> response = songController.getMySongsPaginated(0, 10, new String[]{"createdAt", "desc"});

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("Test Song", response.getBody().getContent().get(0).getTitle());
        assertEquals(1L, response.getBody().getTotalElements());
        verify(songService).getMySongsWithChordsPaginated(any(Pageable.class));
    }

    @Test
    void getMySongsPaginated_ShouldLimitSizeToMax20() {
        // Arrange
        when(songService.getMySongsWithChordsPaginated(any(Pageable.class))).thenReturn(testPageResponse);

        // Act
        ResponseEntity<PageResponse<SongWithChordsResponse>> response = songController.getMySongsPaginated(0, 50, new String[]{"createdAt", "desc"});

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(songService).getMySongsWithChordsPaginated(argThat(pageable -> 
            pageable.getPageSize() == 20));
    }

    @Test
    void getMySongsPaginated_ShouldSetMinSizeTo1() {
        // Arrange
        when(songService.getMySongsWithChordsPaginated(any(Pageable.class))).thenReturn(testPageResponse);

        // Act
        ResponseEntity<PageResponse<SongWithChordsResponse>> response = songController.getMySongsPaginated(0, 0, new String[]{"createdAt", "desc"});

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(songService).getMySongsWithChordsPaginated(argThat(pageable -> 
            pageable.getPageSize() == 1));
    }

    @Test
    void getPublicSongsPaginated_ShouldReturnPaginatedResponse() {
        // Arrange
        when(songService.getPublicSongsWithChordsPaginated(any(Pageable.class))).thenReturn(testPageResponse);

        // Act
        ResponseEntity<PageResponse<SongWithChordsResponse>> response = songController.getPublicSongsPaginated(0, 10, new String[]{"publishedAt", "desc"});

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("Test Song", response.getBody().getContent().get(0).getTitle());
        verify(songService).getPublicSongsWithChordsPaginated(any(Pageable.class));
    }

    @Test
    void searchSongsPaginated_ShouldReturnPaginatedResponse() {
        // Arrange
        when(songService.searchPublicSongsWithChordsPaginated(eq("test"), any(Pageable.class))).thenReturn(testPageResponse);

        // Act
        ResponseEntity<PageResponse<SongWithChordsResponse>> response = songController.searchSongsPaginated("test", 0, 10, new String[]{"title", "asc"});

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("Test Song", response.getBody().getContent().get(0).getTitle());
        verify(songService).searchPublicSongsWithChordsPaginated("test", any(Pageable.class));
    }

    @Test
    void submitForApproval_ShouldReturnOkResponse() {
        // Arrange
        when(songService.submitForApprovalWithChords(1L)).thenReturn(testResponse);

        // Act
        ResponseEntity<SongWithChordsResponse> response = songController.submitForApproval(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Song", response.getBody().getTitle());
        verify(songService).submitForApprovalWithChords(1L);
    }

    @Test
    void getAvailableChords_ShouldReturnChordList() {
        // Arrange
        List<ChordInfo> chords = Arrays.asList(
                ChordInfo.builder().name("C").build(),
                ChordInfo.builder().name("D").build()
        );
        when(chordService.getAllChordsForSelection()).thenReturn(chords);

        // Act
        ResponseEntity<List<ChordInfo>> response = songController.getAvailableChords();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(chordService).getAllChordsForSelection();
    }

    @Test
    void getCommonChords_ShouldReturnChordList() {
        // Arrange
        List<ChordInfo> chords = Arrays.asList(
                ChordInfo.builder().name("C").build(),
                ChordInfo.builder().name("G").build()
        );
        when(chordService.getCommonChordsForSelection()).thenReturn(chords);

        // Act
        ResponseEntity<List<ChordInfo>> response = songController.getCommonChords();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(chordService).getCommonChordsForSelection();
    }

    @Test
    void getSongAnalytics_ShouldReturnAnalyticsResponse() {
        // Arrange
        SongAnalyticsResponse analytics = SongAnalyticsResponse.builder()
                .songId(1L)
                .title("Test Song")
                .totalChords(5)
                .build();
        when(songService.getSongAnalytics(1L)).thenReturn(analytics);

        // Act
        ResponseEntity<SongAnalyticsResponse> response = songController.getSongAnalytics(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getSongId());
        assertEquals("Test Song", response.getBody().getTitle());
        verify(songService).getSongAnalytics(1L);
    }

    @Test
    void importSong_ShouldReturnParsedRequest() {
        // Arrange
        String rawText = "Test song lyrics";
        when(songImportService.parse(rawText)).thenReturn(testRequest);

        // Act
        ResponseEntity<SongWithChordsRequest> response = songController.importSong(rawText);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Song", response.getBody().getTitle());
        verify(songImportService).parse(rawText);
    }

    @Test
    void transposeSong_ShouldReturnTransposedResponse() {
        // Arrange
        SongWithChordsResponse transposedResponse = SongWithChordsResponse.builder()
                .id(1L)
                .title("Test Song")
                .key("D") // Transposed from C
                .build();
        when(songService.transposeSong(1L, 2)).thenReturn(transposedResponse);

        // Act
        ResponseEntity<SongWithChordsResponse> response = songController.transposeSong(1L, 2);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("D", response.getBody().getKey());
        verify(songService).transposeSong(1L, 2);
    }

    @Test
    void getMySongsPaginated_ShouldCreateCorrectPageable() {
        // Arrange
        when(songService.getMySongsWithChordsPaginated(any(Pageable.class))).thenReturn(testPageResponse);

        // Act
        ResponseEntity<PageResponse<SongWithChordsResponse>> response = songController.getMySongsPaginated(1, 15, new String[]{"title", "asc"});

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(songService).getMySongsWithChordsPaginated(argThat(pageable -> 
            pageable.getPageNumber() == 1 && 
            pageable.getPageSize() == 15 &&
            pageable.getSort().iterator().next().getProperty().equals("title") &&
            pageable.getSort().iterator().next().getDirection() == Sort.Direction.ASC));
    }

    @Test
    void getMySongsPaginated_ShouldHandleDefaultSort() {
        // Arrange
        when(songService.getMySongsWithChordsPaginated(any(Pageable.class))).thenReturn(testPageResponse);

        // Act
        ResponseEntity<PageResponse<SongWithChordsResponse>> response = songController.getMySongsPaginated(0, 10, new String[]{});

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(songService).getMySongsWithChordsPaginated(argThat(pageable -> 
            pageable.getPageNumber() == 0 && 
            pageable.getPageSize() == 10 &&
            pageable.getSort().iterator().next().getProperty().equals("createdAt") &&
            pageable.getSort().iterator().next().getDirection() == Sort.Direction.DESC));
    }
}
