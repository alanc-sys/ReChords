package com.misacordes.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongRequest {
    private String title;
    private String artist;
    private String album;
    private Integer year;
}
