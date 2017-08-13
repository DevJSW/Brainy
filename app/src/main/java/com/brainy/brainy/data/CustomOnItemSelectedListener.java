package com.brainy.brainy.data;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

/**
 * Created by Shephard on 8/12/2017.
 */

public class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Toast.makeText(adapterView.getContext(),
        "OnItemSelectedListener : " + adapterView.getItemAtPosition(i).toString(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
