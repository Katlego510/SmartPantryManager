package com.example.smartpantrymanager.util;

import com.example.smartpantrymanager.model.Item;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * STRICT-MATCHING RULE: a recipe is suggested only if EVERY ingredient it needs
 * is in the pantry in at least the required quantity. One missing/short ingredient
 * excludes the whole recipe.
 *
 * Robustness: names are case-insensitive and singularised ("Tomatoes" == "tomato"),
 * units are converted to a base unit (kg->g, l->ml, tbsp->ml ...), and duplicate
 * pantry rows for the same ingredient are added together.
 */
public class RecipeMatcher {

    /** Lower-case, trim and make singular so "Tomatoes" and "tomato" compare equal. */
    public static String normalizeName(String raw) {
        if (raw == null) return "";
        String s = raw.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        if (s.endsWith("ies") && s.length() > 4) return s.substring(0, s.length() - 3) + "y"; // berries -> berry
        if (s.endsWith("oes")) return s.substring(0, s.length() - 2);                          // tomatoes -> tomato
        if (s.endsWith("ches") || s.endsWith("shes") || s.endsWith("xes") || s.endsWith("sses"))
            return s.substring(0, s.length() - 2);
        if (s.endsWith("s") && !s.endsWith("ss")) return s.substring(0, s.length() - 1);      // eggs -> egg
        return s;
    }

    /** Map the many ways of writing a unit onto one canonical unit. */
    public static String normalizeUnit(String raw) {
        String u = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        switch (u) {
            case "g": case "gram": case "grams": case "gm": return "g";
            case "kg": case "kilogram": case "kilograms": return "kg";
            case "ml": case "milliliter": case "milliliters": case "millilitre": case "millilitres": return "ml";
            case "l": case "liter": case "liters": case "litre": case "litres": return "l";
            case "tsp": case "teaspoon": case "teaspoons": return "tsp";
            case "tbsp": case "tablespoon": case "tablespoons": return "tbsp";
            case "cup": case "cups": return "cup";
            default: return "pcs";
        }
    }

    /** Units can only be compared inside the same dimension (mass, volume, count). */
    private static String dimension(String unit) {
        switch (unit) {
            case "g": case "kg": return "mass";
            case "ml": case "l": case "tsp": case "tbsp": case "cup": return "volume";
            default: return "count";
        }
    }

    /** Convert to grams / millilitres / pieces. */
    private static double toBase(double qty, String unit) {
        switch (unit) {
            case "kg": case "l": return qty * 1000;
            case "tsp": return qty * 5;
            case "tbsp": return qty * 15;
            case "cup": return qty * 250;
            default: return qty;
        }
    }

    private static String key(String name, String unit) {
        return normalizeName(name) + "|" + dimension(unit);
    }

    /** Returns only the recipes the user can make right now. */
    public static List<Recipe> getSuggestedRecipes(List<Item> pantry, List<Recipe> recipes) {
        // 1. Total stock per ingredient (in base units)
        Map<String, Double> stock = new HashMap<>();
        for (Item item : pantry) {
            String unit = normalizeUnit(item.getUnit());
            String k = key(item.getName(), unit);
            Double current = stock.get(k);
            stock.put(k, (current == null ? 0 : current) + toBase(item.getQuantity(), unit));
        }
        // 2. Keep a recipe only if ALL of its ingredients are covered
        List<Recipe> result = new ArrayList<>();
        for (Recipe r : recipes) {
            if (canMake(r, stock)) result.add(r);
        }
        return result;
    }

    private static boolean canMake(Recipe recipe, Map<String, Double> stock) {
        if (recipe.getIngredients().isEmpty()) return false;
        for (RecipeIngredient ing : recipe.getIngredients()) {
            String unit = normalizeUnit(ing.getUnit());
            Double have = stock.get(key(ing.getName(), unit));
            double need = toBase(ing.getQuantity(), unit);
            if (have == null || have + 1e-9 < need) return false; // missing or not enough -> reject
        }
        return true;
    }
}
