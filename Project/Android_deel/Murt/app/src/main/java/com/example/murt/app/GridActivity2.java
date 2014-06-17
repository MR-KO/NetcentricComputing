package com.example.murt.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;


public class GridActivity2 extends MainActivity {

	private TextView noDevices;
	private int nrDevices = 1;
	private ImageView imageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//        imageView = (ImageView) findViewById(R.id.device_grid);

		if (nrDevices == 0) {
			setContentView(R.layout.activity_grid_no_devices);
		} else {
			setContentView(R.layout.activity_grid);
		}

		setContent();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void setContent() {
		if (nrDevices == 0) {
			return;
		} else {
			Context context = getApplicationContext();
			Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.device);
			if (bitmap != null) {
				imageView.setImageBitmap(bitmap);
			}
		}
	}
}
