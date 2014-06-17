package com.example.murt.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class FullscreenActivity extends Activity {
	public final static String TAG = "FullscreenActivity";
	private ImageView image;
	private ImageHandler handler;
	private Bitmap original_img;
	private Bitmap[] imgs;

	private int imgType;
	private int[] devicesPerRow;

	/* Start indicates the original image, [0, imgs.length - 1] indicates the splitted images. */
	private final static int START = -1;
	private int index = START;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* Go fullscreen. */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_fullscreen);

		handler = new ImageHandler();
		Intent intent = getIntent();

		/* Check the type of the image from the intent. */
		if (intent.getIntExtra(MainActivity.INTENT_TYPE, MainActivity.TYPE_FILE) == MainActivity.TYPE_RES) {
			imgType = MainActivity.TYPE_RES;
			Drawable drawable = getResources().getDrawable(MainActivity.DEFAULT_RES);
			handler.open(drawable);
		} else {
			imgType = MainActivity.TYPE_FILE;
			handler.open(intent.getStringExtra(MainActivity.INTENT_ORIGINAL_IMAGE));
		}

		original_img = handler.getImage();

		/* Get devices per row from intent. */
		devicesPerRow = intent.getIntArrayExtra(MainActivity.INTENT_DEVICES_PER_ROW);

		if (devicesPerRow == null || devicesPerRow.length == 0) {
			devicesPerRow = new int[2];
			devicesPerRow[0] = 2;
			devicesPerRow[1] = 1;
		}

		/* Set the imageview to the correct image. */
		image = (ImageView)findViewById(R.id.imageView);
		index++;

		if (imgType == MainActivity.TYPE_RES) {
			image.setImageResource(MainActivity.DEFAULT_RES);
		} else {
			image.setImageBitmap(original_img);
		}

		/* Try to read the supposedly created temp files containing the splitted images. */
		int numDevices = 0;

		for (int i = 0; i < devicesPerRow.length; i++) {
			numDevices += devicesPerRow[i];
		}

		imgs = new Bitmap[numDevices];
		boolean success = true;
		File inputDir = getCacheDir();

		for (int i = 0; i < numDevices; i++) {
			/* Read 1 file and put it in the imgs Bitmap array. */
			try {
				File inputFile = new File(inputDir, MainActivity.SPLIITED_IMGS_PREFIX + i + MainActivity.SPLITTED_IMGS_EXT);
				Log.d(TAG, "Trying to read from file: " + inputFile.getAbsolutePath());
				FileInputStream fileInput = new FileInputStream(inputFile);
				imgs[i] = BitmapFactory.decodeStream(fileInput);
				fileInput.close();
			} catch (IOException e) {
				success = false;
				Log.e(TAG, e.getMessage());
			}
		}

		/* If we failed for some reason, re-create the splitted images. */
		if (!success) {
            Log.i(TAG, "Success was false, some or all images could not be read from file.");
//			imgs = handler.splitImg(cols, rows);
            devicesPerRow = new int[2];
			devicesPerRow[0] = 2;
			devicesPerRow[1] = 1;
            imgs = handler.splitImgToDevices(devicesPerRow);


		}

		/* When clicking the imageview, rotate between the original image and the splitted images. */
		image.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				/* If we're at the start again, set the image to the original. */
				if (index == START) {
					if (imgType == MainActivity.TYPE_RES) {
						image.setImageResource(MainActivity.DEFAULT_RES);
					} else {
						image.setImageBitmap(original_img);
					}
				} else {
					/* Else, rotate the splitted images. */
					image.setImageBitmap(imgs[index]);
				}

				/* Rotate the index. */
				if (index == imgs.length - 1) {
					index = START;
				} else {
					index++;
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
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
