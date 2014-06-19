package com.example.murt.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class GridActivity extends MainActivity {

    public static String TAG = "GridActivity";
    private DynamicGridView gridView1;
    private DynamicGridView gridView2;
    private DeviceDynamicAdapter adapter1;
    private DeviceDynamicAdapter adapter2;
    private Button acceptGrid;
    private int nrDevices;
    private int columns;
    private int rest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nrDevices = Devices.deviceStrings.size();
        Log.i(TAG, "nrDevices = " + nrDevices);

        if (nrDevices <= 0) {
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

            int round = nrDevices / columns;
            rest = nrDevices % columns;
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

        acceptGrid = (Button) findViewById(R.id.acceptGrid);
        acceptGrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> newGrid = new ArrayList<String>();
                for (int i = 0; i < adapter1.getCount(); i++) {
                    newGrid.add(getDevice(i, adapter1));
                }

                if (rest > 0) {
                    for (int i = 0; i < adapter2.getCount(); i++) {
                        newGrid.add(adapter1.getCount() + i, getDevice(i, adapter2));
                    }
                }

                for (int i = 0; i < newGrid.size(); i++) {
                    String device = newGrid.get(i);
                    Devices.deviceStrings.set(i, device);
                }

                /* Intent for showing image....*/
//                Intent intent = new Intent(GridActivity.this, MurtActivity.class);
//                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /* Returns the device of the given adapter at te given position. */
    public String getDevice(int position, DeviceDynamicAdapter adapter) {
        return adapter.getItem(position).toString();
    }
}