package com.example.recipemanager.service;

import com.example.recipemanager.entity.Recipe;
import com.example.recipemanager.exception.ForbiddenException;
import com.example.recipemanager.exception.ResourceNotFoundException;
import com.example.recipemanager.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class RecipeService {
    @Autowired
    private RecipeRepository recipeRepo;

    // CREATE | creates recipe
    public Recipe createRecipe(Recipe recipe) {
        if (!StringUtils.hasText(recipe.getTitle())) {
            throw new IllegalArgumentException("Recipe title is required");
        }
        return recipeRepo.save(recipe);
    }

    // READ ALL | gets all recipes, for that user
    public List<Recipe> getAllRecipes(String userId) {
        return recipeRepo.findByUserId(userId);
    }

    // READ ONE | gets one recipe, if it belongs to the user
    public Recipe getOneRecipe(String recipeId, String userId) {
        Recipe recipe = recipeRepo.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe with id " + recipeId + " not found"));

        if (!recipe.getUserId().equals(userId)) {
            throw new ForbiddenException("You do not have permission to access this recipe");
        }

        return recipe;
    }

    // UPDATE | updates a recipe, if it belongs to the user
    public Recipe updateRecipe(String recipeId, String userId, Recipe recipe) {
        Recipe currentRecipe = getOneRecipe(recipeId, userId);

        if (!StringUtils.hasText(recipe.getTitle())) {
            throw new IllegalArgumentException("Recipe title is required");
        }

        currentRecipe.setTitle(recipe.getTitle());
        currentRecipe.setDescription(recipe.getDescription());
        currentRecipe.setIngredients(recipe.getIngredients());
        currentRecipe.setPrepTime(recipe.getPrepTime());
        currentRecipe.setCategory(recipe.getCategory());

        return recipeRepo.save(currentRecipe);
    }

    // DELETE | deletes a recipe, if it belongs to the user
    public void deleteRecipe(String recipeId, String userId) {
        Recipe recipe = getOneRecipe(recipeId, userId);
        recipeRepo.delete(recipe);
    }
}