package com.example.recipemanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"jwt.secret=test-secret-for-CI-with-32-chars-minimum!!"})
class RecipemanagerApplicationTests {

	@Test
	void contextLoads() {
	}

}
