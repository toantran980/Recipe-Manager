package com.example.recipemanager.validation;

import com.example.recipemanager.repository.RecipeRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class UniqueRecipeTitleValidator implements ConstraintValidator<UniqueRecipeTitle, String> {

    @Autowired
    private RecipeRepository recipeRepository;

    @Override
    public void initialize(UniqueRecipeTitle constraintAnnotation) {
    }

    @Override
    public boolean isValid(String title, ConstraintValidatorContext context) {
        if (title == null || title.trim().isEmpty()) {
            return true; // Let @NotBlank handle empty validation
        }

        return !recipeRepository.findByTitle(title).isPresent();
    }
}