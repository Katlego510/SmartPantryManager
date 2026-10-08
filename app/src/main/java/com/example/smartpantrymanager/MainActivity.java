package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.adapters.ItemAdapter;
import com.example.smartpantrymanager.database.DatabaseHelper;
import com.example.smartpantrymanager.model.Item;
import com.example.smartpantrymanager.util.NavHelper;

import java.util.List;

/** Pantry List screen: shows every ingredient and lets the user add, edit and delete. */
public class MainActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private RecyclerView rvPantry;
    private TextView tvEmpty, tvBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        rvPantry = findViewById(R.id.rvPantry);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvBanner = findViewById(R.id.tvBanner);
        rvPantry.setLayoutManager(new LinearLayoutManager(this));

        // Intent: open the Add screen
        findViewById(R.id.fabAdd).setOnClickListener(v ->
                startActivity(new Intent(this, AddItemActivity.class)));

        NavHelper.setup(this, R.id.nav_pantry);
    }

    /** Reload from the database every time the screen becomes visible (after add/edit/delete). */
    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    private void loadPantry() {
        List<Item> items = db.getAllItems();
        boolean alerts = getSharedPreferences(SettingsActivity.PREFS, MODE_PRIVATE)
                .getBoolean(SettingsActivity.KEY_EXPIRY_ALERTS, true);

        rvPantry.setAdapter(new ItemAdapter(items, alerts, new ItemAdapter.Listener() {
            @Override
            public void onEdit(Item item) {
                // Intent with data: pass the item id to the edit screen
                Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
                intent.putExtra(AddItemActivity.EXTRA_ITEM_ID, item.getId());
                startActivity(intent);
            }

            @Override
            public void onDelete(Item item) {
                confirmDelete(item);
            }
        }));

        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);

        // Expiring-soon banner (controlled by the Settings toggle)
        int expiring = 0;
        if (alerts) {
            for (Item i : items) if (i.isExpiringSoon(3)) expiring++;
        }
        if (expiring > 0) {
            tvBanner.setText("Warning: " + expiring + " item(s) expired or expiring within 3 days");
            tvBanner.setVisibility(View.VISIBLE);
        } else {
            tvBanner.setVisibility(View.GONE);
        }
    }

    private void confirmDelete(Item item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage("Remove " + item.getName() + " from your pantry?")
                .setPositiveButton("Delete", (d, w) -> {
                    db.deleteItem(item.getId());
                    Toast.makeText(this, "Ingredient deleted", Toast.LENGTH_SHORT).show();
                    loadPantry();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
