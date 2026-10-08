package com.example.smartpantrymanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantrymanager.model.Item;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite database: pantry CRUD + seeded recipe collection.
 * Tables: pantry, recipes, recipe_ingredients (one recipe -> many ingredients).
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "pantry.db";
    private static final int DB_VERSION = 2;

    private static final String T_PANTRY = "pantry";
    private static final String T_RECIPES = "recipes";
    private static final String T_ING = "recipe_ingredients";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_PANTRY + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "expiry_date TEXT)");
        db.execSQL("CREATE TABLE " + T_RECIPES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "steps TEXT NOT NULL)");
        db.execSQL("CREATE TABLE " + T_ING + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "FOREIGN KEY(recipe_id) REFERENCES " + T_RECIPES + "(id) ON DELETE CASCADE)");
        seedRecipes(db); // pre-load recipes on first run
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + T_ING);
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + T_PANTRY);
        onCreate(db);
    }

    // ------------------------------------------------------------------
    // PANTRY CRUD
    // ------------------------------------------------------------------

    /** CREATE */
    public long addItem(Item item) {
        return getWritableDatabase().insert(T_PANTRY, null, toValues(item));
    }

    /** UPDATE */
    public int updateItem(Item item) {
        return getWritableDatabase().update(T_PANTRY, toValues(item),
                "id = ?", new String[]{String.valueOf(item.getId())});
    }

    /** DELETE */
    public int deleteItem(int id) {
        return getWritableDatabase().delete(T_PANTRY, "id = ?", new String[]{String.valueOf(id)});
    }

    /** READ one */
    public Item getItem(int id) {
        try (Cursor c = getReadableDatabase().query(T_PANTRY, null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (c.moveToFirst()) return itemFromCursor(c);
        }
        return null;
    }

    /** READ all */
    public List<Item> getAllItems() {
        List<Item> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(T_PANTRY, null, null, null,
                null, null, "name COLLATE NOCASE ASC")) {
            while (c.moveToNext()) list.add(itemFromCursor(c));
        }
        return list;
    }

    private ContentValues toValues(Item item) {
        ContentValues v = new ContentValues();
        v.put("name", item.getName());
        v.put("quantity", item.getQuantity());
        v.put("unit", item.getUnit());
        v.put("expiry_date", item.getExpiryDate());
        return v;
    }

    private Item itemFromCursor(Cursor c) {
        return new Item(
                c.getInt(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getDouble(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("unit")),
                c.getString(c.getColumnIndexOrThrow("expiry_date")));
    }

    // ------------------------------------------------------------------
    // RECIPES
    // ------------------------------------------------------------------

    public List<Recipe> getAllRecipes() {
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(T_RECIPES, null, null, null, null, null, "name ASC")) {
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndexOrThrow("id"));
                list.add(new Recipe(id,
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getString(c.getColumnIndexOrThrow("steps")),
                        getIngredientsFor(db, id)));
            }
        }
        return list;
    }

    public Recipe getRecipe(int id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(T_RECIPES, null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (c.moveToFirst()) {
                return new Recipe(id,
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getString(c.getColumnIndexOrThrow("steps")),
                        getIngredientsFor(db, id));
            }
        }
        return null;
    }

    private List<RecipeIngredient> getIngredientsFor(SQLiteDatabase db, int recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        try (Cursor c = db.query(T_ING, null, "recipe_id = ?",
                new String[]{String.valueOf(recipeId)}, null, null, "id ASC")) {
            while (c.moveToNext()) {
                list.add(new RecipeIngredient(
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getDouble(c.getColumnIndexOrThrow("quantity")),
                        c.getString(c.getColumnIndexOrThrow("unit"))));
            }
        }
        return list;
    }

    // ------------------------------------------------------------------
    // SEED DATA (18 recipes). Ingredient format: "name|quantity|unit"
    // ------------------------------------------------------------------

    private void addRecipe(SQLiteDatabase db, String name, String steps, String... ingredients) {
        ContentValues rv = new ContentValues();
        rv.put("name", name);
        rv.put("steps", steps);
        long recipeId = db.insert(T_RECIPES, null, rv);
        for (String s : ingredients) {
            String[] p = s.split("\\|");
            ContentValues iv = new ContentValues();
            iv.put("recipe_id", recipeId);
            iv.put("name", p[0]);
            iv.put("quantity", Double.parseDouble(p[1]));
            iv.put("unit", p[2]);
            db.insert(T_ING, null, iv);
        }
    }

    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Scrambled Eggs",
                "1. Whisk the eggs with salt.\n2. Melt butter in a pan on medium heat.\n3. Pour in the eggs and stir gently until just set.",
                "egg|2|pcs", "butter|10|g", "salt|1|tsp");
        addRecipe(db, "Cheese Omelette",
                "1. Whisk the eggs.\n2. Melt butter in a pan and pour in the eggs.\n3. Add grated cheese, fold and cook for 1 minute.",
                "egg|3|pcs", "cheese|50|g", "butter|10|g");
        addRecipe(db, "Pancakes",
                "1. Mix flour, sugar, egg and milk into a smooth batter.\n2. Rest for 5 minutes.\n3. Fry ladlefuls in a hot pan until golden on both sides.",
                "flour|200|g", "milk|300|ml", "egg|1|pcs", "sugar|1|tbsp");
        addRecipe(db, "Tomato Pasta",
                "1. Boil the pasta until tender.\n2. Fry chopped onion and garlic in oil, add chopped tomatoes and simmer 10 minutes.\n3. Toss the pasta through the sauce.",
                "pasta|200|g", "tomato|4|pcs", "onion|1|pcs", "garlic|2|pcs", "oil|1|tbsp");
        addRecipe(db, "Egg Fried Rice",
                "1. Fry chopped onion in oil.\n2. Push aside, scramble the eggs in the pan.\n3. Add cooked rice and stir-fry for 3 minutes.",
                "rice|200|g", "egg|2|pcs", "onion|1|pcs", "oil|2|tbsp");
        addRecipe(db, "Cheese Toastie",
                "1. Butter the bread on the outside.\n2. Fill with cheese.\n3. Toast in a pan until golden and melted.",
                "bread|4|pcs", "cheese|80|g", "butter|10|g");
        addRecipe(db, "French Toast",
                "1. Whisk eggs, milk and sugar.\n2. Soak the bread slices.\n3. Fry until golden on both sides.",
                "bread|4|pcs", "egg|2|pcs", "milk|100|ml", "sugar|1|tbsp");
        addRecipe(db, "Mashed Potatoes",
                "1. Peel and boil potatoes until soft.\n2. Drain and mash with butter.\n3. Stir in warm milk until creamy.",
                "potato|500|g", "butter|30|g", "milk|100|ml");
        addRecipe(db, "Chicken and Rice Bowl",
                "1. Fry onion in oil, add diced chicken and cook through.\n2. Add cooked rice and stir well.\n3. Serve hot.",
                "chicken|300|g", "rice|200|g", "onion|1|pcs", "oil|1|tbsp");
        addRecipe(db, "Tuna Pasta",
                "1. Boil the pasta.\n2. Fry onion in oil, add drained tuna.\n3. Toss with the pasta and serve.",
                "pasta|200|g", "tuna|1|pcs", "onion|1|pcs", "oil|1|tbsp");
        addRecipe(db, "Banana Pancakes",
                "1. Mash bananas and mix with eggs and flour.\n2. Fry small spoonfuls in a hot pan.\n3. Flip when bubbles appear.",
                "banana|2|pcs", "egg|2|pcs", "flour|100|g");
        addRecipe(db, "Vegetable Soup",
                "1. Chop carrots, potatoes and onion.\n2. Simmer in stock for 25 minutes.\n3. Blend or serve chunky.",
                "carrot|2|pcs", "potato|2|pcs", "onion|1|pcs", "stock|500|ml");
        addRecipe(db, "Oat Porridge",
                "1. Combine oats and milk in a pot.\n2. Cook on low heat, stirring, for 5 minutes.\n3. Sweeten with sugar.",
                "oats|80|g", "milk|300|ml", "sugar|1|tsp");
        addRecipe(db, "Bean Stew",
                "1. Fry onion in oil.\n2. Add chopped tomatoes and beans.\n3. Simmer for 20 minutes.",
                "bean|400|g", "tomato|2|pcs", "onion|1|pcs", "oil|1|tbsp");
        addRecipe(db, "Egg Noodle Stir-fry",
                "1. Boil the noodles and drain.\n2. Fry onion in oil, scramble in the eggs.\n3. Add the noodles and toss for 2 minutes.",
                "noodle|200|g", "egg|2|pcs", "onion|1|pcs", "oil|1|tbsp");
        addRecipe(db, "Garlic Bread",
                "1. Mash crushed garlic into softened butter.\n2. Spread over the bread.\n3. Bake at 180C for 8 minutes.",
                "bread|4|pcs", "butter|40|g", "garlic|3|pcs");
        addRecipe(db, "Chicken Stir-fry",
                "1. Slice chicken, pepper and onion.\n2. Fry chicken in oil until cooked.\n3. Add vegetables and stir-fry for 4 minutes.",
                "chicken|300|g", "bell pepper|1|pcs", "onion|1|pcs", "oil|1|tbsp");
        addRecipe(db, "Rice Pudding",
                "1. Combine rice, milk and sugar in a pot.\n2. Simmer on low for 30 minutes, stirring often.\n3. Serve warm.",
                "rice|100|g", "milk|500|ml", "sugar|2|tbsp");
    }
}
