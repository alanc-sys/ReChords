package com.misacordes.application.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "songs")
public class Song {

        @Id
        @GeneratedValue
        private long id;
        @Column(nullable = false)
        private String title;
        private String artist;
        private String album;
        private Integer year;
        @ManyToOne
        @JoinColumn(name="user_id", nullable = false)
        private User user;
        @Column(name="created_at")
        private LocalDateTime createdAt;

        @PrePersist
        public void onCreated(){
            createdAt = LocalDateTime.now();
        }
}
