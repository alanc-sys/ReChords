package com.misacordes.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddSongToPlaylistRequest {
    private Long songId;
    private Integer orderIndex; // Opcional, si no se especifica se añade al final
}
