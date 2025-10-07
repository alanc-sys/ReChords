package com.misacordes.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongResponse {
    private long id;
    private String title;
    private String artist;
    private String album;
    private Integer year;
    private LocalDateTime createdAt;
}
