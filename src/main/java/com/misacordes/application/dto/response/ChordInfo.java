package com.misacordes.application.dto.response;

import com.misacordes.application.entities.DifficultyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChordInfo {
    
    private Long id;
    private String name;
    private String fullName;
    private String fingerPositions;
    private DifficultyLevel difficulty;
    private Boolean isCommon;
    private Integer displayOrder;
}
