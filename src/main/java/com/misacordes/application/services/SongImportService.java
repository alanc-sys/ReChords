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
    private static final Pattern CHORD_PATTERN = Pattern.compile("\\b([A-G][b#]?(m|maj|dim|aug|sus)?[0-9]?[7]?(?![a-zA-Z]))\\b");

    public SongWithChordsRequest parse(String rawText) {
        // Verificar que el usuario esté autenticado
        User currentUser = getCurrentUser();
        
        SongWithChordsRequest request = new SongWithChordsRequest();
        List<LineWithChords> lyrics = new ArrayList<>();
        String[] lines = rawText.split("\\r?\\n");

        if (lines.length > 0) {
            String[] titleParts = lines[0].split("-");
            if (titleParts.length > 1) {
                request.setArtist(titleParts[0].trim());
                request.setTitle(titleParts[1].trim());
            } else {
                request.setTitle(lines[0].trim());
            }
        }

        int lineNumber = 0;
        for (int i = 0; i < lines.length; i++) {
            String currentLine = lines[i];

            // Ignorar líneas de tablatura y metadata por ahora
            if (isTabLine(currentLine) || isSectionHeader(currentLine) || currentLine.trim().isEmpty()) {
                // Simplemente lo añadimos como una línea de texto sin acordes
                lyrics.add(new LineWithChords(lineNumber++, currentLine, new ArrayList<>()));
                continue;
            }

            // 2. Lógica de emparejamiento de línea de acorde y letra
            if (isChordLine(currentLine)) {
                // Si la siguiente línea no es otra línea de acordes y no está vacía, es la letra
                if (i + 1 < lines.length && !isChordLine(lines[i+1]) && !lines[i+1].trim().isEmpty()) {
                    lyrics.add(processLinePair(lineNumber++, currentLine, lines[i+1]));
                    i++; // Avanzamos una línea extra porque ya la hemos procesado
                } else {
                    // Línea de acordes sin letra debajo (ej. en solos instrumentales)
                    lyrics.add(processLinePair(lineNumber++, currentLine, ""));
                }
            } else {
                // Línea de letra sin acordes
                lyrics.add(new LineWithChords(lineNumber++, currentLine, new ArrayList<>()));
            }
        }

        request.setLyrics(lyrics);
        request.setCreatedBy(currentUser.getId());
        return request;
    }

    private boolean isSectionHeader(String line) {
        return line.trim().startsWith("[") && line.trim().endsWith("]");
    }

    private boolean isTabLine(String line) {
        return line.matches("^[A-G#b|x\\-p/\\\\~b\\dr]+$");
    }

    private boolean isChordLine(String line) {
        String trimmedLine = line.trim();
        if (trimmedLine.isEmpty()) return false;

        Matcher matcher = CHORD_PATTERN.matcher(trimmedLine);
        int chordChars = 0;
        while(matcher.find()) {
            chordChars += matcher.group(0).length();
        }

        // Si más del 50% de los caracteres no-espacio son parte de acordes, es una línea de acordes.
        return (double) chordChars / trimmedLine.replaceAll("\\s+", "").length() > 0.5;
    }

    private LineWithChords processLinePair(int lineNumber, String chordLine, String textLine) {
        List<ChordPositionInfo> chords = new ArrayList<>();
        Matcher matcher = CHORD_PATTERN.matcher(chordLine);

        while (matcher.find()) {
            String chordName = matcher.group(1);
            int startPosition = matcher.start();

            // Busca el acorde en el catálogo para obtener su ID (opcional pero recomendado)
            Long chordId = chordCatalogRepository.findByName(chordName)
                    .map(ChordCatalog::getId)
                    .orElse(null);

            chords.add(new ChordPositionInfo(startPosition, chordName, chordId));
        }

        return new LineWithChords(lineNumber, textLine, chords);
    }
}
