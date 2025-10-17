package com.misacordes.application.dto.request;

import com.misacordes.application.entities.ChordCategory;
import com.misacordes.application.entities.DifficultyLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// import javax.validation.constraints.NotBlank; // Comentado temporalmente
// import javax.validation.constraints.NotNull; // Comentado temporalmente

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChordRequest {

    @NotBlank
    private String name;

    private String fullName;

    @NotNull(message = "La categoría es obligatoria")
    private ChordCategory category;

    private Integer displayOrder;

    private Boolean isCommon;

    private DifficultyLevel difficultyLevel;

    private String fingerPositions;

    private String notes;
}