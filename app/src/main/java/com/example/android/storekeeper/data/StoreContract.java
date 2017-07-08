package com.example.android.storekeeper.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * {@link StoreContract} holds all constants needed for the database
 */

public class StoreContract {
    private StoreContract() {
    }

    //global variables
    public static final String CONTENT_AUTH = "com.example.android.storekeeper";
    public static final Uri BASE_URI = Uri.parse("content://" + CONTENT_AUTH);

    public static final String PATH_ITEM = "item";

    /**
     * {@link ItemEntry} defines all items in the database.
     */
    public static final class ItemEntry implements BaseColumns {

        //MIME types for calling the list or a single item
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/"
                + CONTENT_AUTH + PATH_ITEM;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
                + CONTENT_AUTH + PATH_ITEM;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI, PATH_ITEM);

        //Table set up
        public static final String TABLE_NAME = "item"; // table name = inventory
        public static final String _ID = BaseColumns._ID; // auto generate unique id for ach item

        //columns
        public static final String ITM_NAME = "name"; //unique name for item
        public static final String ITM_ORDER_NO = "order_no"; //company internal or global number
        public static final String ITM_PRICE = "price"; //price per item
        public static final String ITM_QUANTITY = "quantity"; //amount currently in stock
        public static final String ITM_SUP_MAIL = "supplier"; //name of supplier
        public static final String ITM_EMAIL_TEMP = "email_template"; //template for order email
        public static final String ITM_DESCRIPTION = "description"; //item description
        public static final String ITM_IMAGE = "image"; //image of item

        //Images provided
        public static final int PLACEHOLDER_IMG = 0;
        public static final int NO_IMG = 1;
        public static final int DUMMY_IMG = 2;
    }
}