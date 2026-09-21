package com.example.recipemanager.repository;

import com.example.recipemanager.entity.RecipeFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecipeFavoriteRepository extends JpaRepository<RecipeFavorite, Long> {
    List<RecipeFavorite> findByUserId(Long userId);
    
    Optional<RecipeFavorite> findByUserIdAndRecipeId(Long userId, Long recipeId);
    
    @Query("SELECT COUNT(f) FROM RecipeFavorite f WHERE f.recipeId = :recipeId")
    Long countByRecipeId(@Param("recipeId") Long recipeId);
    
    void deleteByUserIdAndRecipeId(Long userId, Long recipeId);
}