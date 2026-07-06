package com.example.recipemanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RecipeRequest(
        @NotBlank(message = "Recipe title is required")
        @Size(max = 100, message = "Title must be 100 characters or fewer")
        String title,

        @NotBlank(message = "Description is required")
        @Size(max = 1000, message = "Description must be 1000 characters or fewer")
        String description,

        @NotEmpty(message = "At least one ingredient is required")
        List<@NotBlank String> ingredients,

        @PositiveOrZero(message = "Prep time must be 0 or greater")
        int prepTime,

        @NotBlank(message = "Category is required")
        @Size(max = 50, message = "Category must be 50 characters or fewer")
        String category
) {
}
