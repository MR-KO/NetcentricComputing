package com.example.murt.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;


public class FullscreenActivity extends Activity {
	private ImageView image;
	private ImageHandler handler;
	private Bitmap original_img;
	private Bitmap[] imgs;

	private int imgType;
	private int rows;
	private int cols;
	private int index = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullscreen);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		handler = new ImageHandler();
		Intent intent = getIntent();

		/* Check the type of the image from the intent. */
		if (intent.getIntExtra(MainActivity.INTENT_TYPE, MainActivity.TYPE_FILE) == MainActivity.TYPE_RES) {
			imgType = MainActivity.TYPE_RES;
			Drawable drawable = getResources().getDrawable(R.drawable.prepare);
			handler.open(drawable);
		} else {
			imgType = MainActivity.TYPE_FILE;
			handler.open(intent.getStringExtra(MainActivity.INTENT_ORIGINAL_IMAGE));
		}

		original_img = handler.getImage();

		/* Get rows and cols from intent. */
		rows = intent.getIntExtra(MainActivity.INTENT_ROWS, -1);
		cols = intent.getIntExtra(MainActivity.INTENT_COLS, -1);

		/* Set the imageview to the correct image. */
		image = (ImageView)findViewById(R.id.imageView);

		if (imgType == MainActivity.TYPE_RES) {
			image.setImageResource(R.drawable.prepare);
		} else {
			image.setImageBitmap(original_img);
		}

		/* Try to read the supposedly created temp files containing the splitted images. */
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
}
