package com.example.smartpantrymanager.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** A single pantry ingredient. */
public class Item {
    private final int id;
    private final String name;
    private final double quantity;
    private final String unit;
    private final String expiryDate; // "yyyy-MM-dd" or null

    public Item(int id, String name, double quantity, String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getExpiryDate() { return expiryDate; }

    /** 2.0 -> "2", 2.5 -> "2.5" */
    public static String fmt(double q) {
        return q == Math.floor(q) ? String.valueOf((long) q) : String.valueOf(q);
    }

    /** True if the item is already expired or expires within the given number of days. */
    public boolean isExpiringSoon(int days) {
        if (expiryDate == null || expiryDate.isEmpty()) return false;
        try {
            Date exp = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(expiryDate);
            long limit = System.currentTimeMillis() + days * 24L * 60 * 60 * 1000;
            return exp != null && exp.getTime() <= limit;
        } catch (ParseException e) {
            return false;
        }
    }
}
