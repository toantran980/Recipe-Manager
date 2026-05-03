package com.example.recipemanager.service;

import com.example.recipemanager.entity.Recipe;
import com.example.recipemanager.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecipeService {
    @Autowired
    private RecipeRepository recipeRepo;

    // CREATE | creates recipe
    public Recipe createRecipe(Recipe recipe) {
        return recipeRepo.save(recipe);
    }

    // READ ALL | gets all recipes, for that user
    public List<Recipe> getAllRecipes(String userId) {
        return recipeRepo.findByUserId(userId);
    }

    // READ ONE | gets one recipe, if it belongs to the user
    public Recipe getOneRecipe(String recipeId, String userId) {
        return recipeRepo.findByIdAndUserId(recipeId, userId)
                // 404, IF NOT FOUND or NOT OWNED BY USER
                .orElseThrow(() -> new RuntimeException("Recipe not found or unauthorized: " + recipeId));
    }

    // UPDATE | updates a recipe, if it belongs to the user
    public Recipe updateRecipe(String recipeId, String userId, Recipe recipe) {
        Recipe currentRecipe = getOneRecipe(recipeId, userId);  // 404 Unauthorized, IF NOT FOUND

        currentRecipe.setTitle(recipe.getTitle());
        currentRecipe.setDescription(recipe.getDescription());
        currentRecipe.setIngredients(recipe.getIngredients());
        currentRecipe.setPrepTime(recipe.getPrepTime());
        currentRecipe.setCategory(recipe.getCategory());

        return recipeRepo.save(currentRecipe);
    }

    // DELETE | deletes a recipe, if it belongs to the user
    public boolean deleteRecipe(String recipeId, String userId) {
        return recipeRepo.findByIdAndUserId(recipeId, userId)
                .map(recipe -> {
                    recipeRepo.delete(recipe);
                    return true;
                }).orElse(false);
    }
}
