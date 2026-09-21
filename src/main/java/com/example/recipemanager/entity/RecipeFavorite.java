package com.example.recipemanager.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "recipe_favorites", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"user_id", "recipe_id"}, name = "uk_user_recipe_favorite")
       },
       indexes = {
           @Index(name = "idx_favorite_user_id", columnList = "user_id"),
           @Index(name = "idx_favorite_recipe_id", columnList = "recipe_id")
       })
@Data
public class RecipeFavorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "recipe_id", nullable = false)
    private Long recipeId;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}