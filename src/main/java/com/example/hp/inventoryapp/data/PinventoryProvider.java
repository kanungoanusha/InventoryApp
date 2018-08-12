package com.example.hp.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import static com.example.hp.inventoryapp.data.PinventoryContract.CONTENT_AUTHORITY;
import static com.example.hp.inventoryapp.data.PinventoryContract.PATH_PRODUCTS;

public class PinventoryProvider extends ContentProvider {

    public static final String LOG_TAG = PinventoryProvider.class.getSimpleName();

    private static final int PRODUCTS = 100;
    private static final int PRODUCT_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    private PinventoryDbHelper mDatabaseHelper;

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new PinventoryDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = database.query(PinventoryContract.PinventoryEntry.PRODUCTS_TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = PinventoryContract.PinventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(PinventoryContract.PinventoryEntry.PRODUCTS_TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values) {
        String productName = values.getAsString(PinventoryContract.PinventoryEntry.COLUMN_PRODUCT_NAME);
        if (productName == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        Integer price = values.getAsInteger(PinventoryContract.PinventoryEntry.COLUMN_PRODUCT_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Product requires valid price");
        }

        Integer quantity = values.getAsInteger(PinventoryContract.PinventoryEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Product requires valid quantity");
        }

        String emailOfSupplier = values.getAsString(PinventoryContract.PinventoryEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);
        if (emailOfSupplier == null) {
            throw new IllegalArgumentException("Item requires an email");
        }

        byte[] productImage = values.getAsByteArray(PinventoryContract.PinventoryEntry.COLUMN_PRODUCT_IMAGE);
        if (productImage == null) {
            throw new IllegalArgumentException("Product requires an image");
        }

        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();

        long id = database.insert(PinventoryContract.PinventoryEntry.PRODUCTS_TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                selection = PinventoryContract.PinventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(PinventoryContract.PinventoryEntry.COLUMN_PRODUCT_NAME)) {
            String productName = values.getAsString(PinventoryContract.PinventoryEntry.COLUMN_PRODUCT_NAME);
            if (productName == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        if (values.containsKey(PinventoryContract.PinventoryEntry.COLUMN_PRODUCT_PRICE)) {
            Integer price = values.getAsInteger(PinventoryContract.PinventoryEntry.COLUMN_PRODUCT_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Product requires valid price");
            }
        }

        if (values.containsKey(PinventoryContract.PinventoryEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer quantity = values.getAsInteger(PinventoryContract.PinventoryEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Product requires valid quantity");
            }
        }

        if (values.containsKey(PinventoryContract.PinventoryEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL)) {
            String emailOfSupplier = values.getAsString(PinventoryContract.PinventoryEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);
            if (emailOfSupplier == null) {
                throw new IllegalArgumentException("Item requires an email");
            }
        }

        if (values.containsKey(PinventoryContract.PinventoryEntry.COLUMN_PRODUCT_IMAGE)) {
            byte[] productImage = values.getAsByteArray(PinventoryContract.PinventoryEntry.COLUMN_PRODUCT_IMAGE);
            if (productImage == null) {
                throw new IllegalArgumentException("Product requires an image");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();

        int rowsUpdated = database.update(PinventoryContract.PinventoryEntry.PRODUCTS_TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                rowsDeleted = database.delete(PinventoryContract.PinventoryEntry.PRODUCTS_TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                selection = PinventoryContract.PinventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(PinventoryContract.PinventoryEntry.PRODUCTS_TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return PinventoryContract.PinventoryEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return PinventoryContract.PinventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}