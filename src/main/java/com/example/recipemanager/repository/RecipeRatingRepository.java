package com.example.recipemanager.repository;

import com.example.recipemanager.entity.RecipeRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecipeRatingRepository extends JpaRepository<RecipeRating, Long> {
    List<RecipeRating> findByRecipeId(Long recipeId);
    
    Optional<RecipeRating> findByRecipeIdAndUserId(Long recipeId, Long userId);
    
    @Query("SELECT AVG(r.rating) FROM RecipeRating r WHERE r.recipe.id = :recipeId")
    Double getAverageRatingForRecipe(@Param("recipeId") Long recipeId);
    
    List<RecipeRating> findByUserId(Long userId);
}