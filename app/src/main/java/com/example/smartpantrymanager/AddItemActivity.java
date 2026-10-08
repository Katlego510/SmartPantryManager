package com.example.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantrymanager.database.DatabaseHelper;
import com.example.smartpantrymanager.model.Item;

import java.util.Calendar;
import java.util.Locale;

/** Add / Edit Ingredient screen (edit mode when an item id is passed in the Intent). */
public class AddItemActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "item_id";
    private static final String[] UNITS = {"pcs", "g", "kg", "ml", "l", "tsp", "tbsp", "cup"};

    private DatabaseHelper db;
    private EditText etName, etQuantity, etExpiry;
    private Spinner spUnit;
    private int editingId = -1; // -1 = adding a new item

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        db = new DatabaseHelper(this);
        TextView tvTitle = findViewById(R.id.tvFormTitle);
        etName = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        etExpiry = findViewById(R.id.etExpiry);
        spUnit = findViewById(R.id.spUnit);

        spUnit.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, UNITS));

        etExpiry.setOnClickListener(v -> showDatePicker());
        findViewById(R.id.btnClearDate).setOnClickListener(v -> etExpiry.setText(""));
        findViewById(R.id.btnSave).setOnClickListener(v -> saveItem());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());

        // Edit mode: load the existing record and pre-fill the form
        editingId = getIntent().getIntExtra(EXTRA_ITEM_ID, -1);
        if (editingId != -1) {
            Item item = db.getItem(editingId);
            if (item == null) {
                finish();
                return;
            }
            tvTitle.setText("Edit Ingredient");
            etName.setText(item.getName());
            etQuantity.setText(Item.fmt(item.getQuantity()));
            etExpiry.setText(item.getExpiryDate() == null ? "" : item.getExpiryDate());
            for (int i = 0; i < UNITS.length; i++) {
                if (UNITS[i].equals(item.getUnit())) spUnit.setSelection(i);
            }
        }
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, y, m, d) -> etExpiry.setText(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveItem() {
        String name = etName.getText().toString().trim();
        String qtyText = etQuantity.getText().toString().trim();
        String expiry = etExpiry.getText().toString().trim();
        String unit = spUnit.getSelectedItem().toString();

        // ---------- Input validation ----------
        boolean valid = true;
        if (name.isEmpty()) {
            etName.setError("Ingredient name is required");
            valid = false;
        } else if (!name.matches("[A-Za-z][A-Za-z \\-]*")) {
            etName.setError("Use letters, spaces and hyphens only");
            valid = false;
        }

        double quantity = 0;
        if (qtyText.isEmpty()) {
            etQuantity.setError("Quantity is required");
            valid = false;
        } else {
            try {
                quantity = Double.parseDouble(qtyText);
                if (quantity <= 0) {
                    etQuantity.setError("Quantity must be greater than 0");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                etQuantity.setError("Enter a valid number");
                valid = false;
            }
        }
        if (!valid) return;

        String expiryValue = expiry.isEmpty() ? null : expiry;
        if (editingId == -1) {
            db.addItem(new Item(0, name, quantity, unit, expiryValue));           // CREATE
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        } else {
            db.updateItem(new Item(editingId, name, quantity, unit, expiryValue)); // UPDATE
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
