package com.example.android.storekeeper.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.storekeeper.data.StoreContract.ItemEntry;

/**
 * {@link StoreProvider} is a {@link ContentProvider} enable all CRUD methods for all tables.
 */
public class StoreProvider extends ContentProvider {

    private static final String LOG_TAG = StoreProvider.class.getSimpleName();//for log messages

    //uri matcher codes for list and item retrieval for each table
    private static final int ITM_LIST = 100;
    private static final int ITM_ENTRY = 101;

    private SQLiteDatabase dbRead;
    private SQLiteDatabase dbWrite;

    /**
     * Match uri to its corresponding code
     */
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static { // find the right uri depending on the table calling and whether its items or entirety
        uriMatcher.addURI(StoreContract.CONTENT_AUTH, StoreContract.PATH_ITEM, ITM_LIST);
        uriMatcher.addURI(StoreContract.CONTENT_AUTH, StoreContract.PATH_ITEM + "/#", ITM_ENTRY);
    }

    private StoreDbHelper storeDbHelper; //Db helper

    @Override
    public boolean onCreate() {
        storeDbHelper = new StoreDbHelper(getContext());

        dbRead = storeDbHelper.getReadableDatabase(); //readable database call
        dbWrite = storeDbHelper.getWritableDatabase(); //writable database call
        return true;
    }

    Cursor cursor; //to hold the data
    int match; // for uri matching

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        match = uriMatcher.match(uri);
        switch (match) {
            case ITM_LIST:
                cursor = dbRead.query(ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ITM_ENTRY:
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = dbRead.query(ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri, which cannot be queried: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        match = uriMatcher.match(uri);
        switch (match) {
            case ITM_LIST:
                return ItemEntry.CONTENT_ITEM_TYPE;
            case ITM_ENTRY:
                return ItemEntry.CONTENT_LIST_TYPE;
            default:
                throw new IllegalArgumentException("Unknown uri " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        match = uriMatcher.match(uri);
        switch (match) {
            case ITM_LIST:
                return insertItem(uri, values);
        }
        return null;
    }

    /**
     * Insert new item into inventory table
     *
     * @param uri    is the incoming request
     * @param values are the values to be add for the item
     * @return uri for the row.
     */
    private Uri insertItem(Uri uri, ContentValues values) {

        //check required columns
        checkItemName(values);
        checkItemImage(values);
        checkItemPrice(values);
        checkItemQuantity(values);
        checkItemSupplier(values);
        checkItemOrderNo(values);

        //Image, category and threshold are not required.

        long id = dbWrite.insert(ItemEntry.TABLE_NAME, null, values);// insert values into table

        if (id == -1) { //Ensure the insertion was successful.
            Log.e(LOG_TAG, "Row could not be inserted for " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id); //return uri with id of new row.
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted; //keep track of the id(s).

        match = uriMatcher.match(uri);
        switch (match) {
            case ITM_LIST: //all rows
                rowsDeleted = dbWrite.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ITM_ENTRY: // given id
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = dbWrite.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException(uri + " could not be deleted.");
        }

        if (rowsDeleted != 0) { // notify all listeners whether anything is deleted.
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        match = uriMatcher.match(uri);
        switch (match) {
            case ITM_ENTRY:
                return updateItem(uri, values, selection, selectionArgs);
        }
        return 0;
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        //check required columns
        if (values.containsKey(ItemEntry.ITM_NAME)) {
            checkItemName(values);
        }

        if (values.containsKey(ItemEntry.ITM_IMAGE)) {
            checkItemImage(values);
        }

        if (values.containsKey(ItemEntry.ITM_PRICE)) {
            checkItemPrice(values);
        }

        if (values.containsKey(ItemEntry.ITM_QUANTITY)) {
            checkItemQuantity(values);
        }

        if (values.containsKey(ItemEntry.ITM_SUP_MAIL)) {
            checkItemSupplier(values);
        }

        if (values.containsKey(ItemEntry.ITM_ORDER_NO)) {
            checkItemOrderNo(values);
        }

        //Image, category and threshold are not required.

        if (values.size() == 0) {
            return 0; // return early if nothing has been changed
        }

        int rowsUpdated = dbWrite.update(ItemEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) { // notify all listeners whether anything is updated.
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated; //return number of rows changed
    }

    /**
     * -----Checkers for the various required columns, to ensure they are not empty-----
     *
     * @param values the values being added or updated for the item in question
     */
    private void checkItemName(ContentValues values) {
        String item = values.getAsString(ItemEntry.ITM_NAME);
        if (item.isEmpty()) {
            throw new IllegalArgumentException("Item needs to be named.");
        }
    }

    private void checkItemImage(ContentValues values) {
        String image = values.getAsString(ItemEntry.ITM_IMAGE);
        if (image.isEmpty()) {
            throw new IllegalArgumentException("Item needs an image.");
        }
    }

    private void checkItemPrice(ContentValues values) {
        String price = values.getAsString(ItemEntry.ITM_PRICE);
        if (price.isEmpty()) {
            throw new IllegalArgumentException("Price needs to be added.");
        }
    }

    private void checkItemQuantity(ContentValues values) {
        int quantity = values.getAsInteger(ItemEntry.ITM_QUANTITY);
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity needs to be added.");
        }
    }

    private void checkItemSupplier(ContentValues values) {
        String supplier = values.getAsString(ItemEntry.ITM_SUP_MAIL);
        if (supplier.isEmpty()) {
            throw new IllegalArgumentException("Supplier needs to be added.");
        }
    }

    private void checkItemOrderNo(ContentValues values) {
        String orderNo = values.getAsString(ItemEntry.ITM_ORDER_NO);
        if (orderNo.isEmpty()) {
            throw new IllegalArgumentException("Order no. needs to be added.");
        }
    }
}