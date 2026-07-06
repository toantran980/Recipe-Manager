package com.example.recipemanager.repository;

import com.example.recipemanager.entity.Recipe;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends MongoRepository<Recipe, String> {
    List<Recipe> findByUserId(String userId);

    Optional<Recipe> findByIdAndUserId(String id, String userId);

    Optional<Recipe> findByTitle(String title);

    Optional<Recipe> findByDescription(String description);

    List<Recipe> findByIngredients(List<String> ingredients);

    Optional<Recipe> findByPrepTime(int prepTime);

    Optional<Recipe> findByCategory(String category);
}
