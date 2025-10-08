package com.misacordes.application.repositories;

import com.misacordes.application.entities.ChordCatalog;
import com.misacordes.application.entities.ChordCategory;
import com.misacordes.application.entities.DifficultyLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChordCatalogRepository extends JpaRepository<ChordCatalog, Long> {
    Optional<ChordCatalog> findByName(String name);

    List<ChordCatalog> findAllByOrderByDisplayOrderAsc();

    List<ChordCatalog> findByIsCommonTrueOrderByDisplayOrderAsc();

    List<ChordCatalog> findByCategoryOrderByDisplayOrderAsc(ChordCategory category);

    List<ChordCatalog> findByDifficultyLevelOrderByDisplayOrderAsc(DifficultyLevel level);

    List<ChordCatalog> findByIsCommonTrueAndCategoryOrderByDisplayOrderAsc(ChordCategory category);
}
