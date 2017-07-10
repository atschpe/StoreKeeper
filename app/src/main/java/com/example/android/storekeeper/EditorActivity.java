package com.example.android.storekeeper;

import android.app.LoaderManager;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.storekeeper.data.StoreContract.ItemEntry;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTouch;

/**
 * Create or update an item
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //identifying the data loaders
    private final int EXISTING_ITM_LOADER = 0;

    //identify that we are updating an existing row of the respective table
    private Uri currentItemUri;
    private String imageSelected; // keep track of the image selected
    private String currencySymbol;

    private static final int SELECT_IMAGE_FILE = 1; //Identifiers for the image requests/intents.

    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    @BindView(R.id.item_edit)
    EditText nameEditor;
    @BindView(R.id.price_edit)
    EditText priceEditor;
    @BindView(R.id.quantity_edit)
    EditText quantityEditor;
    @BindView(R.id.increment_quantity)
    ImageButton quantityIncrement;
    @BindView(R.id.decrement_quantity)
    ImageButton quantityDecrement;
    @BindView(R.id.order_no_edit)
    EditText orderNoEditor;
    @BindView(R.id.description_edit)
    EditText descriptionEditor;
    @BindView(R.id.supplier_edit)
    EditText supplierEditor;
    @BindView(R.id.temp_edit)
    EditText templateEditor;
    @BindView(R.id.image_edit)
    ImageView imageEditor;
    @BindView(R.id.spin_image_edit)
    Spinner imageSelector;
    @BindView(R.id.file_button)
    ImageButton imageFromFile;
    @BindView(R.id.url_button)
    ImageButton imageFromUrl;
    @BindView(R.id.url_view)
    LinearLayout urlPrompt;
    @BindView(R.id.load_url)
    Button loadUrl;
    @BindView(R.id.url_input)
    EditText urlInput;

    int quantityTracker; //keep track of the quantity

    private boolean itemHasChanged = false; //flag whether there has been a change

    //Monitor any touch events so as to change to boolean and react accordingly.
    @OnTouch({R.id.item_edit, R.id.price_edit, R.id.quantity_edit, R.id.order_no_edit,
            R.id.description_edit, R.id.url_input})
    public boolean onTouch() {
        return itemHasChanged = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        ButterKnife.bind(this); //Initialising

        Intent receiveData = getIntent();//did the intent launching the editor have data attached?
        currentItemUri = receiveData.getData();

        if (currentItemUri == null) {
            setTitle(getString(R.string.new_item_editor)); //no data: user intends to add new item
            quantityEditor.setText("5");
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.item_editor)); //data transferred: user intends to edit it.


            getLoaderManager().initLoader(EXISTING_ITM_LOADER, null, this);//initiate data loading.
        }

        Currency currency = Currency.getInstance(Locale.getDefault());
        currencySymbol = String.valueOf(currency.getSymbol(Locale.getDefault()));

        setupSpinner();

        quantityIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantityTracker++;
                quantityEditor.setText(String.valueOf(quantityTracker));
            }
        });

        quantityDecrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantityTracker > 0) {
                    quantityTracker--;
                }
                quantityEditor.setText(String.valueOf(quantityTracker));
            }
        });

        //User wants to use a locally stored image
        imageFromFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getImageFile = new Intent();
                getImageFile.setType("image/*");
                getImageFile.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(getImageFile, "Select image"), SELECT_IMAGE_FILE);
            }
        });

        //user wants to use an image from the internet
        imageFromUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                urlPrompt.setVisibility(View.VISIBLE);

                Intent openBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
                startActivity(openBrowser);
            }
        });

        //load image from the url the user pasted/typed into the editText.
        loadUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imageUrl = urlInput.getText().toString().trim();
                loadUrl(imageEditor, imageUrl);
                imageSelected = imageUrl;
                urlPrompt.setVisibility(View.GONE);
            }
        });
    }

    private void loadUrl(ImageView imageEditor, String imageUrl) {
        Picasso.with(this).load(imageUrl).into(imageEditor);
    }

    /**
     * Display image from intent
     *
     * @param requestCode ensures intent was successful before proceeding.
     * @param resultCode  is the identifier indicating whether the intent comes from file or camera.
     * @param data        is the received information.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_IMAGE_FILE) {
                Uri selectedImageUri = data.getData();
                Picasso.with(this).load(selectedImageUri).into(imageEditor);
                imageSelected = selectedImageUri.toString();
            }
        }
    }

    /**
     * setup spinners for use
     */
    private void setupSpinner() {

        //set up the image spinner for the drawables.
        ArrayAdapter drawableSpinner = ArrayAdapter.createFromResource(this,
                R.array.drawable_options, android.R.layout.simple_spinner_item);
        drawableSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        imageSelector.setAdapter(drawableSpinner);

        //set imageView and keep track of the selected image.
        imageSelector.setPrompt("Standard images");
        imageSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case ItemEntry.PLACEHOLDER_IMG:
                        loadDrawable(imageEditor, R.drawable.ic_image_placeholder);
                        imageSelected = String.valueOf(ItemEntry.PLACEHOLDER_IMG);
                        break;
                    case ItemEntry.NO_IMG:
                        loadDrawable(imageEditor, R.drawable.ic_image_placeholder);
                        imageSelected = String.valueOf(ItemEntry.NO_IMG);
                        break;
                    case ItemEntry.DUMMY_IMG:
                        loadDrawable(imageEditor, R.drawable.ic_dummy);
                        imageSelected = String.valueOf(ItemEntry.DUMMY_IMG);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadDrawable(ImageView imageEditor, int drawable) {
        Picasso.with(this).load(drawable).into(imageEditor);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //inflate menu
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) { //hide delete-item if user is updating
        if (currentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete_editor);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //determine what happens with option select
        switch (item.getItemId()) {
            case R.id.restock_editor:
                restockItem(); //send email to supplier to order new stock.
            case R.id.done_editor:
                saveItem(); //save all information to the table
                finish();
                return true;
            case R.id.delete_editor:
                confirmDeleteAction(); //warn user about delete action
                return true;
            case android.R.id.home:
                if (!itemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardClick = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                warnUnsavedDialog(discardClick);
        }
        return super.onOptionsItemSelected(item);
    }

    private void restockItem() {
        String itemName = nameEditor.getText().toString().trim();
        String emailTemplate = templateEditor.getText().toString().trim().replaceAll("#ITEM#", itemName);

        String contactEmail = supplierEditor.getText().toString().trim();
        String addressString = "mailto:" + contactEmail;

        Intent sendMail = new Intent(Intent.ACTION_SENDTO, Uri.parse(addressString));
        sendMail.putExtra(Intent.EXTRA_SUBJECT, "Order: " + itemName);
        sendMail.putExtra(Intent.EXTRA_TEXT, emailTemplate);
        startActivity(sendMail);
    }


    /**
     * build warning dialog for unsaved data
     *
     * @param discardClick is an onclick listener montiroing the discard action
     */
    private void warnUnsavedDialog(DialogInterface.OnClickListener discardClick) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage(R.string.unsaved_alert);
        alertBuilder.setPositiveButton(R.string.discard_button, discardClick);
        alertBuilder.setNegativeButton(R.string.keep_editing_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = alertBuilder.create(); //create and show the dialog
        alertDialog.show();
    }

    private void saveItem() {

        String nameData = nameEditor.getText().toString().trim();
        String imageData = imageSelected;
        String priceRaw = priceEditor.getText().toString().trim().replace(currencySymbol, "");
        double priceData = Double.parseDouble(priceRaw);
        String quantityRaw = quantityEditor.getText().toString().trim();
        int quantityData = Integer.parseInt(quantityRaw);
        String supplierData = supplierEditor.getText().toString().trim();
        String orderNoData = orderNoEditor.getText().toString().trim();
        String descriptionData = descriptionEditor.getText().toString().trim();
        String templateData = templateEditor.getText().toString().trim();

        //if there is no uri or any views populated return early => nothing to add/update
        if (currentItemUri == null && TextUtils.isEmpty(nameData) && TextUtils.isEmpty(imageData)
                && TextUtils.isEmpty(priceRaw) && TextUtils.isEmpty(quantityRaw)
                && TextUtils.isEmpty(orderNoData)) {
            return;
        }

        //gather all inputs ready for storing in the table
        ContentValues itemValues = new ContentValues();
        itemValues.put(ItemEntry.ITM_NAME, nameData);
        itemValues.put(ItemEntry.ITM_IMAGE, imageSelected);
        itemValues.put(ItemEntry.ITM_PRICE, priceData);
        itemValues.put(ItemEntry.ITM_QUANTITY, quantityData);
        itemValues.put(ItemEntry.ITM_SUP_MAIL, supplierData);
        itemValues.put(ItemEntry.ITM_ORDER_NO, orderNoData);

        if (descriptionData.isEmpty()) {
            descriptionData = ""; // leave empty
        }
        itemValues.put(ItemEntry.ITM_DESCRIPTION, descriptionData);

        if (templateData.isEmpty()) {
            templateData = ""; // leave empty
        }
        itemValues.put(ItemEntry.ITM_EMAIL_TEMP, templateData);

        if (currentItemUri == null) { //indicates a new item being added
            Uri newItem = getContentResolver().insert(ItemEntry.CONTENT_URI, itemValues);

            if (newItem == null) { //error occurred during saving
                Toast.makeText(this, R.string.error_saving_new_item, Toast.LENGTH_SHORT).show();
            } else { //success
                Toast.makeText(this, R.string.save_new_item_success, Toast.LENGTH_SHORT).show();
            }
        } else { // indicates an update
            int rowUpdated = getContentResolver().update(currentItemUri, itemValues, null, null);
            Log.v(LOG_TAG, "row updated: " + rowUpdated);

            if (rowUpdated == 0) {//erroe ocurred during updating
                Toast.makeText(this, R.string.error_updating_item, Toast.LENGTH_SHORT).show();
            } else { //success
                Toast.makeText(this, R.string.save_updated_item_success, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * build warning dialog for deleting data
     */
    private void confirmDeleteAction() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage(R.string.delete_query);
        alertBuilder.setPositiveButton(R.string.delete_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteItem(); //go to delete method
            }
        });
        alertBuilder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss(); //return to editor
                }
            }
        });
        AlertDialog alertDialog = alertBuilder.create(); //create and show the dialog
        alertDialog.show();
    }

    private void deleteItem() {
        if (currentItemUri != null) { //only do something if there is an existing item in the table
            int rowDeleted = getContentResolver().delete(ItemEntry.CONTENT_URI, null, null);
            if (rowDeleted == 0) { //error occurred
                Toast.makeText(this, R.string.error_delete_item, Toast.LENGTH_SHORT).show();
            } else { //successful
                Toast.makeText(this, R.string.successful_item_delete, Toast.LENGTH_SHORT).show();
            }
        }
        finish(); //close activity
    }

    @Override
    public void onBackPressed() {
        if (!itemHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardClick = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish(); //discard is clicked so close the activity
            }
        };
        warnUnsavedDialog(discardClick); //warn user about unsaved data.

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{ //request all columns for viewing/editing
                ItemEntry._ID,
                ItemEntry.ITM_NAME,
                ItemEntry.ITM_IMAGE,
                ItemEntry.ITM_PRICE,
                ItemEntry.ITM_QUANTITY,
                ItemEntry.ITM_SUP_MAIL,
                ItemEntry.ITM_DESCRIPTION,
                ItemEntry.ITM_EMAIL_TEMP,
                ItemEntry.ITM_ORDER_NO};
        return new CursorLoader(this, currentItemUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor crs) {
        if (crs == null && crs.getCount() < 1) {
            return;
        }

        if (crs.moveToFirst()) { // get columns
            int nameColumn = crs.getColumnIndex(ItemEntry.ITM_NAME);
            int imageColumn = crs.getColumnIndex(ItemEntry.ITM_IMAGE);
            int priceColumn = crs.getColumnIndex(ItemEntry.ITM_PRICE);
            int quantityColumn = crs.getColumnIndex(ItemEntry.ITM_QUANTITY);
            int supplierColumn = crs.getColumnIndex(ItemEntry.ITM_SUP_MAIL);
            int descriptionColumn = crs.getColumnIndex(ItemEntry.ITM_DESCRIPTION);
            int emailTempColumn = crs.getColumnIndex(ItemEntry.ITM_EMAIL_TEMP);
            int orderNoColumn = crs.getColumnIndex(ItemEntry.ITM_ORDER_NO);

            //get data from columns
            String nameData = crs.getString(nameColumn);
            String imageData = crs.getString(imageColumn);
            double priceRawData = crs.getDouble(priceColumn);
            int quantityData = crs.getInt(quantityColumn);
            String supplierData = crs.getString(supplierColumn);
            String descriptionData = crs.getString(descriptionColumn);
            String emailTempData = crs.getString(emailTempColumn);
            String orderNoData = crs.getString(orderNoColumn);

            //display the image based on the format of its data.
            if (imageData.length() == 1) {
                switch (Integer.parseInt(imageData)) {
                    case ItemEntry.PLACEHOLDER_IMG:
                        Picasso.with(this).load(R.drawable.ic_image_placeholder).into(imageEditor);
                        break;
                    case ItemEntry.NO_IMG:
                        Picasso.with(this).load(R.drawable.ic_no_img).into(imageEditor);
                        break;
                    case ItemEntry.DUMMY_IMG:
                        Picasso.with(this).load(R.drawable.ic_dummy).into(imageEditor);
                        break;
                }
            } else {
                Picasso.with(this).load(imageData).into(imageEditor);
            }

            //Prepare price for display
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
            String priceData = formatter.format(priceRawData);

            //set data to views
            nameEditor.setText(nameData);
            priceEditor.setText(priceData);
            quantityEditor.setText(String.valueOf(quantityData));
            supplierEditor.setText(supplierData);
            descriptionEditor.setText(descriptionData);
            templateEditor.setText(emailTempData);
            orderNoEditor.setText(orderNoData);

            quantityTracker = quantityData;//update quantity tracker
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { //reset all fields if loader is invalidated
        nameEditor.setText("");
        imageEditor.setImageResource(R.drawable.ic_image_placeholder);
        priceEditor.setText("");
        quantityEditor.setText("5");
        supplierEditor.setText("");
        descriptionEditor.setText("");
        templateEditor.setText("");
        orderNoEditor.setText("");
    }
}