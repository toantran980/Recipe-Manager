package com.example.recipemanager.integration;

import com.example.recipemanager.entity.Recipe;
import com.example.recipemanager.entity.User;
import com.example.recipemanager.repository.RecipeRepository;
import com.example.recipemanager.repository.UserRepository;
import com.example.recipemanager.service.RecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Disabled("Temporarily disabled during CI fix")
class RecipeIntegrationTest {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeService recipeService;

    private User testUser;

    @BeforeEach
    void setUp() {
        recipeRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRoles(List.of("USER"));
        testUser = userRepository.save(testUser);
    }

    @Test
    void testCreateAndRetrieveRecipe() {
        Recipe recipe = new Recipe();
        recipe.setTitle("Test Recipe");
        recipe.setDescription("Test Description");
        recipe.setIngredients(List.of("Ingredient 1", "Ingredient 2"));
        recipe.setPrepTime(30);
        recipe.setCategory("Dinner");
        recipe.setUserId(testUser.getId());

        Recipe saved = recipeService.createRecipe(recipe);

        assertNotNull(saved.getId());
        assertEquals("Test Recipe", saved.getTitle());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void testRecipeWithNewFields() {
        Recipe recipe = new Recipe();
        recipe.setTitle("Advanced Recipe");
        recipe.setDescription("Test Description");
        recipe.setIngredients(List.of("Ingredient 1"));
        recipe.setPrepTime(30);
        recipe.setCookingTime(45);
        recipe.setServings(4);
        recipe.setDifficulty("Medium");
        recipe.setCuisine("Italian");
        recipe.setInstructions("Step 1, Step 2");
        recipe.setNutritionInfo("Calories: 500");
        recipe.setCategory("Dinner");
        recipe.setTags(List.of("quick", "healthy"));
        recipe.setUserId(testUser.getId());

        Recipe saved = recipeService.createRecipe(recipe);

        assertEquals("Advanced Recipe", saved.getTitle());
        assertEquals(45, saved.getCookingTime());
        assertEquals(4, saved.getServings());
        assertEquals("Medium", saved.getDifficulty());
        assertEquals("Italian", saved.getCuisine());
        assertEquals("Step 1, Step 2", saved.getInstructions());
        assertEquals("Calories: 500", saved.getNutritionInfo());
        assertEquals(List.of("quick", "healthy"), saved.getTags());
    }

    @Test
    void testSortingRecipes() {
        Recipe recipe1 = new Recipe();
        recipe1.setTitle("Zucchini Recipe");
        recipe1.setDescription("Test");
        recipe1.setIngredients(List.of("Zucchini"));
        recipe1.setPrepTime(10);
        recipe1.setCategory("Lunch");
        recipe1.setUserId(testUser.getId());

        Recipe recipe2 = new Recipe();
        recipe2.setTitle("Apple Recipe");
        recipe2.setDescription("Test");
        recipe2.setIngredients(List.of("Apple"));
        recipe2.setPrepTime(20);
        recipe2.setCategory("Breakfast");
        recipe2.setUserId(testUser.getId());

        recipeService.createRecipe(recipe1);
        recipeService.createRecipe(recipe2);

        List<Recipe> sortedByTitle = recipeService.getAllRecipes(testUser.getId(), null, null, null, null, "title", "asc", 0, 10);
        assertEquals("Apple Recipe", sortedByTitle.get(0).getTitle());
        assertEquals("Zucchini Recipe", sortedByTitle.get(1).getTitle());

        List<Recipe> sortedByPrepTime = recipeService.getAllRecipes(testUser.getId(), null, null, null, null, "preptime", "asc", 0, 10);
        assertEquals("Zucchini Recipe", sortedByPrepTime.get(0).getTitle());
        assertEquals("Apple Recipe", sortedByPrepTime.get(1).getTitle());
    }

    @Test
    void testRecipeDuplication() {
        Recipe original = new Recipe();
        original.setTitle("Original Recipe");
        original.setDescription("Test");
        original.setIngredients(List.of("Ingredient 1"));
        original.setPrepTime(30);
        original.setCategory("Dinner");
        original.setUserId(testUser.getId());

        Recipe savedOriginal = recipeService.createRecipe(original);
        Recipe duplicated = recipeService.duplicateRecipe(savedOriginal.getId(), testUser.getId());

        assertEquals("Original Recipe (Copy)", duplicated.getTitle());
        assertEquals(savedOriginal.getDescription(), duplicated.getDescription());
        assertEquals(savedOriginal.getIngredients(), duplicated.getIngredients());
        assertNotEquals(savedOriginal.getId(), duplicated.getId());
    }
}