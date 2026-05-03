# Recipe Manager System
### Team Members
- James Nguyen: 806134391
- Toan Tran: 881738009

## Video Demo
- Link review video: [Google Drive]() <- TO BE ADDED SOON

## Instructions
- Ensure that both Docker Desktop and Postman are downloaded and running, at the same time.
- Either git clone OR download the zip file from this repo, and then unzip the project folder.
- Navigate to the project folder AND open Windows PowerShell within the folder.
- In Powershell, run the following command:\
• docker compose up --build
- In Postman, follow the API requests listed below, in exact order, to demo the full experience.
- For each API Request (*listed below*), the 3rd bullet point contains the JSON body, which you can plug into the "Body" tab on Postman.

## Below are the API requests needed for Postman testing:

### 1) POST /api/auth/register
- Scenario: Allen Key creates an account
- http://localhost:8080/api/auth/register
- {\
  "username": "AllenKey",\
  "email": "allenkey@csu.fullerton.edu",\
  "password": "hexwrench123",\
  "roles": ["USER"]\
  }

<img width="698" height="620" alt="Screenshot 2026-05-03 025255" src="https://github.com/user-attachments/assets/8d2fd556-c175-4c73-9aae-a51c1cac64f4" />

### 2) POST /api/auth/login
- Scenario: Allen Key logins into his account
- http://localhost:8080/api/auth/login
- {\
  "email": "allenkey@csu.fullerton.edu",\
  "password": "hexwrench123"\
  }

<img width="695" height="615" alt="Screenshot 2026-05-03 025447" src="https://github.com/user-attachments/assets/5732029c-8e86-4169-9227-d46e8a85c6ed" />

### 3) POST /api/recipes
- Scenario: Allen Key adds a Vegetable Stir Fry recipe
- http://localhost:8080/api/recipes
- {\
  "title": "Vegetable Stir Fry",\
  "description": "A quick and healthy meal with seasonal vegetables.",\
  "ingredients": ["Broccoli", "Carrots", "Bell Peppers", "Soy Sauce", "Ginger"],\
  "prepTime": 20,\
  "category": "Dinner"\
  }

<img width="697" height="772" alt="Screenshot 2026-05-03 025545" src="https://github.com/user-attachments/assets/dc2d5688-d089-48e5-9d70-e9bcafe673e4" />

### 4) GET /api/recipes
- Scenario: Allen Key views all of his recipes
- http://localhost:8080/api/recipes

<img width="696" height="808" alt="Screenshot 2026-05-03 025721" src="https://github.com/user-attachments/assets/9173d559-eeab-404e-b9bb-00595acbd209" />

### 5) POST /api/auth/register
- Scenario: John Wick creates an account
- http://localhost:8080/api/auth/register
- {\
  "username": "JohnWick",\
  "email": "johnwick@gmail.com",\
  "password": "theContinental#123",\
  "roles": ["USER"]\
  }

<img width="696" height="612" alt="Screenshot 2026-05-03 025922" src="https://github.com/user-attachments/assets/0047df63-53b4-4d64-8b66-3c7768249407" />

### 6) POST /api/auth/login
- Scenario: John Wick logins into his account
- http://localhost:8080/api/auth/login
- {\
  "email": "johnwick@gmail.com",\
  "password": "theContinental#123"\
  }

<img width="697" height="612" alt="Screenshot 2026-05-03 030036" src="https://github.com/user-attachments/assets/b0902bcf-1f0f-4173-ae7a-099c67b0fa88" />

### 7) POST /api/recipes
- Scenario: John Wicks adds a Lemon Herb Roasted Chicken recipe
- http://localhost:8080/api/recipes
- {\
  "title": "Lemon Herb Roasted Chicken",\
  "description": "Juicy chicken thighs roasted with lemon, garlic, and fresh herbs.",\
  "ingredients": ["Chicken Thighs", "Lemon", "Garlic", "Olive Oil", "Thyme", "Rosemary"],\
  "prepTime": 15,\
  "category": "Dinner"\
  }

<img width="695" height="810" alt="Screenshot 2026-05-03 030141" src="https://github.com/user-attachments/assets/699fbb59-76a3-4920-a709-fc77925c8daf" />

### 8) POST /api/recipes
- Scenario: John Wicks adds a Caprese Salad recipe
- http://localhost:8080/api/recipes
- {\
  "title": "Caprese Salad",\
  "description": "A fresh Italian salad with tomatoes, mozzarella, and basil.",\
  "ingredients": ["Tomatoes", "Mozzarella", "Basil", "Olive Oil", "Balsamic Glaze"],\
  "prepTime": 10,\
  "category": "Lunch"\
  }

<img width="697" height="769" alt="Screenshot 2026-05-03 030217" src="https://github.com/user-attachments/assets/0e5305d6-2f61-4a25-81a7-7e37e9d1a87a" />

### 9) GET /api/recipes
- Scenario: John Wick views all of his recipes
- http://localhost:8080/api/recipes

<img width="698" height="872" alt="Screenshot 2026-05-03 030330" src="https://github.com/user-attachments/assets/fca0a70c-0613-4ecb-83e8-49baaa344705" />

### 10) GET /api/recipes/{id}
- Scenario: John Wick views one of his recipes (Caprese Salad)
- http://localhost:8080/api/recipes/{id}

<img width="696" height="516" alt="Screenshot 2026-05-03 030425" src="https://github.com/user-attachments/assets/66a12467-1e06-4700-b34f-9e4e910610b5" />

### 11) PUT /api/recipes/{id}
- Scenario: John Wick updates one of his recipes (Caprese Salad)
- http://localhost:8080/api/recipes/{id}
- {\
  "title": "Caprese Salad (Version 2.0)",\
  "description": "An updated Italian salad with salt and pepper seasoning.",\
  "ingredients": ["Tomatoes", "Mozzarella", "Basil", "Olive Oil", "Salt", "Pepper"],\
  "prepTime": 5,\
  "category": "Snack"\
  }

<img width="699" height="690" alt="Screenshot 2026-05-03 030528" src="https://github.com/user-attachments/assets/022ef2ea-c824-41e4-965c-c325e3e7cfea" />

### 12) DELETE /api/recipes/{id}
- Scenario: John Wick deletes one of his recipes (Lemon Herb Roasted Chicken)
- http://localhost:8080/api/recipes/{id}

<img width="700" height="443" alt="Screenshot 2026-05-03 030649" src="https://github.com/user-attachments/assets/2cb99911-e58e-4d47-805c-2a24f9a68b98" />

*Note*: Verifying changes in GET /api/recipes (see picture below)\
• Lemon Herb Roasted Chicken recipe is no longer present\
• Only the updated Caprese Salad recipe remains (after Step 11)
<img width="697" height="573" alt="Screenshot 2026-05-03 030723" src="https://github.com/user-attachments/assets/615c82e9-ab0e-4b36-8f58-d96d213c0d1b" />
