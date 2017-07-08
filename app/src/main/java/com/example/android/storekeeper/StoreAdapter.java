package com.example.android.storekeeper;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.example.android.storekeeper.data.StoreContract.ItemEntry;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * {@link StoreAdapter} is a {@link CursorAdapter} enabling the cursor to populate the views with the
 * retrieved data.
 */
public class StoreAdapter extends StoreHolder<StoreAdapter.ViewHolder> {

    private Context ctxt;

    @BindView(R.id.image_catalog)
    ImageView image_holder;
    @BindView(R.id.name_catalog)
    TextView name_holder;
    @BindView(R.id.price_catalog)
    TextView price_holder;
    @BindView(R.id.quantity_catalog)
    TextView quantity_holder;
    @BindView(R.id.description_catalog)
    TextView description_holder;
    @BindView(R.id.holder_catalog)
    RelativeLayout item_holder;

    /**
     * constructor
     *
     * @param ctxt   of the app
     * @param cursor accessing the database
     */
    public StoreAdapter(Context ctxt, Cursor cursor) {
        super((CatalogActivity) ctxt, cursor);
        this.ctxt = ctxt;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    /**
     * Create new ViewHolder
     *
     * @param parent   the context where in it will be created
     * @param viewType needn't be defined here
     * @return view Holder.
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item,
                parent, false);
        ViewHolder viewHolder = new ViewHolder(itemView);
        ButterKnife.bind(this, itemView);// initialise the views
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor crs) {

        final long id = crs.getLong(crs.getColumnIndex(ItemEntry._ID));

        //get column index
        int imageColumn = crs.getColumnIndex(ItemEntry.ITM_IMAGE);
        int nameColumn = crs.getColumnIndex(ItemEntry.ITM_NAME);
        int priceColumn = crs.getColumnIndex(ItemEntry.ITM_PRICE);
        int quantityColumn = crs.getColumnIndex(ItemEntry.ITM_QUANTITY);
        int descriptionColumn = crs.getColumnIndex(ItemEntry.ITM_DESCRIPTION);

        //retrieve data
        String imageData = crs.getString(imageColumn);
        String nameData = crs.getString(nameColumn);
        double priceRawData = crs.getDouble(priceColumn);
        int quantityRawData = crs.getInt(quantityColumn);
        String descriptionData = crs.getString(descriptionColumn);

        //Prepare price for display
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
        String priceData = ctxt.getString(R.string.price) + Currency.getInstance(Locale.getDefault())
                + ctxt.getString(R.string.spacer) + formatter.format(priceRawData);
        
        //prepare quantity for display
        String quantityData = ctxt.getString(R.string.in_stock) + quantityRawData;

        //set data to views
        name_holder.setText(nameData);
        price_holder.setText(priceData);
        quantity_holder.setText(String.valueOf(quantityData));
        description_holder.setText(descriptionData);

        //display the image based on the format of its data.
        if (imageData.length() == 1) {
            switch (Integer.parseInt(imageData)) {
                case ItemEntry.PLACEHOLDER_IMG:
                    Picasso.with(ctxt).load(R.drawable.ic_image_placeholder).into(image_holder);
                    break;
                case ItemEntry.NO_IMG:
                    Picasso.with(ctxt).load(R.drawable.ic_no_image).into(image_holder);
                    break;
                case ItemEntry.DUMMY_IMG:
                    Picasso.with(ctxt).load(R.drawable.ic_dummy).into(image_holder);
                    break;
            }
        } else {
            Picasso.with(ctxt).load(imageData).into(image_holder);
        }

        //open editor with loaded data of the item selected.
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent updateItem = new Intent(ctxt, EditorActivity.class);

                Uri selectedItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);
                updateItem.setData(selectedItemUri);
                ctxt.startActivity(updateItem);
            }
        });
    }
}