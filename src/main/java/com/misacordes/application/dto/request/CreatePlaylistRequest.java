package com.misacordes.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlaylistRequest {
    private String name;
    private String description;
    private Boolean isPublic = false;
}
