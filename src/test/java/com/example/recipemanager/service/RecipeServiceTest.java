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
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
        recipe.setUserId(1L);
        when(recipeRepository.findByUserId(1L)).thenReturn(List.of(recipe));

        List<Recipe> recipes = recipeService.getAllRecipes(1L);

        assertEquals(1, recipes.size());
        assertEquals(1L, recipes.get(0).getUserId());
    }

    @Test
    void getOneRecipeThrowsWhenRecipeMissing() {
        when(recipeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> recipeService.getOneRecipe(999L, 1L));
    }

    @Test
    void getOneRecipeThrowsWhenUserIsNotOwner() {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setUserId(2L);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));

        assertThrows(ForbiddenException.class, () -> recipeService.getOneRecipe(1L, 1L));
    }

    @Test
    void updateRecipeSavesChangesWhenValid() {
        Recipe existing = new Recipe();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setTitle("Old");

        Recipe updated = new Recipe();
        updated.setTitle("New");
        updated.setDescription("Fresh");
        updated.setIngredients(List.of("Salt"));
        updated.setPrepTime(10);
        updated.setCategory("Lunch");

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(recipeRepository.save(org.mockito.ArgumentMatchers.any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Recipe result = recipeService.updateRecipe(1L, 1L, updated);

        assertEquals("New", result.getTitle());
        assertEquals("Fresh", result.getDescription());
        verify(recipeRepository).save(org.mockito.ArgumentMatchers.any(Recipe.class));
    }

    @Test
    void getAllRecipesSupportsSearchAndFiltering() {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setUserId(1L);
        recipe.setTitle("Vegetable Stir Fry");
        recipe.setCategory("Dinner");
        recipe.setPrepTime(20);
        recipe.setIngredients(List.of("Broccoli", "Soy Sauce"));

        when(recipeRepository.findByUserId(1L)).thenReturn(List.of(recipe));

        List<Recipe> recipes = recipeService.getAllRecipes(1L, "stir", "Dinner", 30, "soy", 0, 10);

        assertEquals(1, recipes.size());
        assertEquals("Vegetable Stir Fry", recipes.get(0).getTitle());
    }

    @Test
    void getAllRecipesSupportsPagination() {
        Recipe first = new Recipe();
        first.setId(1L);
        first.setUserId(1L);
        first.setTitle("First Recipe");

        Recipe second = new Recipe();
        second.setId(2L);
        second.setUserId(1L);
        second.setTitle("Second Recipe");

        when(recipeRepository.findByUserId(1L)).thenReturn(List.of(first, second));

        List<Recipe> recipes = recipeService.getAllRecipes(1L, null, null, null, null, 1, 1);

        assertEquals(1, recipes.size());
        assertEquals("Second Recipe", recipes.get(0).getTitle());
    }

    @Test
    void uploadRecipeImageStoresAUrlForTheRecipe() throws Exception {
        Recipe existing = new Recipe();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setTitle("Soup");

        MockMultipartFile file = new MockMultipartFile("file", "soup.png", "image/png", "image-bytes".getBytes());

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(recipeRepository.save(org.mockito.ArgumentMatchers.any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Recipe result = recipeService.uploadRecipeImage(1L, 1L, file);

        assertEquals(1L, result.getUserId());
        assertEquals("Soup", result.getTitle());
        assertTrue(result.getImageUrl() != null && !result.getImageUrl().isBlank());
        verify(recipeRepository).save(org.mockito.ArgumentMatchers.any(Recipe.class));
    }
}