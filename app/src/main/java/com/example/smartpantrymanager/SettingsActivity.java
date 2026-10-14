package com.example.smartpantrymanager;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.smartpantrymanager.util.NavHelper;

/** Settings screen: toggle for expiring-soon alerts (saved in SharedPreferences). */
public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS = "settings";
    public static final String KEY_EXPIRY_ALERTS = "expiry_alerts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        SwitchCompat sw = findViewById(R.id.swExpiryAlerts);
        sw.setChecked(prefs.getBoolean(KEY_EXPIRY_ALERTS, true));
        sw.setOnCheckedChangeListener((button, checked) ->
                prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, checked).apply());

        NavHelper.setup(this, R.id.nav_settings);
    }
}
