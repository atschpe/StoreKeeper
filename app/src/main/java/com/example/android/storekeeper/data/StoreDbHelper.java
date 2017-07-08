package com.example.android.storekeeper.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.storekeeper.data.StoreContract.ItemEntry;

/**
 * {@link StoreDbHelper} is a {@link SQLiteOpenHelper} to create and manage the database.
 */

public class StoreDbHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = StoreDbHelper.class.getSimpleName();//for log messages
    private static final String DATABASE_NAME = "store.db";
    private static final int DATABASE_VERSION = 1; //increment if changes are made to the schema.

    //SQL building blocks to minimise typos & increase quick grasp of requirements for each item
    private static final String CREATE = "CREATE TABLE ";
    private static final String OPEN = " (";
    private static final String COMMA = ", ";
    private static final String CLOSE = ");";
    private static final String INT_AUTO = " INTEGER PRIMARY KEY AUTOINCREMENT";
    private static final String INT_REQUIRED = " INTEGER NOT NULL";
    private static final String REAL_REQUIRED = " REAL NOT NULL";
    private static final String TEXT = " TEXT";
    private static final String TEXT_REQUIRED = " TEXT NOT NULL";

    /**
     * setup constructor
     *
     * @param ctxt of the app
     */
    public StoreDbHelper(Context ctxt) {
        super(ctxt, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //create inventory database
        String SQL_CREATE_ITM_TABLE = CREATE + ItemEntry.TABLE_NAME + OPEN
                + ItemEntry._ID + INT_AUTO + COMMA
                + ItemEntry.ITM_NAME + TEXT_REQUIRED + COMMA
                + ItemEntry.ITM_IMAGE + TEXT_REQUIRED + COMMA
                + ItemEntry.ITM_DESCRIPTION + TEXT + COMMA
                + ItemEntry.ITM_PRICE + REAL_REQUIRED + COMMA
                + ItemEntry.ITM_QUANTITY + INT_REQUIRED + COMMA
                + ItemEntry.ITM_SUP_MAIL + TEXT_REQUIRED + COMMA
                + ItemEntry.ITM_EMAIL_TEMP + TEXT + COMMA
                + ItemEntry.ITM_ORDER_NO + TEXT_REQUIRED + CLOSE;
        db.execSQL(SQL_CREATE_ITM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //no upgrades yet to be considered
    }
}