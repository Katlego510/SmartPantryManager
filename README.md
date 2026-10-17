# Smart Pantry Manager

A Java Android app that reduces food waste by tracking the ingredients in your pantry and suggesting
only the recipes you can cook right now (strict matching: every ingredient must be present in the
required quantity).

## Features
- Pantry management: add, edit, delete ingredients (name, quantity, unit, optional expiry date)
- 18 pre-loaded recipes (seeded on first run)
- Suggested Recipes screen using strict matching (handles singular/plural names and unit differences)
- Recipe detail screen (ingredients + method)
- Settings screen with expiring-soon alerts toggle
- Input validation and empty-state messages

## Database choice: SQLite
SQLite (via `SQLiteOpenHelper`) runs locally on the device, needs no server or internet, persists
data between app launches, and suits a small relational model (pantry, recipes, recipe_ingredients).

## Setup / Run
1. Clone the repository and open it in Android Studio (Giraffe or newer).
2. Let Gradle sync finish.
3. Select an emulator (e.g. Medium Phone, API 24+) or a physical device.
4. Press Run. The database and recipes are created automatically on first launch.

## Tech
Java, Android Studio, SQLite, RecyclerView with custom adapters, Intents, Material Components.
