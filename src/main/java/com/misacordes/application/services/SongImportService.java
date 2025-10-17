package com.misacordes.application.services;

import com.misacordes.application.dto.request.ChordPositionInfo;
import com.misacordes.application.dto.request.LineWithChords;
import com.misacordes.application.dto.request.SongWithChordsRequest;
import com.misacordes.application.entities.ChordCatalog;
import com.misacordes.application.entities.User;
import com.misacordes.application.repositories.ChordCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SongImportService extends BaseService {

    private final ChordCatalogRepository chordCatalogRepository;
    // Patrón mejorado: acepta mayúsculas y minúsculas, más variaciones de acordes
    private static final Pattern CHORD_PATTERN = Pattern.compile("\\b([A-Ga-g][b#]?(m|maj|min|dim|aug|sus|add)?[0-9]?[79]?(?![a-zA-Z]))\\b");

    public SongWithChordsRequest parse(String rawText) {
        // Verificar que el usuario esté autenticado
        User currentUser = getCurrentUser();
        
        SongWithChordsRequest request = new SongWithChordsRequest();
        List<LineWithChords> lyrics = new ArrayList<>();
        String[] lines = rawText.split("\\r?\\n");

        // Variables para detectar título y artista
        String detectedTitle = null;
        String detectedArtist = null;
        int startIndex = 0;

        // Buscar título y artista en las primeras líneas
        for (int i = 0; i < Math.min(5, lines.length); i++) {
            String line = lines[i].trim();
            
            // Saltar líneas vacías al inicio
            if (line.isEmpty()) {
                startIndex = i + 1;
                continue;
            }
            
            // Formato estándar: "Artista - Título"
            if (line.contains(" - ") && !isChordLine(line) && !isTabLine(line) && !isSectionHeader(line)) {
                String[] parts = line.split(" - ", 2);
                detectedArtist = parts[0].trim();
                detectedTitle = parts[1].trim();
                startIndex = i + 1;
                break;
            }
            
            // Si la primera línea no vacía parece un título (no es acorde, tab o sección)
            if (detectedTitle == null && !isChordLine(line) && !isTabLine(line) && !isSectionHeader(line)) {
                // Si la línea es corta (menos de 50 caracteres) y no tiene mucha puntuación, puede ser el título
                if (line.length() < 50 && line.split("\\s+").length <= 8) {
                    detectedTitle = line;
                    startIndex = i + 1;
                    break;
                }
            }
            
            // Si llegamos a una línea de acordes o contenido real, detener búsqueda
            if (isChordLine(line) || isSectionHeader(line)) {
                break;
            }
        }

        // Valores por defecto si no se detectó nada
        request.setTitle(detectedTitle != null ? detectedTitle : "Canción sin título");
        request.setArtist(detectedArtist != null ? detectedArtist : "Artista desconocido");

        // Procesar el resto de las líneas (letra y acordes)
        int lineNumber = 0;
        for (int i = startIndex; i < lines.length; i++) {
            String currentLine = lines[i];

            // Ignorar líneas de tablatura y metadata
            if (isTabLine(currentLine) || isSectionHeader(currentLine) || currentLine.trim().isEmpty()) {
                // Simplemente lo añadimos como una línea de texto sin acordes
                lyrics.add(new LineWithChords(lineNumber++, currentLine, new ArrayList<>()));
                continue;
            }

            // Lógica de emparejamiento de línea de acorde y letra
            if (isChordLine(currentLine)) {
                // Si la siguiente línea no es otra línea de acordes y no está vacía, es la letra
                if (i + 1 < lines.length && !isChordLine(lines[i+1]) && !lines[i+1].trim().isEmpty() 
                    && !isTabLine(lines[i+1]) && !isSectionHeader(lines[i+1])) {
                    lyrics.add(processLinePair(lineNumber++, currentLine, lines[i+1]));
                    i++; // Avanzamos una línea extra porque ya la hemos procesado
                } else {
                    // Línea de acordes sin letra debajo (ej. en solos instrumentales)
                    // Solo agregar si tiene acordes reales
                    LineWithChords chordOnlyLine = processLinePair(lineNumber++, currentLine, "");
                    if (!chordOnlyLine.getChords().isEmpty()) {
                        lyrics.add(chordOnlyLine);
                    }
                }
            } else {
                // Línea de letra sin acordes (solo si no está vacía o es una sección)
                if (!currentLine.trim().isEmpty()) {
                    lyrics.add(new LineWithChords(lineNumber++, currentLine, new ArrayList<>()));
                }
            }
        }

        request.setLyrics(lyrics);
        request.setCreatedBy(currentUser.getId());
        return request;
    }

    private boolean isSectionHeader(String line) {
        String trimmed = line.trim();
        // Detectar secciones como [Intro], [Estribillo], [Tab - Solo], etc.
        return trimmed.startsWith("[") && trimmed.contains("]");
    }

    private boolean isTabLine(String line) {
        String trimmed = line.trim();
        
        // Detectar líneas de tablatura que empiezan con nota y pipe: E|, B|, G|, D|, A|, e|
        if (trimmed.matches("^[EBGDAebgda]\\|.*")) {
            return true;
        }
        
        // Detectar líneas que son solo caracteres de tablatura
        if (trimmed.matches("^[A-G#b|x\\-0-9p/\\\\~hbr]+$") && trimmed.contains("|")) {
            return true;
        }
        
        // Detectar líneas de información de acordes (ej: "B7 = X 2 1 2 0 2")
        if (trimmed.matches("^[A-G][b#]?(m|maj|dim|aug|sus)?[0-9]?[7]?\\s*=.*")) {
            return true;
        }
        
        // Detectar separadores de acordes (ej: "----------------- Acordes -----------------")
        if (trimmed.matches("^-+.*-+$")) {
            return true;
        }
        
        return false;
    }

    private boolean isChordLine(String line) {
        String trimmedLine = line.trim();
        if (trimmedLine.isEmpty()) return false;
        
        // Si es una línea de tablatura o sección, no es línea de acordes
        if (isTabLine(trimmedLine) || isSectionHeader(trimmedLine)) {
            return false;
        }
        
        // Remover secciones al inicio para analizar solo los acordes
        String lineWithoutSection = trimmedLine.replaceFirst("^\\[.*?\\]\\s*", "");
        
        // Si después de quitar la sección no queda nada, no es línea de acordes
        if (lineWithoutSection.trim().isEmpty()) {
            return false;
        }

        Matcher matcher = CHORD_PATTERN.matcher(lineWithoutSection);
        int chordChars = 0;
        int matchCount = 0;
        
        while(matcher.find()) {
            chordChars += matcher.group(0).length();
            matchCount++;
        }
        
        String withoutSpaces = lineWithoutSection.replaceAll("\\s+", "");
        
        // Si no tiene caracteres sin espacios, no es línea de acordes
        if (withoutSpaces.isEmpty()) {
            return false;
        }
        
        double ratio = (double) chordChars / withoutSpaces.length();
        
        // Línea es de acordes si:
        // - Tiene al menos 1 acorde Y más del 60% de caracteres son acordes, O
        // - Tiene al menos 2 acordes Y más del 40% de caracteres son acordes
        if (matchCount >= 2 && ratio > 0.4) {
            return true;
        }
        
        if (matchCount >= 1 && ratio > 0.6) {
            return true;
        }

        return false;
    }

    private LineWithChords processLinePair(int lineNumber, String chordLine, String textLine) {
        List<ChordPositionInfo> chords = new ArrayList<>();
        
        // Remover sección al inicio si existe (ej: "[Intro] G B7 Em" -> "G B7 Em")
        String cleanChordLine = chordLine.trim().replaceFirst("^\\[.*?\\]\\s*", "");
        
        // Usar la línea original (con espacios) para mantener las posiciones correctas
        String originalChordLine = chordLine;
        int offset = chordLine.indexOf(cleanChordLine);
        if (offset < 0) offset = 0;
        
        Matcher matcher = CHORD_PATTERN.matcher(originalChordLine);

        while (matcher.find()) {
            String chordName = matcher.group(1);
            // Normalizar a mayúsculas (ej: "c" -> "C", "am" -> "Am")
            String normalizedChord = normalizeChordName(chordName);
            
            // La posición incluye los espacios iniciales de la línea original
            int startPosition = matcher.start();

            // Busca el acorde en el catálogo para obtener su ID (opcional pero recomendado)
            Long chordId = chordCatalogRepository.findByName(normalizedChord)
                    .map(ChordCatalog::getId)
                    .orElse(null);

            chords.add(new ChordPositionInfo(startPosition, normalizedChord, chordId));
        }

        return new LineWithChords(lineNumber, textLine, chords);
    }

    private String normalizeChordName(String chordName) {
        if (chordName == null || chordName.isEmpty()) {
            return chordName;
        }
        
        // Primera letra siempre mayúscula
        String normalized = chordName.substring(0, 1).toUpperCase();
        
        // Resto del acorde en minúscula (excepto números y símbolos)
        if (chordName.length() > 1) {
            normalized += chordName.substring(1).toLowerCase();
        }
        
        return normalized;
    }
}
