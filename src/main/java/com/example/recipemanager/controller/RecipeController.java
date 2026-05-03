package com.example.recipemanager.controller;

import com.example.recipemanager.entity.Recipe;
import com.example.recipemanager.security.AuthUser;
import com.example.recipemanager.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    @Autowired
    private RecipeService recipeService;

    // CREATE endpoint
    // POST /api/recipes
    @PostMapping
    public ResponseEntity<Recipe> createRecipe(
            @RequestBody Recipe recipe,
            @AuthenticationPrincipal AuthUser authUser) {
        recipe.setUserId(authUser.getUserId());
        Recipe newRecipe = recipeService.createRecipe(recipe);
        return new ResponseEntity<>(newRecipe, HttpStatus.CREATED); // 201 CREATED
    }

    // READ ALL endpoint - Filtered by authenticated user
    // GET /api/recipes
    @GetMapping
    public ResponseEntity<List<Recipe>> getAllRecipes(@AuthenticationPrincipal AuthUser authUser) {
        List<Recipe> allRecipes = recipeService.getAllRecipes(authUser.getUserId());
        return ResponseEntity.ok(allRecipes);   // 200 OK
    }

    // READ ONE endpoint - Checks for user ownership
    // GET /api/recipes/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Recipe> getOneRecipe(
            @PathVariable("id") String recipeId,
            @AuthenticationPrincipal AuthUser authUser) {
        Recipe recipe = recipeService.getOneRecipe(recipeId, authUser.getUserId());
        return ResponseEntity.ok(recipe);   // 200 OK
    }

    // UPDATE endpoint - Checks for user ownership
    // PUT /api/recipes/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Recipe> updateRecipe(
            @PathVariable("id") String recipeId,
            @RequestBody Recipe recipe,
            @AuthenticationPrincipal AuthUser authUser) {
        Recipe updatedRecipe = recipeService.updateRecipe(recipeId, authUser.getUserId(), recipe);
        return ResponseEntity.ok(updatedRecipe);   // 200 OK
    }

    // DELETE endpoint - Checks for user ownership
    // DELETE /api/recipes/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(
            @PathVariable("id") String recipeId,
            @AuthenticationPrincipal AuthUser authUser) {
        boolean deletedStatus = recipeService.deleteRecipe(recipeId, authUser.getUserId());
        if (deletedStatus) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 NO CONTENT
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);  // 404 NOT FOUND
    }
}
