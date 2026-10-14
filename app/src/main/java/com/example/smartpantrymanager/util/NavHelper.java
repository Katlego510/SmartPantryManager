package com.example.smartpantrymanager.util;

import android.app.Activity;
import android.content.Intent;

import com.example.smartpantrymanager.MainActivity;
import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.SettingsActivity;
import com.example.smartpantrymanager.SuggestedRecipesActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Wires the bottom navigation bar to the three top-level screens using Intents. */
public class NavHelper {

    public static void setup(Activity activity, int selectedId) {
        BottomNavigationView nav = activity.findViewById(R.id.bottomNav);
        nav.setSelectedItemId(selectedId); // set BEFORE the listener so it doesn't fire
        nav.setOnItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == selectedId) return true;
            Class<?> target;
            if (id == R.id.nav_pantry) target = MainActivity.class;
            else if (id == R.id.nav_recipes) target = SuggestedRecipesActivity.class;
            else target = SettingsActivity.class;
            Intent intent = new Intent(activity, target);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            activity.startActivity(intent);
            return false;
        });
    }
}
