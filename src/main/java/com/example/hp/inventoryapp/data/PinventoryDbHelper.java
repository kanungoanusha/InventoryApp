package com.example.hp.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PinventoryDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pinventory.db";
    private static final int DATABASE_VERSION = 1;

    public PinventoryDbHelper(Context context) { super(context, DATABASE_NAME, null, DATABASE_VERSION); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_PRODUCTS_TABLE = "CREATE TABLE " + PinventoryContract.PinventoryEntry.PRODUCTS_TABLE_NAME + "("
                + PinventoryContract.PinventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PinventoryContract.PinventoryEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + PinventoryContract.PinventoryEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                + PinventoryContract.PinventoryEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + PinventoryContract.PinventoryEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL + " TEXT NOT NULL, "
                + PinventoryContract.PinventoryEntry.COLUMN_PRODUCT_IMAGE + " BLOB NOT NULL);";

        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // It is safe now as we only have one database version
        db.execSQL(" DROP TABLE IF EXISTS " + PinventoryContract.PinventoryEntry.PRODUCTS_TABLE_NAME);
        onCreate(db);
    }
}

