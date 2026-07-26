# Deployment & Config Restructure Todo

- [x] Plan confirmed
- [x] Step 1: Restructure application.properties (env var defaults, Actuator config, `spring.profiles.active` from env)
- [x] Step 2: Fix application-dev.properties (fixed MongoDB URI, removed Redis refs)
- [x] Step 3: Create application-prod.properties (all values from env vars, in .gitignore)
- [x] Step 4: Add spring-boot-starter-actuator to pom.xml (also confirmed spring-boot-starter-data-redis is already there)
- [x] Step 5: Fix SecurityConfig.java to permit actuator health endpoints
- [x] Step 6: Fix RedisConfig.java - replace deprecated GenericJackson2JsonRedisSerializer with Jackson2JsonRedisSerializer
- [x] Step 7: Fix RecipeService.java - include userId in @Cacheable key to prevent cross-user cache poisoning (security bug)
- [x] Step 8: Fix LatencyBenchmark.java - remove unused `resp` local variable
- [x] Step 9: All changes complete - ready for build verification
