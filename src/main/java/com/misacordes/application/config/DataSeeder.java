package com.misacordes.application.config;

import com.misacordes.application.entities.ChordCatalog;
import com.misacordes.application.entities.ChordCategory;
import com.misacordes.application.entities.DifficultyLevel;
import com.misacordes.application.repositories.ChordCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ChordCatalogRepository chordCatalogRepository;

    @Override
    public void run(String... args) throws Exception {
        // Solo poblar si la tabla está vacía
        if (chordCatalogRepository.count() == 0) {
            seedChords();
        }
    }

    private void seedChords() {
        int order = 1;

        // ========== MAYORES (Comunes) ==========
        createChord("C", "Do mayor", ChordCategory.MAJOR, order++, true, DifficultyLevel.BEGINNER);
        createChord("D", "Re mayor", ChordCategory.MAJOR, order++, true, DifficultyLevel.BEGINNER);
        createChord("E", "Mi mayor", ChordCategory.MAJOR, order++, true, DifficultyLevel.BEGINNER);
        createChord("F", "Fa mayor", ChordCategory.MAJOR, order++, true, DifficultyLevel.INTERMEDIATE);
        createChord("G", "Sol mayor", ChordCategory.MAJOR, order++, true, DifficultyLevel.BEGINNER);
        createChord("A", "La mayor", ChordCategory.MAJOR, order++, true, DifficultyLevel.BEGINNER);
        createChord("B", "Si mayor", ChordCategory.MAJOR, order++, true, DifficultyLevel.INTERMEDIATE);

        // ========== MENORES (Comunes) ==========
        createChord("Am", "La menor", ChordCategory.MINOR, order++, true, DifficultyLevel.BEGINNER);
        createChord("Dm", "Re menor", ChordCategory.MINOR, order++, true, DifficultyLevel.BEGINNER);
        createChord("Em", "Mi menor", ChordCategory.MINOR, order++, true, DifficultyLevel.BEGINNER);
        createChord("Cm", "Do menor", ChordCategory.MINOR, order++, false, DifficultyLevel.INTERMEDIATE);
        createChord("Fm", "Fa menor", ChordCategory.MINOR, order++, false, DifficultyLevel.ADVANCED);
        createChord("Gm", "Sol menor", ChordCategory.MINOR, order++, false, DifficultyLevel.INTERMEDIATE);
        createChord("Bm", "Si menor", ChordCategory.MINOR, order++, true, DifficultyLevel.INTERMEDIATE);

        // ========== SÉPTIMAS (Comunes) ==========
        createChord("C7", "Do séptima", ChordCategory.SEVENTH, order++, true, DifficultyLevel.BEGINNER);
        createChord("D7", "Re séptima", ChordCategory.SEVENTH, order++, true, DifficultyLevel.BEGINNER);
        createChord("E7", "Mi séptima", ChordCategory.SEVENTH, order++, true, DifficultyLevel.BEGINNER);
        createChord("G7", "Sol séptima", ChordCategory.SEVENTH, order++, true, DifficultyLevel.BEGINNER);
        createChord("A7", "La séptima", ChordCategory.SEVENTH, order++, true, DifficultyLevel.BEGINNER);
        createChord("B7", "Si séptima", ChordCategory.SEVENTH, order++, false, DifficultyLevel.INTERMEDIATE);

        // ========== MAYORES SÉPTIMAS ==========
        createChord("Cmaj7", "Do mayor séptima", ChordCategory.MAJOR_SEVENTH, order++, false, DifficultyLevel.INTERMEDIATE);
        createChord("Dmaj7", "Re mayor séptima", ChordCategory.MAJOR_SEVENTH, order++, false, DifficultyLevel.INTERMEDIATE);
        createChord("Emaj7", "Mi mayor séptima", ChordCategory.MAJOR_SEVENTH, order++, false, DifficultyLevel.INTERMEDIATE);
        createChord("Gmaj7", "Sol mayor séptima", ChordCategory.MAJOR_SEVENTH, order++, false, DifficultyLevel.INTERMEDIATE);
        createChord("Amaj7", "La mayor séptima", ChordCategory.MAJOR_SEVENTH, order++, false, DifficultyLevel.INTERMEDIATE);

        // ========== MENORES SÉPTIMAS ==========
        createChord("Am7", "La menor séptima", ChordCategory.MINOR_SEVENTH, order++, false, DifficultyLevel.INTERMEDIATE);
        createChord("Dm7", "Re menor séptima", ChordCategory.MINOR_SEVENTH, order++, false, DifficultyLevel.INTERMEDIATE);
        createChord("Em7", "Mi menor séptima", ChordCategory.MINOR_SEVENTH, order++, false, DifficultyLevel.INTERMEDIATE);

        // ========== SUSPENDIDOS ==========
        createChord("Csus4", "Do suspendido cuarta", ChordCategory.SUSPENDED, order++, false, DifficultyLevel.BEGINNER);
        createChord("Dsus4", "Re suspendido cuarta", ChordCategory.SUSPENDED, order++, false, DifficultyLevel.BEGINNER);
        createChord("Esus4", "Mi suspendido cuarta", ChordCategory.SUSPENDED, order++, false, DifficultyLevel.BEGINNER);
        createChord("Gsus4", "Sol suspendido cuarta", ChordCategory.SUSPENDED, order++, false, DifficultyLevel.BEGINNER);
        createChord("Asus4", "La suspendido cuarta", ChordCategory.SUSPENDED, order++, false, DifficultyLevel.BEGINNER);

        // ========== ADD ==========
        createChord("Cadd9", "Do add9", ChordCategory.ADD, order++, false, DifficultyLevel.INTERMEDIATE);
        createChord("Dadd9", "Re add9", ChordCategory.ADD, order++, false, DifficultyLevel.INTERMEDIATE);
        createChord("Gadd9", "Sol add9", ChordCategory.ADD, order++, false, DifficultyLevel.INTERMEDIATE);

        System.out.println("✅ Catálogo de acordes inicializado con " + (order - 1) + " acordes");
    }

    private void createChord(String name, String fullName, ChordCategory category,
                             int displayOrder, boolean isCommon, DifficultyLevel difficulty) {
        ChordCatalog chord = ChordCatalog.builder()
                .name(name)
                .fullName(fullName)
                .category(category)
                .displayOrder(displayOrder)
                .isCommon(isCommon)
                .difficultyLevel(difficulty)
                .build();

        chordCatalogRepository.save(chord);
    }
}
