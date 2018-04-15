package com.example.siamsot.appinventoryv1;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * {@link ContentProvider} for Inventory app.
 */
public class InventoryProvider extends ContentProvider {
    private InventoryDbHelper mDbHelper;

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the items table
     */
    private static final int ITEMS = 100;

    /**
     * URI matcher code for the content URI for a single item in the items table
     */
    private static final int ITEM_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ITEMS, ITEMS);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ITEMS + "/#", ITEM_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                /**For the ITEMS code, query the stock table directly with the given
                 * projection, selection, selection arguments, and sort order. The cursor
                 * could contain multiple rows of the pets table.
                 */
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case ITEM_ID:

                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // Set notification URI on the Cursor
        // so we know what content URI the Cursor was created for.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                /**
                 * Sanity checks/ Data validation!
                 */
                String name = contentValues.getAsString(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME);
                if (name == null) {
                    throw new IllegalArgumentException("Item requires a name");
                }
                Double price = contentValues.getAsDouble(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE);
                if (price < 0) {
                    throw new IllegalArgumentException("Item's price must be bigger than 0");
                }
                int qty = contentValues.getAsInteger(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY);
                if (qty <= 0) {
                    throw new IllegalArgumentException("Item's quantity must be greater than 0");
                }
                String supplier_name = contentValues.getAsString(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME);
                if (supplier_name == null) {
                    throw new IllegalArgumentException("Item requires a supplier's name");
                }
                String supplier_phone = contentValues.getAsString(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_PHONE);
                if (supplier_phone == null) {
                    throw new IllegalArgumentException("Item requires a supplier's phone");
                }
                String supplier_mail = contentValues.getAsString(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_MAIL);
                if (supplier_mail == null) {
                    throw new IllegalArgumentException("Item requires a supplier's mail");
                }

                /**
                 * Insert an item into the database with the given content values. Return the new content URI
                 * for that specific row in the database.
                 */
                // Create and/or open a database to read from it
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                long id = db.insert(InventoryContract.InventoryEntry.TABLE_NAME, null, contentValues);
                // If the ID is -1, then the insertion failed. Log an error and return null.
                if (id == -1) {
                    Log.e(LOG_TAG, "Failed to insert row for " + uri);
                    return null;
                }

                // Notify all listeners that the data has changed
                // if observer is null, the cursor adapter gets notified.
                getContext().getContentResolver().notifyChange(uri, null);

                // Once we know the ID of the new row in the table,
                // return the new URI with the ID appended to the end of it
                return ContentUris.withAppendedId(uri, id);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case ITEM_ID:
                // For the ITEM_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }
    /**
     * Update items in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more items).
     * Return the number of rows that were successfully updated.
     */
    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        /**
         * Sanity checks/ Data validation!
         */
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME)) {
            String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Item requires a name");
            }
        }

        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE)) {
            int price = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE);
            if (price < 0) {
                throw new IllegalArgumentException("Item's price must be bigger than 0");
            }
        }
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY)) {
            int qty = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY);
            if (qty < 0) {
                throw new IllegalArgumentException("Item's quantity must be greater than 0");
            }
        }

        // image can be null

        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME)) {
            String supplier_name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME);
            if (supplier_name == null) {
                throw new IllegalArgumentException("Item requires a supplier's name");
            }
        }
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_PHONE)) {
            String supplier_phone = values.getAsString(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_PHONE);
            if (supplier_phone == null) {
                throw new IllegalArgumentException("Item requires a supplier's phone");
            }
        }
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_MAIL)) {
            String supplier_mail = values.getAsString(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_MAIL);
            if (supplier_mail == null) {
                throw new IllegalArgumentException("Item requires a supplier's mail");
            }
        }

        if (values.size() == 0) { return 0; }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(InventoryContract.InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            // Notify all listeners that the data has changed
            // if observer is null, the cursor adapter gets notified.
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }


    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // Delete all rows that match the selection and selection args
                // Notify all listeners that the data has changed
                // if observer is null, the cursor adapter gets notified.
                getContext().getContentResolver().notifyChange(uri, null);
                int rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                // If 1 or more rows were deleted, then notify all listeners that the data at the
                // given URI has changed
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                // Return the number of rows deleted
                return rowsDeleted;
            case ITEM_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                // Notify all listeners that the data has changed
                // if observer is null, the cursor adapter gets notified.
                getContext().getContentResolver().notifyChange(uri, null);
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                // If 1 or more rows were deleted, then notify all listeners that the data at the
                // given URI has changed
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                // Return the number of rows deleted
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }
    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return InventoryContract.InventoryEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return InventoryContract.InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}

