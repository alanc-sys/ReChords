package com.misacordes.application.controller;

import com.misacordes.application.entities.Song;
import com.misacordes.application.repositories.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/songs")
public class FileUploadController {

    @Value("${upload.dir:uploads/covers/}")
    private String UPLOAD_DIR;
    
    private static final long MAX_FILE_SIZE = 5_000_000; // 5MB

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    @Autowired
    private SongRepository songRepository;

    @PostMapping("/{id}/cover")
    public ResponseEntity<?> uploadCover(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Archivo vacío");
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body("Imagen muy grande (máximo 5MB)");
            }

            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
                return ResponseEntity.badRequest().body("Tipo de archivo no permitido. Solo se permiten: JPG, PNG, GIF, WEBP");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                return ResponseEntity.badRequest().body("Nombre de archivo inválido");
            }
            
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                return ResponseEntity.badRequest().body("Extensión no permitida. Solo se permiten: .jpg, .jpeg, .png, .gif, .webp");
            }

            Song song = songRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Canción no encontrada"));

            String currentUsername = authentication.getName();
            if (!song.getCreatedBy().getUsername().equals(currentUsername)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("No tienes permisos para modificar esta canción");
            }

            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(filename);
            
            if (!filePath.normalize().startsWith(uploadPath.normalize())) {
                return ResponseEntity.badRequest().body("Ruta de archivo inválida");
            }

            Files.copy(file.getInputStream(), filePath);

            String coverUrl = "/api/uploads/covers/" + filename;
            song.setCoverImageUrl(coverUrl);
            song.setCoverColor(null); // Limpiar color si había uno generado
            songRepository.save(song);

            return ResponseEntity.ok(coverUrl);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar la imagen: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}/cover")
    public ResponseEntity<?> removeCover(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Canción no encontrada"));

        String currentUsername = authentication.getName();
        if (!song.getCreatedBy().getUsername().equals(currentUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes permisos para modificar esta canción");
        }

        song.setCoverImageUrl(null);
        songRepository.save(song);

        return ResponseEntity.ok().build();
    }
}

