package com.misacordes.application.config;

import com.misacordes.application.entities.ChordCatalog;
import com.misacordes.application.entities.ChordCategory;
import com.misacordes.application.entities.DifficultyLevel;
import com.misacordes.application.entities.Role;
import com.misacordes.application.entities.User;
import com.misacordes.application.repositories.ChordCatalogRepository;
import com.misacordes.application.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ChordCatalogRepository chordCatalogRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Crear usuario admin si no existe
        if (userRepository.findByUsername("admin") == null) {
            seedAdminUser();
        }
        
        // Solo poblar si la tabla está vacía
        if (chordCatalogRepository.count() == 0) {
            seedChords();
        } else {
            updateMissingFingerPositions();
            addChordVariations();
        }
    }

    private void updateMissingFingerPositions() {
        long acordesSinPosiciones = chordCatalogRepository.findAll().stream()
                .filter(chord -> chord.getFingerPositions() == null || chord.getFingerPositions().isEmpty())
                .count();
        
        if (acordesSinPosiciones > 0) {
            System.out.println("🎸 Actualizando posiciones de " + acordesSinPosiciones + " acordes...");
            
            // Usar el helper method existente para actualizar
            int orden = 1;
            
            // MAYORES
            updateChordIfExists("C", "x32010", orden++);
            updateChordIfExists("C#", "x46664", orden++);
            updateChordIfExists("D", "xx0232", orden++);
            updateChordIfExists("Eb", "x68886", orden++);
            updateChordIfExists("E", "022100", orden++);
            updateChordIfExists("F", "133211", orden++);
            updateChordIfExists("F#", "244322", orden++);
            updateChordIfExists("G", "320003", orden++);
            updateChordIfExists("Ab", "466544", orden++);
            updateChordIfExists("A", "x02220", orden++);
            updateChordIfExists("Bb", "x13331", orden++);
            updateChordIfExists("B", "x24442", orden++);
            
            // MENORES
            updateChordIfExists("Cm", "x35543", orden++);
            updateChordIfExists("C#m", "x46654", orden++);
            updateChordIfExists("Dm", "xx0231", orden++);
            updateChordIfExists("Ebm", "x68876", orden++);
            updateChordIfExists("Em", "022000", orden++);
            updateChordIfExists("Fm", "133111", orden++);
            updateChordIfExists("F#m", "244222", orden++);
            updateChordIfExists("Gm", "355333", orden++);
            updateChordIfExists("G#m", "466444", orden++);
            updateChordIfExists("Am", "x02210", orden++);
            updateChordIfExists("Bbm", "x13321", orden++);
            updateChordIfExists("Bm", "x24432", orden++);
            
            // SÉPTIMAS
            updateChordIfExists("C7", "x32310", orden++);
            updateChordIfExists("D7", "xx0212", orden++);
            updateChordIfExists("E7", "020100", orden++);
            updateChordIfExists("F7", "131211", orden++);
            updateChordIfExists("G7", "320001", orden++);
            updateChordIfExists("A7", "x02020", orden++);
            updateChordIfExists("B7", "x21202", orden++);
            
            // MAYORES SÉPTIMAS
            updateChordIfExists("Cmaj7", "x32000", orden++);
            updateChordIfExists("Dmaj7", "xx0222", orden++);
            updateChordIfExists("Emaj7", "021100", orden++);
            updateChordIfExists("Fmaj7", "x33210", orden++);
            updateChordIfExists("Gmaj7", "320002", orden++);
            updateChordIfExists("Amaj7", "x02120", orden++);
            updateChordIfExists("Bmaj7", "x24342", orden++);
            
            // MENORES SÉPTIMAS
            updateChordIfExists("Cm7", "x35343", orden++);
            updateChordIfExists("Dm7", "xx0211", orden++);
            updateChordIfExists("Em7", "020000", orden++);
            updateChordIfExists("Fm7", "131111", orden++);
            updateChordIfExists("Gm7", "353333", orden++);
            updateChordIfExists("Am7", "x02010", orden++);
            updateChordIfExists("Bm7", "x24232", orden++);
            
            // SUSPENDIDOS
            updateChordIfExists("Csus2", "x30010", orden++);
            updateChordIfExists("Csus4", "x33010", orden++);
            updateChordIfExists("Dsus2", "xx0230", orden++);
            updateChordIfExists("Dsus4", "xx0233", orden++);
            updateChordIfExists("Esus4", "022200", orden++);
            updateChordIfExists("Gsus2", "300033", orden++);
            updateChordIfExists("Gsus4", "330013", orden++);
            updateChordIfExists("Asus2", "x02200", orden++);
            updateChordIfExists("Asus4", "x02230", orden++);
            
            // ADD9
            updateChordIfExists("Cadd9", "x32030", orden++);
            updateChordIfExists("Dadd9", "x54030", orden++);
            updateChordIfExists("Eadd9", "024100", orden++);
            updateChordIfExists("Gadd9", "320203", orden++);
            updateChordIfExists("Aadd9", "x02420", orden++);
            
            // DISMINUIDOS
            updateChordIfExists("Cdim", "x3424x", orden++);
            updateChordIfExists("Ddim", "xx0131", orden++);
            updateChordIfExists("Edim", "xx2323", orden++);
            updateChordIfExists("Fdim", "xx3434", orden++);
            updateChordIfExists("Gdim", "xx5656", orden++);
            
            // AUMENTADOS
            updateChordIfExists("Caug", "x32110", orden++);
            updateChordIfExists("Daug", "xx0332", orden++);
            updateChordIfExists("Eaug", "032110", orden++);
            updateChordIfExists("Gaug", "321003", orden++);
            updateChordIfExists("Aaug", "x03221", orden++);
            
            // VARIACIONES DE ACORDES COMUNES
            addChordVariations();
            
            System.out.println("✅ Posiciones de acordes actualizadas exitosamente!");
        }
    }
    private void addChordVariations() {
        int orden = 1000; // Empezar desde 1000 para no conflictuar con los acordes principales
        
        // Variaciones de C
        createChordIfNotExists("C_var1", "Do mayor (Forma 2)", ChordCategory.MAJOR, orden++, false, DifficultyLevel.BEGINNER, "x35553");
        createChordIfNotExists("C_var2", "Do mayor (Forma 3)", ChordCategory.MAJOR, orden++, false, DifficultyLevel.INTERMEDIATE, "8-10-10-9-8-8");
        
        // Variaciones de G
        createChordIfNotExists("G_var1", "Sol mayor (Forma 2)", ChordCategory.MAJOR, orden++, false, DifficultyLevel.INTERMEDIATE, "355433");
        createChordIfNotExists("G_var2", "Sol mayor (Forma 3)", ChordCategory.MAJOR, orden++, false, DifficultyLevel.BEGINNER, "320033");
        
        // Variaciones de D
        createChordIfNotExists("D_var1", "Re mayor (Forma 2)", ChordCategory.MAJOR, orden++, false, DifficultyLevel.INTERMEDIATE, "x57775");
        createChordIfNotExists("D_var2", "Re mayor (Forma 3)", ChordCategory.MAJOR, orden++, false, DifficultyLevel.BEGINNER, "xx0235");
        
        // Variaciones de A
        createChordIfNotExists("A_var1", "La mayor (Forma 2)", ChordCategory.MAJOR, orden++, false, DifficultyLevel.INTERMEDIATE, "577655");
        
        // Variaciones de E
        createChordIfNotExists("E_var1", "Mi mayor (Forma 2)", ChordCategory.MAJOR, orden++, false, DifficultyLevel.INTERMEDIATE, "x79997");
        
        // Variaciones de F
        createChordIfNotExists("F_var1", "Fa mayor (Forma 2 - Sin cejilla)", ChordCategory.MAJOR, orden++, false, DifficultyLevel.BEGINNER, "xx3211");
        createChordIfNotExists("F_var2", "Fa mayor (Forma 3)", ChordCategory.MAJOR, orden++, false, DifficultyLevel.INTERMEDIATE, "x8-10-10-10-8");
        
        // Variaciones de Am
        createChordIfNotExists("Am_var1", "La menor (Forma 2)", ChordCategory.MINOR, orden++, false, DifficultyLevel.INTERMEDIATE, "577555");
        
        // Variaciones de Em
        createChordIfNotExists("Em_var1", "Mi menor (Forma 2)", ChordCategory.MINOR, orden++, false, DifficultyLevel.INTERMEDIATE, "x79987");
        
        // Variaciones de Dm
        createChordIfNotExists("Dm_var1", "Re menor (Forma 2)", ChordCategory.MINOR, orden++, false, DifficultyLevel.INTERMEDIATE, "x57765");
        
        long totalVariaciones = chordCatalogRepository.count() - 72; // 72 son los acordes base
        System.out.println("✅ Variaciones de acordes agregadas! Total variaciones: " + totalVariaciones);
    }
    
    /**
     * Crear un acorde solo si no existe
     */
    private void createChordIfNotExists(String name, String fullName, ChordCategory category,
                                        int displayOrder, boolean isCommon, DifficultyLevel difficulty,
                                        String fingerPositions) {
        if (chordCatalogRepository.findByName(name).isEmpty()) {
            ChordCatalog chord = ChordCatalog.builder()
                    .name(name)
                    .fullName(fullName)
                    .category(category)
                    .displayOrder(displayOrder)
                    .isCommon(isCommon)
                    .difficultyLevel(difficulty)
                    .fingerPositions(fingerPositions)
                    .build();
            
            chordCatalogRepository.save(chord);
        }
    }
    
    /**
     * Actualizar un acorde si existe
     */
    private void updateChordIfExists(String name, String fingerPositions, int displayOrder) {
        chordCatalogRepository.findByName(name).ifPresent(chord -> {
            if (chord.getFingerPositions() == null || chord.getFingerPositions().isEmpty()) {
                chord.setFingerPositions(fingerPositions);
                chord.setDisplayOrder(displayOrder);
                chordCatalogRepository.save(chord);
            }
        });
    }
    
    private void seedAdminUser() {
        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .firstname("Administrador")
                .lastname("Sistema")
                .country("ES")
                .role(Role.ADMIN)
                .build();
        
        userRepository.save(admin);
    }

    private void seedChords() {
        int order = 1;

        // ========== MAYORES (Comunes) ==========
        createChord("C", "Do mayor", ChordCategory.MAJOR, order++, true, DifficultyLevel.BEGINNER, "x32010");
        createChord("C#", "Do sostenido mayor", ChordCategory.MAJOR, order++, false, DifficultyLevel.INTERMEDIATE, "x46664");
        createChord("D", "Re mayor", ChordCategory.MAJOR, order++, true, DifficultyLevel.BEGINNER, "xx0232");
        createChord("Eb", "Mi bemol mayor", ChordCategory.MAJOR, order++, false, DifficultyLevel.INTERMEDIATE, "x68886");
        createChord("E", "Mi mayor", ChordCategory.MAJOR, order++, true, DifficultyLevel.BEGINNER, "022100");
        createChord("F", "Fa mayor", ChordCategory.MAJOR, order++, true, DifficultyLevel.INTERMEDIATE, "133211");
        createChord("F#", "Fa sostenido mayor", ChordCategory.MAJOR, order++, false, DifficultyLevel.INTERMEDIATE, "244322");
        createChord("G", "Sol mayor", ChordCategory.MAJOR, order++, true, DifficultyLevel.BEGINNER, "320003");
        createChord("Ab", "La bemol mayor", ChordCategory.MAJOR, order++, false, DifficultyLevel.INTERMEDIATE, "466544");
        createChord("A", "La mayor", ChordCategory.MAJOR, order++, true, DifficultyLevel.BEGINNER, "x02220");
        createChord("Bb", "Si bemol mayor", ChordCategory.MAJOR, order++, false, DifficultyLevel.INTERMEDIATE, "x13331");
        createChord("B", "Si mayor", ChordCategory.MAJOR, order++, true, DifficultyLevel.INTERMEDIATE, "x24442");

        // ========== MENORES (Comunes) ==========
        createChord("Cm", "Do menor", ChordCategory.MINOR, order++, true, DifficultyLevel.INTERMEDIATE, "x35543");
        createChord("C#m", "Do sostenido menor", ChordCategory.MINOR, order++, false, DifficultyLevel.INTERMEDIATE, "x46654");
        createChord("Dm", "Re menor", ChordCategory.MINOR, order++, true, DifficultyLevel.BEGINNER, "xx0231");
        createChord("Ebm", "Mi bemol menor", ChordCategory.MINOR, order++, false, DifficultyLevel.INTERMEDIATE, "x68876");
        createChord("Em", "Mi menor", ChordCategory.MINOR, order++, true, DifficultyLevel.BEGINNER, "022000");
        createChord("Fm", "Fa menor", ChordCategory.MINOR, order++, true, DifficultyLevel.ADVANCED, "133111");
        createChord("F#m", "Fa sostenido menor", ChordCategory.MINOR, order++, false, DifficultyLevel.INTERMEDIATE, "244222");
        createChord("Gm", "Sol menor", ChordCategory.MINOR, order++, true, DifficultyLevel.INTERMEDIATE, "355333");
        createChord("G#m", "Sol sostenido menor", ChordCategory.MINOR, order++, false, DifficultyLevel.INTERMEDIATE, "466444");
        createChord("Am", "La menor", ChordCategory.MINOR, order++, true, DifficultyLevel.BEGINNER, "x02210");
        createChord("Bbm", "Si bemol menor", ChordCategory.MINOR, order++, false, DifficultyLevel.INTERMEDIATE, "x13321");
        createChord("Bm", "Si menor", ChordCategory.MINOR, order++, true, DifficultyLevel.INTERMEDIATE, "x24432");

        // ========== SÉPTIMAS (Comunes) ==========
        createChord("C7", "Do séptima", ChordCategory.SEVENTH, order++, true, DifficultyLevel.BEGINNER, "x32310");
        createChord("D7", "Re séptima", ChordCategory.SEVENTH, order++, true, DifficultyLevel.BEGINNER, "xx0212");
        createChord("E7", "Mi séptima", ChordCategory.SEVENTH, order++, true, DifficultyLevel.BEGINNER, "020100");
        createChord("F7", "Fa séptima", ChordCategory.SEVENTH, order++, false, DifficultyLevel.INTERMEDIATE, "131211");
        createChord("G7", "Sol séptima", ChordCategory.SEVENTH, order++, true, DifficultyLevel.BEGINNER, "320001");
        createChord("A7", "La séptima", ChordCategory.SEVENTH, order++, true, DifficultyLevel.BEGINNER, "x02020");
        createChord("B7", "Si séptima", ChordCategory.SEVENTH, order++, true, DifficultyLevel.INTERMEDIATE, "x21202");

        // ========== MAYORES SÉPTIMAS ==========
        createChord("Cmaj7", "Do mayor séptima", ChordCategory.MAJOR_SEVENTH, order++, true, DifficultyLevel.INTERMEDIATE, "x32000");
        createChord("Dmaj7", "Re mayor séptima", ChordCategory.MAJOR_SEVENTH, order++, true, DifficultyLevel.INTERMEDIATE, "xx0222");
        createChord("Emaj7", "Mi mayor séptima", ChordCategory.MAJOR_SEVENTH, order++, true, DifficultyLevel.INTERMEDIATE, "021100");
        createChord("Fmaj7", "Fa mayor séptima", ChordCategory.MAJOR_SEVENTH, order++, false, DifficultyLevel.INTERMEDIATE, "x33210");
        createChord("Gmaj7", "Sol mayor séptima", ChordCategory.MAJOR_SEVENTH, order++, true, DifficultyLevel.INTERMEDIATE, "320002");
        createChord("Amaj7", "La mayor séptima", ChordCategory.MAJOR_SEVENTH, order++, true, DifficultyLevel.INTERMEDIATE, "x02120");
        createChord("Bmaj7", "Si mayor séptima", ChordCategory.MAJOR_SEVENTH, order++, false, DifficultyLevel.INTERMEDIATE, "x24342");

        // ========== MENORES SÉPTIMAS ==========
        createChord("Cm7", "Do menor séptima", ChordCategory.MINOR_SEVENTH, order++, false, DifficultyLevel.INTERMEDIATE, "x35343");
        createChord("Dm7", "Re menor séptima", ChordCategory.MINOR_SEVENTH, order++, true, DifficultyLevel.INTERMEDIATE, "xx0211");
        createChord("Em7", "Mi menor séptima", ChordCategory.MINOR_SEVENTH, order++, true, DifficultyLevel.INTERMEDIATE, "020000");
        createChord("Fm7", "Fa menor séptima", ChordCategory.MINOR_SEVENTH, order++, false, DifficultyLevel.INTERMEDIATE, "131111");
        createChord("Gm7", "Sol menor séptima", ChordCategory.MINOR_SEVENTH, order++, false, DifficultyLevel.INTERMEDIATE, "353333");
        createChord("Am7", "La menor séptima", ChordCategory.MINOR_SEVENTH, order++, true, DifficultyLevel.INTERMEDIATE, "x02010");
        createChord("Bm7", "Si menor séptima", ChordCategory.MINOR_SEVENTH, order++, false, DifficultyLevel.INTERMEDIATE, "x24232");

        // ========== SUSPENDIDOS ==========
        createChord("Csus2", "Do suspendido segunda", ChordCategory.SUSPENDED, order++, false, DifficultyLevel.BEGINNER, "x30010");
        createChord("Csus4", "Do suspendido cuarta", ChordCategory.SUSPENDED, order++, true, DifficultyLevel.BEGINNER, "x33010");
        createChord("Dsus2", "Re suspendido segunda", ChordCategory.SUSPENDED, order++, false, DifficultyLevel.BEGINNER, "xx0230");
        createChord("Dsus4", "Re suspendido cuarta", ChordCategory.SUSPENDED, order++, true, DifficultyLevel.BEGINNER, "xx0233");
        createChord("Esus4", "Mi suspendido cuarta", ChordCategory.SUSPENDED, order++, true, DifficultyLevel.BEGINNER, "022200");
        createChord("Gsus2", "Sol suspendido segunda", ChordCategory.SUSPENDED, order++, false, DifficultyLevel.BEGINNER, "300033");
        createChord("Gsus4", "Sol suspendido cuarta", ChordCategory.SUSPENDED, order++, true, DifficultyLevel.BEGINNER, "330013");
        createChord("Asus2", "La suspendido segunda", ChordCategory.SUSPENDED, order++, false, DifficultyLevel.BEGINNER, "x02200");
        createChord("Asus4", "La suspendido cuarta", ChordCategory.SUSPENDED, order++, true, DifficultyLevel.BEGINNER, "x02230");

        // ========== ADD9 ==========
        createChord("Cadd9", "Do add9", ChordCategory.ADD, order++, true, DifficultyLevel.INTERMEDIATE, "x32030");
        createChord("Dadd9", "Re add9", ChordCategory.ADD, order++, true, DifficultyLevel.INTERMEDIATE, "x54030");
        createChord("Eadd9", "Mi add9", ChordCategory.ADD, order++, false, DifficultyLevel.INTERMEDIATE, "024100");
        createChord("Gadd9", "Sol add9", ChordCategory.ADD, order++, true, DifficultyLevel.INTERMEDIATE, "320203");
        createChord("Aadd9", "La add9", ChordCategory.ADD, order++, false, DifficultyLevel.INTERMEDIATE, "x02420");

        // ========== DISMINUIDOS ==========
        createChord("Cdim", "Do disminuido", ChordCategory.DIMINISHED, order++, false, DifficultyLevel.ADVANCED, "x3424x");
        createChord("Ddim", "Re disminuido", ChordCategory.DIMINISHED, order++, false, DifficultyLevel.ADVANCED, "xx0131");
        createChord("Edim", "Mi disminuido", ChordCategory.DIMINISHED, order++, false, DifficultyLevel.ADVANCED, "xx2323");
        createChord("Fdim", "Fa disminuido", ChordCategory.DIMINISHED, order++, false, DifficultyLevel.ADVANCED, "xx3434");
        createChord("Gdim", "Sol disminuido", ChordCategory.DIMINISHED, order++, false, DifficultyLevel.ADVANCED, "xx5656");

        // ========== AUMENTADOS ==========
        createChord("Caug", "Do aumentado", ChordCategory.AUGMENTED, order++, false, DifficultyLevel.ADVANCED, "x32110");
        createChord("Daug", "Re aumentado", ChordCategory.AUGMENTED, order++, false, DifficultyLevel.ADVANCED, "xx0332");
        createChord("Eaug", "Mi aumentado", ChordCategory.AUGMENTED, order++, false, DifficultyLevel.ADVANCED, "032110");
        createChord("Gaug", "Sol aumentado", ChordCategory.AUGMENTED, order++, false, DifficultyLevel.ADVANCED, "321003");
        createChord("Aaug", "La aumentado", ChordCategory.AUGMENTED, order++, false, DifficultyLevel.ADVANCED, "x03221");
    }

    private void createChord(String name, String fullName, ChordCategory category,
                             int displayOrder, boolean isCommon, DifficultyLevel difficulty,
                             String fingerPositions) {
        ChordCatalog chord = ChordCatalog.builder()
                .name(name)
                .fullName(fullName)
                .category(category)
                .displayOrder(displayOrder)
                .isCommon(isCommon)
                .difficultyLevel(difficulty)
                .fingerPositions(fingerPositions)
                .build();

        chordCatalogRepository.save(chord);
    }
}
