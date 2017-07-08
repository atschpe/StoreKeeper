package com.example.android.storekeeper;

import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * {@link StoreHolder} is a {@link RecyclerView.ViewHolder} linking the Views to their content.
 * Built on https://gist.github.com/skyfishjy/443b7448f59be978bc59 &
 * https://github.com/Rmhuneineh/Inventory-UdacityProject10 to fill in the gaps of what is
 * new for me in using RecyclerView with a database.
 */

public abstract class StoreHolder<ViewHolder extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<ViewHolder> {

    private final CatalogActivity ctxt;
    private Cursor crs;
    private boolean dataIsValid;
    private int rowId;
    private final DataSetObserver observer;


    public StoreHolder(CatalogActivity ctxt, Cursor crs) {

        //initialise all needed
        this.ctxt = ctxt;
        this.crs = crs;
        this.dataIsValid = crs != null;
        this.rowId = dataIsValid ? crs.getColumnIndex("_id") : -1;
        this.observer = new NotifyDataObserver();
        if (crs != null) {
            crs.registerDataSetObserver(observer);
        }

    }

    public Cursor getCrs() {
        return crs;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    public abstract void onBindViewHolder(ViewHolder holder, Cursor crs);

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (!dataIsValid) {
            throw new IllegalStateException("The cursor must be valid");
        }
        if (!crs.moveToPosition(position)) {
            throw new IllegalStateException("Could not move cursore to position: " + position);
        }
        onBindViewHolder(holder, crs);
    }

    @Override
    public int getItemCount() {
        if (dataIsValid && crs != null) {
            return crs.getCount();
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        if (dataIsValid && crs != null && crs.moveToPosition(position)) {
            return crs.getLong(rowId);
        }
        return 0;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(true);
    }

    /**
     * Exchange recent cursor for a new one.
     *
     * @param crs is the cursor
     */
    public void changeCursor(Cursor crs) {
        Cursor recentCrs = swapCursor(crs);
        if (recentCrs != null) {
            recentCrs.close();
        }
    }

    public Cursor swapCursor(Cursor newCrs) {
        if (newCrs == crs) {
            return null;
        }
        final Cursor recentCrs = crs;

        if (recentCrs != null && observer != null) {
            recentCrs.unregisterDataSetObserver(observer);
        }

        crs = newCrs;
        if (newCrs != null) {
            if (observer != null) {
                crs.registerDataSetObserver(observer);
            }
            rowId = newCrs.getColumnIndexOrThrow("_id");
            dataIsValid = true;
            notifyDataSetChanged();
        } else {
            rowId = -1;
            dataIsValid = false;
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
        return recentCrs;
    }

    private class NotifyDataObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            dataIsValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            dataIsValid = false;
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
    }
}