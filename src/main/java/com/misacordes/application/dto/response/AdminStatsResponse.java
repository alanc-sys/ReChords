package com.misacordes.application.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {
    private Long totalSongs;
    private Long draftSongs;
    private Long pendingSongs;
    private Long approvedSongs;
    private Long rejectedSongs;
    private Long totalUsers;
}