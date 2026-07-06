package com.example.recipemanager.service;

import com.example.recipemanager.entity.Recipe;
import com.example.recipemanager.exception.ForbiddenException;
import com.example.recipemanager.exception.ResourceNotFoundException;
import com.example.recipemanager.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecipeService {
    @Autowired
    private RecipeRepository recipeRepo;

    // CREATE | creates recipe
    public Recipe createRecipe(Recipe recipe) {
        if (!StringUtils.hasText(recipe.getTitle())) {
            throw new IllegalArgumentException("Recipe title is required");
        }
        if (recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            throw new IllegalArgumentException("At least one ingredient is required");
        }
        return recipeRepo.save(recipe);
    }

    // READ ALL | gets all recipes, for that user
    public List<Recipe> getAllRecipes(String userId) {
        return getAllRecipes(userId, null, null, null, null, 0, 20);
    }

    public List<Recipe> getAllRecipes(String userId, String search, String category, Integer maxPrepTime, String ingredient, Integer page, Integer size) {
        List<Recipe> recipes = recipeRepo.findByUserId(userId).stream()
                .filter(recipe -> {
                    String title = recipe.getTitle() == null ? "" : recipe.getTitle().toLowerCase();
                    String description = recipe.getDescription() == null ? "" : recipe.getDescription().toLowerCase();
                    List<String> ingredients = recipe.getIngredients() == null ? List.of() : recipe.getIngredients();

                    boolean matchesSearch = !StringUtils.hasText(search)
                            || title.contains(search.toLowerCase())
                            || description.contains(search.toLowerCase());
                    boolean matchesCategory = !StringUtils.hasText(category)
                            || category.equalsIgnoreCase(recipe.getCategory());
                    boolean matchesPrepTime = maxPrepTime == null || recipe.getPrepTime() <= maxPrepTime;
                    boolean matchesIngredient = !StringUtils.hasText(ingredient)
                            || ingredients.stream().anyMatch(item -> item != null && item.toLowerCase().contains(ingredient.toLowerCase()));
                    return matchesSearch && matchesCategory && matchesPrepTime && matchesIngredient;
                })
                .sorted(Comparator.comparing(Recipe::getTitle))
                .collect(Collectors.toList());

        int safePage = page == null || page < 0 ? 0 : page;
        int safeSize = size == null || size <= 0 ? 20 : size;
        int fromIndex = safePage * safeSize;
        if (fromIndex >= recipes.size()) {
            return List.of();
        }
        return recipes.subList(fromIndex, Math.min(fromIndex + safeSize, recipes.size()));
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
        if (recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            throw new IllegalArgumentException("At least one ingredient is required");
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

    public Recipe uploadRecipeImage(String recipeId, String userId, MultipartFile file) {
        Recipe recipe = getOneRecipe(recipeId, userId);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are supported");
        }

        try {
            String imageBase64 = Base64.getEncoder().encodeToString(file.getBytes());
            recipe.setImageUrl("data:" + contentType + ";base64," + imageBase64);
            return recipeRepo.save(recipe);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read uploaded image", e);
        }
    }
}