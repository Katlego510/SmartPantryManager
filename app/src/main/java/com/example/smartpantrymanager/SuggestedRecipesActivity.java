package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.adapters.RecipeAdapter;
import com.example.smartpantrymanager.database.DatabaseHelper;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.util.NavHelper;
import com.example.smartpantrymanager.util.RecipeMatcher;

import java.util.List;

/** Suggested Recipes screen: runs the strict-matching logic against the current pantry. */
public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private RecyclerView rvRecipes;
    private TextView tvNoMatch, tvSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested);

        db = new DatabaseHelper(this);
        rvRecipes = findViewById(R.id.rvRecipes);
        tvNoMatch = findViewById(R.id.tvNoMatch);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        rvRecipes.setLayoutManager(new LinearLayoutManager(this));

        NavHelper.setup(this, R.id.nav_recipes);
    }

    /** Re-run the matching every time the screen is shown so it reflects pantry changes. */
    @Override
    protected void onResume() {
        super.onResume();
        List<Recipe> suggested = RecipeMatcher.getSuggestedRecipes(db.getAllItems(), db.getAllRecipes());

        rvRecipes.setAdapter(new RecipeAdapter(suggested, recipe -> {
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
            startActivity(intent);
        }));

        boolean none = suggested.isEmpty();
        tvNoMatch.setVisibility(none ? View.VISIBLE : View.GONE);
        tvSubtitle.setText(none ? "Nothing to cook yet"
                : "You can cook " + suggested.size() + " recipe(s) right now");
    }
}
