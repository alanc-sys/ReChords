package com.misacordes.application.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "chord_catalog")
public class ChordCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 10)
    private String name;

    @Column(name = "full_name", length = 50)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChordCategory category;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_common")
    private Boolean isCommon;

    @Column(name = "difficulty_level")
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    @Lob
    @Column(name = "finger_positions", columnDefinition = "TEXT")
    private String fingerPositions;

    @Column(length = 200)
    private String notes;
}