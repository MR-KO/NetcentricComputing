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
import java.util.List;

public class GridActivity extends MainActivity {

    public static String TAG = "GridActivity";
    private DynamicGridView gridView1;
    private DynamicGridView gridView2;
    private DeviceDynamicAdapter adapter1;
    private DeviceDynamicAdapter adapter2;
    private int nrDevices;
    private int columns;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nrDevices = Devices.deviceStrings.size();
        Log.i(TAG, "nrDevices = " + nrDevices);

        if (nrDevices == 0) {
            setContentView(R.layout.activity_grid_no_devices);
        } else {
            setContentView(R.layout.activity_grid);

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                columns = extras.getInt("columnAmount");
            } else {
                columns = 3;
            }

            DynamicGridView grid1 = (DynamicGridView) findViewById(R.id.dynamic_grid1);
            DynamicGridView grid2 = (DynamicGridView) findViewById(R.id.dynamic_grid2);

            Log.i(TAG, "columnAmount = " + columns);

            int round = nrDevices / columns;
            int rest = nrDevices % columns;
            grid1.setNumColumns(columns);
            grid2.setNumColumns(rest);

            final List<String> devices1 = Devices.deviceStrings.subList(0, round * columns);
            final List<String> devices2 = Devices.deviceStrings.subList(round * columns, Devices.deviceStrings.size());

            adapter1 = new DeviceDynamicAdapter(this, devices1,
                    columns);
            gridView1 = (DynamicGridView) findViewById(R.id.dynamic_grid1);
            gridView1.setAdapter(adapter1);

            gridView1.setOnDropListener(new DynamicGridView.OnDropListener() {
                @Override
                public void onActionDrop() {
                    gridView1.stopEditMode();
                    String derp = getDevice(0, adapter1);
                    Log.i(TAG, "derp = " + derp);
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
                adapter2 = new DeviceDynamicAdapter(this, devices2,
                        rest);
                gridView2 = (DynamicGridView) findViewById(R.id.dynamic_grid2);
                gridView2.setAdapter(adapter2);

                gridView2.setOnDropListener(new DynamicGridView.OnDropListener() {
                    @Override
                    public void onActionDrop() {
                        gridView2.stopEditMode();
                        String derp2 = getDevice(0, adapter2);
                        Log.i(TAG, "derp2 = " + derp2);
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

    public String getDevice(int position, DeviceDynamicAdapter adapter) {
        return adapter.getItem(position).toString();
    }
}