package com.example.recipemanager.controller;

import com.example.recipemanager.entity.Recipe;
import com.example.recipemanager.service.RecipeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeControllerTest {

    @Mock
    private RecipeService recipeService;

    @InjectMocks
    private RecipeController recipeController;

    @Test
    void createRecipeReturnsCreatedStatusForValidPayload() {
        Recipe recipe = new Recipe();
        recipe.setId("recipe-1");
        recipe.setTitle("Soup");
        recipe.setDescription("A test recipe");
        recipe.setIngredients(List.of("salt"));
        recipe.setPrepTime(10);
        recipe.setCategory("Dinner");

        when(recipeService.createRecipe(any(Recipe.class))).thenReturn(recipe);

        ResponseEntity<Recipe> response = recipeController.createRecipe(
                new com.example.recipemanager.dto.RecipeRequest("Soup", "A test recipe", List.of("salt"), 10, "Dinner"),
                new com.example.recipemanager.security.AuthUser("user-1", "test@example.com")
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Soup", response.getBody().getTitle());
    }
}
