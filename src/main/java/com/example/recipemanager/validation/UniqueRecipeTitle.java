package com.example.recipemanager.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueRecipeTitleValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueRecipeTitle {
    String message() default "Recipe title must be unique for this user";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}