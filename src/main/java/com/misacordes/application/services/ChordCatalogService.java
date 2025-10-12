package com.misacordes.application.services;

import com.misacordes.application.entities.ChordCatalog;
import com.misacordes.application.repositories.ChordCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChordCatalogService {

    private final ChordCatalogRepository chordCatalogRepository;

    @Transactional
    public void updateChordFingerPositions() {
        Map<String, String> chordPositions = getChordPositions();
        
        chordPositions.forEach((chordName, fingerPositions) -> {
            Optional<ChordCatalog> chordOpt = chordCatalogRepository.findByName(chordName);
            chordOpt.ifPresent(chord -> {
                chord.setFingerPositions(fingerPositions);
                chordCatalogRepository.save(chord);
            });
        });
    }

    private Map<String, String> getChordPositions() {
        Map<String, String> positions = new HashMap<>();
        
        // ========== MAYORES ==========
        positions.put("C", "x32010");
        positions.put("C#", "x46664");
        positions.put("D", "xx0232");
        positions.put("Eb", "x68886");
        positions.put("E", "022100");
        positions.put("F", "133211");
        positions.put("F#", "244322");
        positions.put("G", "320003");
        positions.put("Ab", "466544");
        positions.put("A", "x02220");
        positions.put("Bb", "x13331");
        positions.put("B", "x24442");
        
        // ========== MENORES ==========
        positions.put("Cm", "x35543");
        positions.put("C#m", "x46654");
        positions.put("Dm", "xx0231");
        positions.put("Ebm", "x68876");
        positions.put("Em", "022000");
        positions.put("Fm", "133111");
        positions.put("F#m", "244222");
        positions.put("Gm", "355333");
        positions.put("G#m", "466444");
        positions.put("Am", "x02210");
        positions.put("Bbm", "x13321");
        positions.put("Bm", "x24432");
        
        // ========== SÉPTIMAS ==========
        positions.put("C7", "x32310");
        positions.put("D7", "xx0212");
        positions.put("E7", "020100");
        positions.put("F7", "131211");
        positions.put("G7", "320001");
        positions.put("A7", "x02020");
        positions.put("B7", "x21202");
        
        // ========== MAYORES SÉPTIMAS ==========
        positions.put("Cmaj7", "x32000");
        positions.put("Dmaj7", "xx0222");
        positions.put("Emaj7", "021100");
        positions.put("Fmaj7", "x33210");
        positions.put("Gmaj7", "320002");
        positions.put("Amaj7", "x02120");
        positions.put("Bmaj7", "x24342");
        
        // ========== MENORES SÉPTIMAS ==========
        positions.put("Cm7", "x35343");
        positions.put("Dm7", "xx0211");
        positions.put("Em7", "020000");
        positions.put("Fm7", "131111");
        positions.put("Gm7", "353333");
        positions.put("Am7", "x02010");
        positions.put("Bm7", "x24232");
        
        // ========== SUSPENDIDOS ==========
        positions.put("Csus2", "x30010");
        positions.put("Csus4", "x33010");
        positions.put("Dsus2", "xx0230");
        positions.put("Dsus4", "xx0233");
        positions.put("Esus4", "022200");
        positions.put("Gsus2", "300033");
        positions.put("Gsus4", "330013");
        positions.put("Asus2", "x02200");
        positions.put("Asus4", "x02230");
        
        // ========== ADD9 ==========
        positions.put("Cadd9", "x32030");
        positions.put("Dadd9", "x54030");
        positions.put("Eadd9", "024100");
        positions.put("Gadd9", "320203");
        positions.put("Aadd9", "x02420");
        
        // ========== DISMINUIDOS ==========
        positions.put("Cdim", "x3424x");
        positions.put("Ddim", "xx0131");
        positions.put("Edim", "xx2323");
        positions.put("Fdim", "xx3434");
        positions.put("Gdim", "xx5656");
        
        // ========== AUMENTADOS ==========
        positions.put("Caug", "x32110");
        positions.put("Daug", "xx0332");
        positions.put("Eaug", "032110");
        positions.put("Gaug", "321003");
        positions.put("Aaug", "x03221");
        
        return positions;
    }
}

