package com.example.murt.app;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class GridActivity extends MainActivity {

    public static String TAG = "GridActivity";
	private DynamicGridView gridView;
    private int nrDevices;
    private int columns;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        nrDevices = Devices.deviceStrings.length;
        Log.i(TAG, "nrDevices = " + nrDevices);

        if (nrDevices == 0) {
            setContentView(R.layout.activity_grid_no_devices);
        }
        else {
            setContentView(R.layout.activity_grid);

            DynamicGridView grid = (DynamicGridView) findViewById(R.id.dynamic_grid);

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                columns = extras.getInt("columnAmount");
            }
            else {
                Log.i(TAG, "DERP!");
            }

            Log.i(TAG, "columnAmount = " + columns);
            grid.setNumColumns(columns);

            gridView = (DynamicGridView) findViewById(R.id.dynamic_grid);
            gridView.setAdapter(new DeviceDynamicAdapter(this,
                    new ArrayList<String>(Arrays.asList(Devices.deviceStrings)),
                    columns));
            //        add callback to stop edit mode if needed
            //        gridView.setOnDropListener(new DynamicGridView.OnDropListener()
            //        {
            //            @Override
            //            public void onActionDrop()
            //            {
            //                gridView.stopEditMode();
            //            }
            //        });
            gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    gridView.startEditMode();
                    return false;
                }
            });

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Toast.makeText(GridActivity.this, parent.getAdapter().getItem(position).toString(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
	}

	@Override
	public void onBackPressed() {
        if (nrDevices == 0) {
            super.onBackPressed();
        }
        else {
            if (gridView.isEditMode()) {
                gridView.stopEditMode();
            } else {
                super.onBackPressed();
            }
        }
	}

    public void showDialog() {

        final Dialog dialog = new Dialog(GridActivity.this);
        dialog.setTitle("Found " + nrDevices + " devices");
        dialog.setContentView(R.layout.grid_dialog);
        Button b1 = (Button) dialog.findViewById(R.id.button1);
        Button b2 = (Button) dialog.findViewById(R.id.button2);
        final NumberPicker np1 = (NumberPicker) dialog.findViewById(R.id.numberPicker1);
        final NumberPicker np2 = (NumberPicker) dialog.findViewById(R.id.numberPicker2);
        np1.setMinValue(1);
        np2.setMinValue(1);
        np1.setMaxValue(5);
        np2.setMaxValue(5);
        np1.setWrapSelectorWheel(false);
        np2.setWrapSelectorWheel(false);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value1 = Integer.parseInt(String.valueOf(np1.getValue()));
                columns = Integer.parseInt(String.valueOf(np2.getValue()));
                Log.i(TAG, "value1 = " + value1 + ", columns = " + columns);
                dialog.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // dismiss the dialog
            }
        });
        dialog.show();

    }
}
