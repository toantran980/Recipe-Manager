package com.example.recipemanager.controller;

import com.example.recipemanager.dto.RecipeRequest;
import com.example.recipemanager.entity.Recipe;
import com.example.recipemanager.security.AuthUser;
import com.example.recipemanager.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@Tag(name = "Recipes", description = "Recipe management endpoints")
public class RecipeController {
    @Autowired
    private RecipeService recipeService;

    // CREATE endpoint
    // POST /api/recipes
    @Operation(summary = "Create a new recipe")
    @PostMapping
    public ResponseEntity<Recipe> createRecipe(
            @Valid @RequestBody RecipeRequest request,
            @AuthenticationPrincipal AuthUser authUser) {
        Recipe recipe = new Recipe();
        recipe.setTitle(request.title());
        recipe.setDescription(request.description());
        recipe.setIngredients(request.ingredients());
        recipe.setPrepTime(request.prepTime());
        recipe.setCategory(request.category());
        recipe.setUserId(authUser.getUserId());
        Recipe newRecipe = recipeService.createRecipe(recipe);
        return new ResponseEntity<>(newRecipe, HttpStatus.CREATED); // 201 CREATED
    }

    // READ ALL endpoint - Filtered by authenticated user
    // GET /api/recipes?search=...&category=...&maxPrepTime=...&ingredient=...&page=...&size=...
    @Operation(summary = "List recipes for the authenticated user")
    @GetMapping
    public ResponseEntity<List<Recipe>> getAllRecipes(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer maxPrepTime,
            @RequestParam(required = false) String ingredient,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        List<Recipe> allRecipes = recipeService.getAllRecipes(authUser.getUserId(), search, category, maxPrepTime, ingredient, page, size);
        return ResponseEntity.ok(allRecipes);   // 200 OK
    }

    // READ ONE endpoint - Checks for user ownership
    // GET /api/recipes/{id}
    @Operation(summary = "Fetch one recipe by id")
    @GetMapping("/{id}")
    public ResponseEntity<Recipe> getOneRecipe(
            @PathVariable("id") String recipeId,
            @AuthenticationPrincipal AuthUser authUser) {
        Recipe recipe = recipeService.getOneRecipe(recipeId, authUser.getUserId());
        return ResponseEntity.ok(recipe);   // 200 OK
    }

    // UPDATE endpoint - Checks for user ownership
    // PUT /api/recipes/{id}
    @Operation(summary = "Update an existing recipe")
    @PutMapping("/{id}")
    public ResponseEntity<Recipe> updateRecipe(
            @PathVariable("id") String recipeId,
            @Valid @RequestBody RecipeRequest request,
            @AuthenticationPrincipal AuthUser authUser) {
        Recipe recipe = new Recipe();
        recipe.setTitle(request.title());
        recipe.setDescription(request.description());
        recipe.setIngredients(request.ingredients());
        recipe.setPrepTime(request.prepTime());
        recipe.setCategory(request.category());
        Recipe updatedRecipe = recipeService.updateRecipe(recipeId, authUser.getUserId(), recipe);
        return ResponseEntity.ok(updatedRecipe);    // 200 OK
    }

    // DELETE endpoint - Checks for user ownership
    // DELETE /api/recipes/{id}
    @Operation(summary = "Delete a recipe")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(
            @PathVariable("id") String recipeId,
            @AuthenticationPrincipal AuthUser authUser) {
        recipeService.deleteRecipe(recipeId, authUser.getUserId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 NO CONTENT
    }

    @Operation(summary = "Upload an image for a recipe")
    @PostMapping("/{id}/image")
    public ResponseEntity<Recipe> uploadRecipeImage(
            @PathVariable("id") String recipeId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AuthUser authUser) {
        Recipe updatedRecipe = recipeService.uploadRecipeImage(recipeId, authUser.getUserId(), file);
        return ResponseEntity.ok(updatedRecipe);
    }
}