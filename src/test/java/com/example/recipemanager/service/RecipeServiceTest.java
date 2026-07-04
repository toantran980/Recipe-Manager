package com.example.recipemanager.service;

import com.example.recipemanager.entity.Recipe;
import com.example.recipemanager.exception.ForbiddenException;
import com.example.recipemanager.exception.ResourceNotFoundException;
import com.example.recipemanager.repository.RecipeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private RecipeService recipeService;

    @Test
    void createRecipeRequiresTitle() {
        Recipe recipe = new Recipe();
        recipe.setTitle("");

        assertThrows(IllegalArgumentException.class, () -> recipeService.createRecipe(recipe));
    }

    @Test
    void getAllRecipesReturnsRecipesForUser() {
        Recipe recipe = new Recipe();
        recipe.setUserId("user-1");
        when(recipeRepository.findByUserId("user-1")).thenReturn(List.of(recipe));

        List<Recipe> recipes = recipeService.getAllRecipes("user-1");

        assertEquals(1, recipes.size());
        assertEquals("user-1", recipes.get(0).getUserId());
    }

    @Test
    void getOneRecipeThrowsWhenRecipeMissing() {
        when(recipeRepository.findById("missing-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> recipeService.getOneRecipe("missing-id", "user-1"));
    }

    @Test
    void getOneRecipeThrowsWhenUserIsNotOwner() {
        Recipe recipe = new Recipe();
        recipe.setId("recipe-1");
        recipe.setUserId("other-user");

        when(recipeRepository.findById("recipe-1")).thenReturn(Optional.of(recipe));

        assertThrows(ForbiddenException.class, () -> recipeService.getOneRecipe("recipe-1", "user-1"));
    }

    @Test
    void updateRecipeSavesChangesWhenValid() {
        Recipe existing = new Recipe();
        existing.setId("recipe-1");
        existing.setUserId("user-1");
        existing.setTitle("Old");

        Recipe updated = new Recipe();
        updated.setTitle("New");
        updated.setDescription("Fresh");
        updated.setIngredients(List.of("Salt"));
        updated.setPrepTime(10);
        updated.setCategory("Lunch");

        when(recipeRepository.findById("recipe-1")).thenReturn(Optional.of(existing));
        when(recipeRepository.save(org.mockito.ArgumentMatchers.any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Recipe result = recipeService.updateRecipe("recipe-1", "user-1", updated);

        assertEquals("New", result.getTitle());
        assertEquals("Fresh", result.getDescription());
        verify(recipeRepository).save(org.mockito.ArgumentMatchers.any(Recipe.class));
    }
}
