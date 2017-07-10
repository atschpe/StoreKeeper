package com.example.android.storekeeper;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.storekeeper.data.StoreContract.ItemEntry;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.rec_view)
    RecyclerView rv;
    @BindView(R.id.empty_view)
    TextView empty;

    public static final String LOG_TAG = CatalogActivity.class.getSimpleName();//For log messages

    private static final int INV_LOADER = 1; //loader identifier.
    private StoreAdapter inventAdapter; // adapter for list view.
    private RecyclerView.LayoutManager viewManager; //layoutManager for the recycler view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        ButterKnife.bind(this);
        inventAdapter = new StoreAdapter(this, null);
        viewManager = new LinearLayoutManager(this);

        fab.setImageResource(R.mipmap.ic_launcher);
        fab.setOnClickListener(new View.OnClickListener() { //open Editor when FAB is clicked.
            @Override
            public void onClick(View v) {
                Intent addNewItem = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(addNewItem);
            }
        });

        rv.setLayoutManager(viewManager);//set layout Manager to recycler view
        rv.setAdapter(inventAdapter);//set adapter to populate recycler view

        getLoaderManager().initLoader(INV_LOADER, null, this);//initiate loader to populate list.
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.ITM_IMAGE,
                ItemEntry.ITM_NAME,
                ItemEntry.ITM_PRICE,
                ItemEntry.ITM_QUANTITY,
                ItemEntry.ITM_DESCRIPTION,
                ItemEntry.ITM_EMAIL_TEMP,
                ItemEntry.ITM_SUP_MAIL};
        return new CursorLoader(this, ItemEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            empty.setVisibility(View.GONE);
        }
        inventAdapter.swapCursor(data); //update cursor with data
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        empty.setVisibility(View.VISIBLE);
        inventAdapter.swapCursor(null); // release all data from cursor
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.catalog_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.dummy_add:
                addDummyItem();
                return true;
            case R.id.delete_all:
                deleteAllItems();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addDummyItem() {

        //dummy image.
        String imageData = String.valueOf(ItemEntry.DUMMY_IMG);

        //Gather values from dummy
        ContentValues values = new ContentValues();
        values.put(ItemEntry.ITM_NAME, "Dummy");
        values.put(ItemEntry.ITM_IMAGE, imageData);
        values.put(ItemEntry.ITM_PRICE, 10.00);
        values.put(ItemEntry.ITM_QUANTITY, 5);
        values.put(ItemEntry.ITM_SUP_MAIL, "order@supplier.com");
        values.put(ItemEntry.ITM_DESCRIPTION, getString(R.string.dummy_description));
        values.put(ItemEntry.ITM_EMAIL_TEMP, getString(R.string.dummy_order_temp));
        values.put(ItemEntry.ITM_ORDER_NO, "000000");

        Uri addDummyItem = this.getContentResolver().insert(ItemEntry.CONTENT_URI, values);
        Log.v(LOG_TAG, "Dummy added to item table of the database.");
    }

    private void deleteAllItems() {
        int rowsDeleted = this.getContentResolver().delete(ItemEntry.CONTENT_URI, null, null);
        Log.v(LOG_TAG, rowsDeleted + "rows have been deleted from the item table in the database.");
    }

}