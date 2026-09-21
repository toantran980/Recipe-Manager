package com.example.recipemanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipes", indexes = {
    @Index(name = "idx_recipe_user_id", columnList = "user_id"),
    @Index(name = "idx_recipe_category", columnList = "category"),
    @Index(name = "idx_recipe_title", columnList = "title"),
    @Index(name = "idx_recipe_created_at", columnList = "created_at"),
    @Index(name = "idx_recipe_cuisine", columnList = "cuisine"),
    @Index(name = "idx_recipe_difficulty", columnList = "difficulty")
})
@Data
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "recipe_ingredients", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "ingredient")
    private List<String> ingredients = new ArrayList<>();

    private int prepTime;

    private Integer cookingTime;

    private Integer servings;

    @Column(length = 20)
    private String difficulty;

    @Column(length = 50)
    private String cuisine;

    @Column(length = 5000)
    private String instructions;

    @Column(length = 1000)
    private String nutritionInfo;

    private String category;

    @ElementCollection
    @CollectionTable(name = "recipe_tags", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private String imageUrl;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}