package com.example.recipemanager.repository;

import com.example.recipemanager.entity.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("SELECT r FROM Recipe r WHERE r.userId = :userId " +
           "AND (:search IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:category IS NULL OR r.category = :category) " +
           "AND (:maxPrepTime IS NULL OR r.prepTime <= :maxPrepTime) " +
           "AND (:cuisine IS NULL OR r.cuisine = :cuisine) " +
           "AND (:difficulty IS NULL OR r.difficulty = :difficulty)")
    Page<Recipe> findByUserIdWithFilters(
        @Param("userId") Long userId,
        @Param("search") String search,
        @Param("category") String category,
        @Param("maxPrepTime") Integer maxPrepTime,
        @Param("cuisine") String cuisine,
        @Param("difficulty") String difficulty,
        Pageable pageable);

    @Query("SELECT r FROM Recipe r WHERE r.userId = :userId AND " +
           "(:tag IS NULL OR :tag IN (SELECT t FROM r.tags t))")
    List<Recipe> findByUserIdWithTag(@Param("userId") Long userId, @Param("tag") String tag);

    @Query("SELECT r FROM Recipe r WHERE r.userId = :userId ORDER BY r.createdAt DESC")
    List<Recipe> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);
}