package com.example.recipemanager.service;

import com.example.recipemanager.entity.Recipe;
import com.example.recipemanager.exception.ForbiddenException;
import com.example.recipemanager.exception.ResourceNotFoundException;
import com.example.recipemanager.repository.RecipeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RecipeService {
    @Autowired
    private RecipeRepository recipeRepo;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    // CREATE | creates recipe
    @CacheEvict(value = "recipes", allEntries = true)
    public Recipe createRecipe(Recipe recipe) {
        log.info("Creating recipe for user {}: {}", recipe.getUserId(), recipe.getTitle());
        if (!StringUtils.hasText(recipe.getTitle())) {
            throw new IllegalArgumentException("Recipe title is required");
        }
        if (recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            throw new IllegalArgumentException("At least one ingredient is required");
        }
        Recipe saved = recipeRepo.save(recipe);
        log.info("Recipe created successfully with ID: {}", saved.getId());
        return saved;
    }

    // READ ALL | gets all recipes, for that user
    public List<Recipe> getAllRecipes(Long userId) {
        return getAllRecipes(userId, null, null, null, null, null, null, 0, 20);
    }

    @Cacheable(value = "recipes", key = "#userId + ':' + (#search != null ? #search : '') + ':' + (#category != null ? #category : '') + ':' + (#sortBy != null ? #sortBy : '') + ':' + (#sortDirection != null ? #sortDirection : '')")
    public List<Recipe> getAllRecipes(Long userId, String search, String category, Integer maxPrepTime, String ingredient, String sortBy, String sortDirection, Integer page, Integer size) {
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
                .sorted(getComparator(sortBy, sortDirection))
                .collect(Collectors.toList());

        int safePage = page == null || page < 0 ? 0 : page;
        int safeSize = size == null || size <= 0 ? 20 : size;
        int fromIndex = safePage * safeSize;
        if (fromIndex >= recipes.size()) {
            return List.of();
        }
        return recipes.subList(fromIndex, Math.min(fromIndex + safeSize, recipes.size()));
    }

    private Comparator<Recipe> getComparator(String sortBy, String sortDirection) {
        boolean ascending = !"desc".equalsIgnoreCase(sortDirection);
        
        Comparator<Recipe> comparator = switch (sortBy == null ? "title" : sortBy.toLowerCase()) {
            case "preptime" -> Comparator.comparing(Recipe::getPrepTime);
            case "createdat" -> Comparator.comparing(Recipe::getCreatedAt, 
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "category" -> Comparator.comparing(Recipe::getCategory, 
                Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(Recipe::getTitle);
        };
        
        return ascending ? comparator : comparator.reversed();
    }

    // READ ONE | gets one recipe, if it belongs to the user
    @Cacheable(value = "recipes", key = "#recipeId")
    public Recipe getOneRecipe(Long recipeId, Long userId) {
        Recipe recipe = recipeRepo.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe with id " + recipeId + " not found"));

        if (!recipe.getUserId().equals(userId)) {
            throw new ForbiddenException("You do not have permission to access this recipe");
        }

        return recipe;
    }

    // UPDATE | updates a recipe, if it belongs to the user
    @CacheEvict(value = "recipes", allEntries = true)
    public Recipe updateRecipe(Long recipeId, Long userId, Recipe recipe) {
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
        currentRecipe.setCookingTime(recipe.getCookingTime());
        currentRecipe.setServings(recipe.getServings());
        currentRecipe.setDifficulty(recipe.getDifficulty());
        currentRecipe.setCuisine(recipe.getCuisine());
        currentRecipe.setInstructions(recipe.getInstructions());
        currentRecipe.setNutritionInfo(recipe.getNutritionInfo());
        currentRecipe.setCategory(recipe.getCategory());
        currentRecipe.setTags(recipe.getTags() != null ? recipe.getTags() : new ArrayList<>());

        return recipeRepo.save(currentRecipe);
    }

    // DELETE | deletes a recipe, if it belongs to the user
    @CacheEvict(value = "recipes", allEntries = true)
    public void deleteRecipe(Long recipeId, Long userId) {
        Recipe recipe = getOneRecipe(recipeId, userId);
        recipeRepo.delete(recipe);
    }

    @CacheEvict(value = "recipes", allEntries = true)
    public Recipe uploadRecipeImage(Long recipeId, Long userId, MultipartFile file) {
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

    @CacheEvict(value = "recipes", allEntries = true)
    public Recipe duplicateRecipe(Long recipeId, Long userId) {
        Recipe originalRecipe = getOneRecipe(recipeId, userId);
        
        Recipe duplicated = new Recipe();
        duplicated.setTitle(originalRecipe.getTitle() + " (Copy)");
        duplicated.setDescription(originalRecipe.getDescription());
        duplicated.setIngredients(new ArrayList<>(originalRecipe.getIngredients()));
        duplicated.setPrepTime(originalRecipe.getPrepTime());
        duplicated.setCookingTime(originalRecipe.getCookingTime());
        duplicated.setServings(originalRecipe.getServings());
        duplicated.setDifficulty(originalRecipe.getDifficulty());
        duplicated.setCuisine(originalRecipe.getCuisine());
        duplicated.setInstructions(originalRecipe.getInstructions());
        duplicated.setNutritionInfo(originalRecipe.getNutritionInfo());
        duplicated.setCategory(originalRecipe.getCategory());
        duplicated.setTags(originalRecipe.getTags() != null ? new ArrayList<>(originalRecipe.getTags()) : new ArrayList<>());
        duplicated.setUserId(userId);
        duplicated.setImageUrl(originalRecipe.getImageUrl());
        
        return recipeRepo.save(duplicated);
    }

    @CacheEvict(value = "recipes", allEntries = true)
    public void bulkDeleteRecipes(List<Long> recipeIds, Long userId) {
        if (recipeIds == null || recipeIds.isEmpty()) {
            throw new IllegalArgumentException("Recipe IDs list cannot be empty");
        }
        
        for (Long recipeId : recipeIds) {
            Recipe recipe = getOneRecipe(recipeId, userId);
            recipeRepo.delete(recipe);
        }
    }

    public String exportRecipe(Long recipeId, Long userId) {
        Recipe recipe = getOneRecipe(recipeId, userId);
        if (objectMapper == null) {
            throw new IllegalStateException("ObjectMapper not available for export");
        }
        try {
            return objectMapper.writeValueAsString(recipe);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to export recipe", e);
        }
    }

    public Recipe importRecipe(String recipeJson, Long userId) {
        if (objectMapper == null) {
            throw new IllegalStateException("ObjectMapper not available for import");
        }
        try {
            Recipe recipe = objectMapper.readValue(recipeJson, Recipe.class);
            recipe.setId(null); // Reset ID for new record
            recipe.setUserId(userId);
            recipe.setTitle(recipe.getTitle() + " (Imported)");
            return recipeRepo.save(recipe);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to import recipe", e);
        }
    }
}