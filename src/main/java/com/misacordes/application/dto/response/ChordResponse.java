package com.misacordes.application.dto.response;

import com.misacordes.application.entities.ChordCategory;
import com.misacordes.application.entities.DifficultyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChordResponse {
    private Long id;
    private String name;
    private String fullName;
    private ChordCategory category;
    private Integer displayOrder;
    private Boolean isCommon;
    private DifficultyLevel difficultyLevel;
    private String fingerPositions;  // JSON opcional
}
