package com.azreashade.shadowpurge;

import android.view.View;
import android.widget.AdapterView;

public class SimpleOnItemSelected implements AdapterView.OnItemSelectedListener {
    public interface OnSelected { void apply(int position); }
    private final OnSelected cb;
    public SimpleOnItemSelected(OnSelected cb) { this.cb = cb; }

    @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { if (cb != null) cb.apply(position); }
    @Override public void onNothingSelected(AdapterView<?> parent) { /* no-op */ }
}
