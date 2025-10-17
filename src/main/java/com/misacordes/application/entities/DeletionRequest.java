package com.misacordes.application.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "deletion_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeletionRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;
    
    @ManyToOne
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;
    
    @Column(length = 500)
    private String reason; // Razón opcional de por qué quiere eliminarla
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeletionStatus status; // PENDING, APPROVED, REJECTED
    
    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;
    
    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(length = 500)
    private String reviewComment; // Comentario del admin al aprobar/rechazar
    
    @PrePersist
    protected void onCreate() {
        requestedAt = LocalDateTime.now();
        if (status == null) {
            status = DeletionStatus.PENDING;
        }
    }
}


