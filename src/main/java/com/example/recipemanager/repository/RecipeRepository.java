package com.example.recipemanager.repository;

import com.example.recipemanager.entity.Recipe;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends MongoRepository<Recipe, String> {
    // find by Recipe's userID
    List<Recipe> findByUserId(String userId);

    // find by User's id and Recipe's userID
    Optional<Recipe> findByIdAndUserId(String id, String userId);

    // find by title
    Optional<Recipe> findByTitle(String title);

    // find by description
    Optional<Recipe> findByDescription(String description);

    // find by ingredients
    List<Recipe> findByIngredients(List<String> ingredients);

    // find by prep time
    Optional<Recipe> findByPrepTime(int prepTime);

    // find by category
    Optional<Recipe> findByCategory(String category);
}
