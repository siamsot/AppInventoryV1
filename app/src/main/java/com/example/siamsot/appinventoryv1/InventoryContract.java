package com.example.siamsot.appinventoryv1;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * contract class that defines name of table and constants
 */
class InventoryContract {
    /**
     * CONTENT_AUTHORITY is used to help identify the Content Provider
     * which we’d setup before in the AndroidManifest tag
     */
    static final String CONTENT_AUTHORITY = "com.example.siamsot.appinventoryv1";
    /**
     * BASE_CONTENT will be shared by every URI associated with InventoryContract
     */
    static final Uri BASE_CONTENT_URI = Uri.parse("content://"+CONTENT_AUTHORITY);
    /**
     * This constants stores the path for each of the tables
     * which will be appended to the base content URI.
     */
    static final String PATH_ITEMS = "items";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private InventoryContract() {
    }

    /**
     * Inner class that defines constant values for the stock database table.
     * One inner class for each table created.
     */
    static abstract class InventoryEntry implements BaseColumns {
        /**
         * a full URI for the class as a constant called CONTENT_URI
         */
        static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);
        /**
         * Name of database table for items
         */
        static final String TABLE_NAME = "items";
        /**
         * Unique ID number for the item (only for use in the database table).
         * <p>
         * Type: INTEGER
         */
        static final String _ID = "_id";
        static final String COLUMN_ITEM_NAME = "name";
        static final String COLUMN_ITEM_PRICE = "price";
        static final String COLUMN_ITEM_QUANTITY = "quantity";
        static final String COLUMN_ITEM_IMAGE = "image";
        static final String COLUMN_SUPPLIER_NAME = "supplier_name";
        static final String COLUMN_SUPPLIER_PHONE = "supplier_phone";
        static final String COLUMN_SUPPLIER_MAIL = "supplier_mail";
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of items.
         */
        static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single item.
         */
        static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

    }
}

