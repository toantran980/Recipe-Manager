package com.example.recipemanager.repository;

import com.example.recipemanager.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByUserId(Long userId);

    Optional<Recipe> findByIdAndUserId(Long id, Long userId);

    Optional<Recipe> findByTitle(String title);

    Optional<Recipe> findByDescription(String description);

    List<Recipe> findByIngredientsContaining(String ingredient);

    Optional<Recipe> findByPrepTime(int prepTime);

    List<Recipe> findByCategory(String category);
}