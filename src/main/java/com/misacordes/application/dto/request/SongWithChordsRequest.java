package com.misacordes.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SongWithChordsRequest {
    
    @NotBlank(message = "El título de la canción es obligatorio")
    @Size(max = 255, message = "El título no puede exceder 255 caracteres")
    private String title;
    
    @Size(max = 255, message = "El artista no puede exceder 255 caracteres")
    private String artist;
    
    @Size(max = 255, message = "El álbum no puede exceder 255 caracteres")
    private String album;
    
    @Min(value = 1900, message = "El año debe ser mayor a 1900")
    @Max(value = 2100, message = "El año debe ser menor a 2100")
    private Integer year;
    
    @Size(max = 10, message = "La tonalidad no puede exceder 10 caracteres")
    private String key;          // Tonalidad de la canción (C, Am, etc.)
    
    @Min(value = 1, message = "El tempo debe ser mayor a 0")
    @Max(value = 500, message = "El tempo debe ser menor a 500 BPM")
    private Integer tempo;       // BPM de la canción
    
    // Enlaces multimedia
    @Size(max = 500, message = "La URL de YouTube no puede exceder 500 caracteres")
    private String youtubeUrl;   // URL de YouTube
    
    @Size(max = 500, message = "La URL de Spotify no puede exceder 500 caracteres")
    private String spotifyUrl;   // URL de Spotify
    
    // Personalización de portada
    @Size(max = 500, message = "La URL de la imagen no puede exceder 500 caracteres")
    private String coverImageUrl; // URL de imagen subida
    
    @Size(max = 7, message = "El color debe ser formato hex (#RRGGBB)")
    private String coverColor;    // Color hex para portada generada
    
    private List<LineWithChords> lyrics; // Líneas con texto y acordes
    private Long createdBy;      // ID del usuario que crea la canción
    private List<ProposedChordRequest> proposedChords; // Acordes nuevos propuestos
}
