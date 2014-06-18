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
	private DynamicGridView gridView1;
    private DynamicGridView gridView2;
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

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                columns = extras.getInt("columnAmount");
            }
            else {
                columns = 3;
            }

            DynamicGridView grid1 = (DynamicGridView) findViewById(R.id.dynamic_grid1);
            DynamicGridView grid2 = (DynamicGridView) findViewById(R.id.dynamic_grid2);

            Log.i(TAG, "columnAmount = " + columns);

            int round = nrDevices / columns;
            int rest = nrDevices % columns;
            grid1.setNumColumns(columns);
            grid2.setNumColumns(rest);

            String[] devices1 = new String[round * columns];
            String[] devices2 = new String[rest];

            System.arraycopy(Devices.deviceStrings, 0, devices1, 0, devices1.length);
            System.arraycopy(Devices.deviceStrings, devices1.length, devices2, 0, devices2.length);

            gridView1 = (DynamicGridView) findViewById(R.id.dynamic_grid1);
            gridView1.setAdapter(new DeviceDynamicAdapter(this,
                    new ArrayList<String>(Arrays.asList(devices1)),
                    columns));

            gridView1.setOnDropListener(new DynamicGridView.OnDropListener() {
                @Override
                public void onActionDrop() {
                    gridView1.stopEditMode();
                }
            });

            gridView1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    gridView1.startEditMode();
                    return false;
                }
            });

            gridView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Toast.makeText(GridActivity.this, parent.getAdapter().getItem(position).toString(),
                            Toast.LENGTH_SHORT).show();
                }
            });

            if (rest > 0) {
                gridView2 = (DynamicGridView) findViewById(R.id.dynamic_grid2);
                gridView2.setAdapter(new DeviceDynamicAdapter(this,
                        new ArrayList<String>(Arrays.asList(devices2)),
                        rest));

                gridView2.setOnDropListener(new DynamicGridView.OnDropListener() {
                    @Override
                    public void onActionDrop() {
                        gridView2.stopEditMode();
                    }
                });

                gridView2.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        gridView2.startEditMode();
                        return false;
                    }
                });

                gridView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(GridActivity.this, parent.getAdapter().getItem(position).toString(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
	}

	@Override
	public void onBackPressed() {
        super.onBackPressed();
	}
}