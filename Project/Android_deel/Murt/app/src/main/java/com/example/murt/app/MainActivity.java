package com.example.murt.app;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class MainActivity extends Activity {

	public static final String TAG = "MainActivity";

	public static final String INTENT_TYPE = "com.example.murt.app.type";
	public static final String INTENT_ORIGINAL_IMAGE = "com.example.murt.app.original";
	public static final String INTENT_DEVICES_PER_ROW = "com.example.murt.app.dev_per_rows";

	public static final String SPLIITED_IMGS_PREFIX = "split_";
	public static final String SPLITTED_IMGS_EXT = ".png";

	public static final int TYPE_RES = 1;
	private int imgType = TYPE_RES;
	public static final int TYPE_FILE = 2;
	public static final int DEFAULT_RES = R.drawable.prepare;
	/* Start indicates the original image, [0, imgs.length - 1] indicates the splitted images. */
	private final static int START = -1;
	private int index = START;
	// Used for selecting image
	private final static int REQ_CODE_PICK_IMAGE = 1;
	private String imgPath = "";
	private ImageView image = null;
	private ImageHandler handler = null;
	private Bitmap[] imgs = null;
	private int[] devicesPerRow = {2, 1};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.i(TAG, "MainActivity.onCreate()!");

		Button gridButton = (Button) findViewById(R.id.gridButton);
		gridButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent launch = new Intent(MainActivity.this, GridActivity.class);
				startActivity(launch);
			}
		});

		Button openImageButton = (Button) findViewById(R.id.openImageButton);
		openImageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				openNewImage();
			}
		});

		image = (ImageView) findViewById(R.id.imageView);

		image.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				rotateSplittedImages();
			}
		});

		Button showOriginalButton = (Button) findViewById(R.id.showOriginalButton);
		showOriginalButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				/* Check if we have an open image... */
				if (handler.getImage() != null) {
					/* Reset imageview. */
					image.setImageBitmap(handler.getImage());
					index = 0;
				} else {
					openNewImage();
				}
			}
		});

		Button fullscreenButton = (Button) findViewById(R.id.fullscreenButton);
		fullscreenButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, FullscreenActivity.class);

				/*
					2 Types of intents, one with resource, one with file path.
					Pass the file path for the original image, or the resource id.
			\t*/
				if (imgType == TYPE_RES) {
					intent.putExtra(INTENT_TYPE, TYPE_RES);

					/* The following isn't actually used, its also hardcoded in FullscreenActivity. */
					intent.putExtra(INTENT_ORIGINAL_IMAGE, DEFAULT_RES);
				} else {
					intent.putExtra(INTENT_TYPE, TYPE_FILE);
					intent.putExtra(INTENT_ORIGINAL_IMAGE, imgPath);
				}

				/* Add the int array of devices per row. */
				intent.putExtra(INTENT_DEVICES_PER_ROW, devicesPerRow);

				/* We do not send the splitted imgs, as they are saved in temporary files or recreated in the new activity. */
				startActivity(intent);

				/* Show a toast for better user experience (not that we care about that) :P */
				Toast toast = Toast.makeText(getApplicationContext(), "Click to rotate", Toast.LENGTH_SHORT);
				toast.show();
			}
		});

		/* Open default image. */
		image.setImageResource(DEFAULT_RES);

		handler = new ImageHandler();
		Drawable drawable = getResources().getDrawable(DEFAULT_RES);
		handler.open(drawable);

//		imgs = handler.splitImg(rows, cols);
		imgs = handler.splitImgToDevices(devicesPerRow);
		deleteTempImageFiles();
		saveImagesToFile(imgs);

		/* Show a toast for better user experience (not that we care about that) :P */
		Toast toast = Toast.makeText(getApplicationContext(), "Click image to rotate", Toast.LENGTH_SHORT);
		toast.show();
	}

	private void openNewImage() {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, REQ_CODE_PICK_IMAGE);
	}

	private void rotateSplittedImages() {
		/* Check if we have an open image... */
		if (handler.getImage() != null && imgs != null) {
			/* If we're at the start again, set the image to the original. */
			if (index == START) {
				if (imgType == MainActivity.TYPE_RES) {
					image.setImageResource(MainActivity.DEFAULT_RES);
				} else {
					image.setImageBitmap(handler.getImage());
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
		} else {
			openNewImage();
		}
	}

	/* Saves the array of bitmap to .PNG files. */
	public boolean saveImagesToFile(Bitmap[] imgs) {
		if (imgs != null) {
			File outputDir = getCacheDir();

			for (int i = 0; i < imgs.length; i++) {
				try {
					File outputFile = new File(outputDir, MainActivity.SPLIITED_IMGS_PREFIX + i +
							MainActivity.SPLITTED_IMGS_EXT);
					OutputStream outStream = new FileOutputStream(outputFile);
					imgs[i].compress(Bitmap.CompressFormat.PNG, 100, outStream);
					outStream.flush();
					outStream.close();
				} catch (IOException e) {
					/* Failed, so remove all created temp images. */
					Log.e(TAG, "Failed to create temp file! " + e.getMessage());
					return false;
				}
			}
		}

		Log.i(TAG, "Successfully saved all imgs to temp files.");
		return true;
	}

	/* List all fils in the cache dir for debug purposes. */
	public File[] listTempImageFiles() {
		File[] files = getCacheDir().listFiles();

		if (files == null) {
			Log.e(TAG, "Cache dir.listFiles() returns null!");
			return null;
		} else if (files.length == 0) {
			Log.e(TAG, "Cache dir.listFiles() is empty array!");
			return null;
		} else {
			Log.d(TAG, "List of files in the cache dir:");

			for (int i = 0; i < files.length; i++) {
				Log.d(TAG, "File " + i + ": " + files[i].getAbsolutePath());
			}
		}

		return files;
	}

	/* Removes all created temporary saved image files, if any. */
	public void deleteTempImageFiles() {
		/* Get a list of all temp image files. */
		File[] files = listTempImageFiles();

		if (files == null) {
			return;
		}

		/* Delete them all. */
		boolean status = true;

		for (int i = 0; i < files.length; i++) {
			status = files[i].delete();

			if (!status) {
				Log.e(TAG, "Failed to delete file: " + files[i].getAbsolutePath());
			}
		}
	}

	/* Returns true upon success, else false. */
	public boolean setNewDevicesPerRowArray(int[] newDevicesPerRow) {
		if (newDevicesPerRow == null) {
			return false;
		}

		devicesPerRow = newDevicesPerRow.clone();
		return true;
	}

	/* Set a specific amount of devices per row. Returns true upon success, else false. */
	public boolean setDevicesPerRow(int row, int devices) {
		/* Do bounds checking as well... */
		if (row < 0 || devices < 1 || row >= devicesPerRow.length) {
			return false;
		} else {
			devicesPerRow[row] = devices;
			return true;
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		switch (requestCode) {
			case REQ_CODE_PICK_IMAGE:
				if (resultCode == RESULT_OK) {
					Uri selectedImage = imageReturnedIntent.getData();
					String[] filePathColumn = {MediaStore.Images.Media.DATA};

					Cursor cursor = getContentResolver().query(
							selectedImage, filePathColumn, null, null, null);
					cursor.moveToFirst();

					int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
					String filePath = cursor.getString(columnIndex);
					cursor.close();

					handler.open(filePath);

					/* Open the image in the handler, and split it. */
					if (handler.getImage() != null) {
						image.setImageBitmap(handler.getImage());
						index = 0;

//						imgs = handler.splitImg(rows, cols);
						imgs = handler.splitImgToDevices(devicesPerRow);

						deleteTempImageFiles();
						saveImagesToFile(imgs);

						imgPath = filePath;
						imgType = TYPE_FILE;
					}
				}
			default:
				break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		deleteTempImageFiles();
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
