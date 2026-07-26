package com.example.recipemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class RecipemanagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecipemanagerApplication.class, args);
	}

}
