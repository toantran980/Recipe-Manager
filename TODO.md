# Deployment & Config Restructure Todo

- [X] Plan confirmed
- [X] Step 1: Restructure application.properties (remove spring.profiles.active, add Actuator config)
- [X] Step 2: Fix application-dev.properties (fix MongoDB URI, remove Redis refs)
- [X] Step 3: Create application-prod.properties (all values from env vars)
- [X] Step 4: Add spring-boot-starter-actuator to pom.xml
- [X] Step 5: Update Dockerfile to set SPRING_PROFILES_ACTIVE=prod
- [X] Step 6: Update render.yaml with SPRING_PROFILES_ACTIVE=prod
- [X] Step 7: Update SecurityConfig to permit /actuator/health and /actuator/info
- [X] Step 8: Verify all changes
