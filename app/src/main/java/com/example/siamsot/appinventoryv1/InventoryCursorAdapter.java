package com.example.siamsot.appinventoryv1;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class InventoryCursorAdapter extends CursorAdapter {

    private String LOG = "InventoryCursorAdapter";

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_template, parent, false);
    }

    /**
     * This method binds the inventory data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current item can be set on the name TextView
     * in the list item layout.
     *
     * @param rootView Existing view, returned earlier by newView() method
     * @param context  app context
     * @param cursor   The cursor from which to get the data. The cursor is already moved to the
     *                 correct row.
     */
    @Override
    public void bindView(final View rootView, final Context context, final Cursor cursor) {
        // Find fields to populate in inflated template
        TextView nameView = rootView.findViewById(R.id.name);
        TextView priceView = rootView.findViewById(R.id.price);
        final TextView qtyView = rootView.findViewById(R.id.qty);
        // Extract properties from cursor
        String name = cursor.getString(cursor.getColumnIndex("name"));
        String price = cursor.getString(cursor.getColumnIndex("price")) + "$";
        String quantity = cursor.getString(cursor.getColumnIndex("quantity"));

        // Populate fields with extracted properties
        nameView.setText(name);
        priceView.setText(price);
        qtyView.setText(quantity);

        final int currentQty = Integer.parseInt(cursor.getString(cursor.getColumnIndex("quantity")));
        //get the id
        final int id = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry._ID));
        Button decreaseB = rootView.findViewById(R.id.decrease);

        decreaseB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentQty > 0) {
                    ContentValues values = new ContentValues();
                    TextView currentQtyView = rootView.findViewById(R.id.qty);
                    Uri mCurrentItemUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);
                    int currentQty = Integer.parseInt(currentQtyView.getText().toString());
                    values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY, currentQty - 1);
                    int updatedRows = context.getContentResolver().update(mCurrentItemUri, values, null, null);
                    Log.d(LOG, "updatedRows: " + updatedRows);

                } else {
                    // can 't be reduced since value is already 0
                    Toast.makeText(context, "Already at zero, please add quantity", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}

