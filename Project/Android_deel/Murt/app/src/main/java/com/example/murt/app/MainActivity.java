package com.example.murt.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.nsd.NsdManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Map;

import nl.uva.netcentric.murt.protocol.AndroidMurtClient;
import nl.uva.netcentric.murt.protocol.AndroidMurtServer;
import nl.uva.netcentric.murt.protocol.MurtConfiguration;
import nl.uva.netcentric.murt.protocol.MurtConnection;
import nl.uva.netcentric.murt.protocol.MurtConnectionListener;


public class MainActivity extends Activity implements MurtConnectionListener {

	public static final String TAG = "MainActivity";

	public static final String INTENT_TYPE = "com.example.murt.app.type";
	public static final String INTENT_IMAGE = "com.example.murt.app.image";
	public static final String INTENT_DEVICES_PER_ROW = "com.example.murt.app.dev_per_rows";
	public static final String INTENT_MODE = "com.example.murt.app.mode";

	public static final String SPLIITED_IMGS_PREFIX = "split_";
	public static final String SPLITTED_IMGS_EXT = ".png";

	public static final int TYPE_RES = 1;
	public static final int TYPE_FILE = 2;
	public static final int DEFAULT_RES = R.drawable.prepare2;

	private int imgType = TYPE_RES;

	/* Start indicates the original imageView, [0, imgs.length - 1] indicates the splitted images. */
	private final static int START = -1;
	private int index = START;

	/* Used for selecting imageView */
	private final static int REQ_CODE_PICK_IMAGE = 1;
	private String imgPath = "";
	private ImageView imageView = null;
	private ImageHandler handler = null;
	private Bitmap[] imgs = null;
	private int[] devicesPerRow = {2, 1};

	private int columns;

	/* Used for server/client stuff */
	public static final int MODE_NONE = 0;
	public static final int MODE_SERVER = 1;
	public static final int MODE_CLIENT = 2;

	private AndroidMurtServer server;
	private AndroidMurtClient client;
	private NsdManager nsdManager;

	public static final String DEVICE_PREFIX = "MurtDevice ";

	private int mode = MODE_NONE;
	private Map<Integer, String> connections = new Hashtable<Integer, String>();
	private boolean updateView = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.i(TAG, "MainActivity.onCreate()!");

		Button gridButton = (Button) findViewById(R.id.gridButton);
		gridButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showDialog();
			}
		});

		Button masterButton = (Button) findViewById(R.id.masterButton);

		masterButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mode = MODE_SERVER;
				nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
				server = new AndroidMurtServer(nsdManager, MainActivity.this, MurtConfiguration.DEBUG_PORT);
			}
		});

		Button clientButton = (Button) findViewById(R.id.clientButton);
		clientButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mode = MODE_CLIENT;
				nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);

				// TODO: Remove stupid shit that Sjoerd put in
				DisplayMetrics displayMetrics = new DisplayMetrics();
				WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
				wm.getDefaultDisplay().getMetrics(displayMetrics);
				int resX = displayMetrics.widthPixels;
				int resY = displayMetrics.heightPixels;
				String config = resX + "," + resY;

				client = new AndroidMurtClient(nsdManager, MainActivity.this, config);

//				// TODO: Test shit
//				ByteArrayOutputStream stream = new ByteArrayOutputStream();
//				imgs[1].compress(Bitmap.CompressFormat.PNG, 100, stream);
//				byte[] tempByte = stream.toByteArray();
//
//				if (tempByte == null) {
//					Log.e(TAG, "Compressed byte array is null!");
//				} else {
//					Log.i(TAG, "Compressed to byte array of length " + tempByte.length);
//				}
//
//				Bitmap tempBitmap = BitmapFactory.decodeByteArray(tempByte, 0, tempByte.length);
//
//				if (tempBitmap == null) {
//					Log.e(TAG, "Decoded bitmap is null!");
//				} else {
//					Log.i(TAG, "Setting decoded bitmap as imageview");
//					imageView.setImageBitmap(tempBitmap);
//				}
			}
		});

		Button openImageButton = (Button) findViewById(R.id.openImageButton);
		openImageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				openNewImage();
			}
		});

		imageView = (ImageView) findViewById(R.id.imageView);

		imageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				rotateSplittedImages();
			}
		});

		handler = new ImageHandler();
		Drawable drawable = getResources().getDrawable(DEFAULT_RES);
		handler.open(drawable);

		Button showOriginalButton = (Button) findViewById(R.id.showOriginalButton);
		showOriginalButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				/* Check if we have an open imageView... */
				if (handler.getImage() != null || updateView) {
					/* Reset imageview. */
					imageView.setImageBitmap(handler.getImage());
					index = 0;
				} else {
					openNewImage();
				}

				if (updateView) {
					Log.i(TAG, "Setting updateView back to false...");
					updateView = false;
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
					Pass the file path for the original imageView, or the resource id.
				*/
				if (imgType == TYPE_RES) {
					intent.putExtra(INTENT_TYPE, TYPE_RES);

					/* The following isn't actually used, its also hardcoded in FullscreenActivity. */
					intent.putExtra(INTENT_IMAGE, DEFAULT_RES);
				} else {
					intent.putExtra(INTENT_TYPE, TYPE_FILE);
					intent.putExtra(INTENT_IMAGE, imgPath);
				}

				/* Also add the mode. */
				intent.putExtra(INTENT_MODE, mode);

				/* Add the int array of devices per row. */
				intent.putExtra(INTENT_DEVICES_PER_ROW, devicesPerRow);

				/* We do not send the splitted imgs, as they are saved in temporary files or recreated in the new activity. */
				startActivity(intent);

				/* Show a toast for better user experience (not that we care about that) :P */
				if (mode == MODE_NONE || mode == MODE_SERVER) {
					Toast toast = Toast.makeText(getApplicationContext(), "Click to rotate", Toast.LENGTH_SHORT);
					toast.show();
				}
			}
		});

		/* Open default imageView. */
		imageView.setImageResource(DEFAULT_RES);

//		imgs = handler.splitImg(rows, cols);
		imgs = handler.splitImgToDevices(devicesPerRow);
		deleteTempImageFiles(getCacheDir());
		saveImagesToFile(imgs);

		/* Show a toast for better user experience (not that we care about that) :P */
		if (mode == MODE_NONE || mode == MODE_SERVER) {
			Toast toast = Toast.makeText(getApplicationContext(), "Click imageView to rotate", Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	private void openNewImage() {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, REQ_CODE_PICK_IMAGE);
	}

	private void rotateSplittedImages() {
		if (mode == MODE_NONE || mode == MODE_SERVER) {
			/* Check if we have an open imageView... */
			if (handler.getImage() != null && imgs != null) {
				/* If we're at the start again, set the imageView to the original. */
				if (index == START) {
					if (imgType == MainActivity.TYPE_RES) {
						imageView.setImageResource(MainActivity.DEFAULT_RES);
					} else {
						imageView.setImageBitmap(handler.getImage());
					}
				} else {
					/* Else, rotate the splitted images. */
					imageView.setImageBitmap(imgs[index]);
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
		} else if (mode == MODE_CLIENT) {
			/* Show our received imageView. */
			if (handler.getImage() != null) {
				imageView.setImageBitmap(handler.getImage());
			} else {
				Log.e(TAG, "Client handler imageView is not set or null!");
			}
		}
	}

	public boolean saveImageToFile(String filename, Bitmap img) {
		if (filename == null || img == null) {
			return false;
		}

		/* Save the image to file. */
		File outputDir = getCacheDir();

		try {
			File outputFile = new File(outputDir, filename);
			OutputStream outStream = new FileOutputStream(outputFile);
			img.compress(Bitmap.CompressFormat.PNG, 100, outStream);
			outStream.flush();
			outStream.close();
		} catch (IOException e) {
			Log.e(TAG, "Failed to save file! " + e.getMessage());
			return false;
		}

		return true;
	}

	public static Bitmap openImageFromFile(File cacheDir, String filename) {
		try {
			File inputFile = new File(cacheDir, filename);
			FileInputStream fileInput = new FileInputStream(inputFile);
			Bitmap img = BitmapFactory.decodeStream(fileInput);
			fileInput.close();
			return img;
		} catch (IOException e) {
			Log.e(TAG, "Failed to read from file! " + e.getMessage());
			return null;
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

	/* Returns null upon failure. */
	public static Bitmap[] openTempImgFiles(File inputDir, int numDevices) {
		Bitmap[] imgs = new Bitmap[numDevices];
		boolean success = true;

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

		/* If we failed for some reason, return null. */
		if (success) {
			return imgs;
		} else {
			return null;
		}
	}

	/* List all fils in the cache dir for debug purposes. */
	public static File[] listTempImageFiles(File cacheDir) {
		File[] files = cacheDir.listFiles();

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

	/* Removes all created temporary saved imageView files, if any. */
	public static void deleteTempImageFiles(File cacheDir) {
		/* Get a list of all temp imageView files. */
		File[] files = listTempImageFiles(cacheDir);

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

					/* Open the imageView in the handler, and split it. */
					if (handler.getImage() != null) {
						imageView.setImageBitmap(handler.getImage());
						index = 0;

//						imgs = handler.splitImg(rows, cols);
						imgs = handler.splitImgToDevices(devicesPerRow);

						deleteTempImageFiles(getCacheDir());
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
		deleteTempImageFiles(getCacheDir());
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

	public void showDialog() {
		final Dialog dialog = new Dialog(MainActivity.this);
		dialog.setTitle("Set amount of columns");
		dialog.setContentView(R.layout.grid_dialog);
		Button b1 = (Button) dialog.findViewById(R.id.button1);
		Button b2 = (Button) dialog.findViewById(R.id.button2);

		final NumberPicker np = (NumberPicker) dialog.findViewById(R.id.numberPicker);
		np.setMinValue(1);
		np.setMaxValue(5);
		np.setWrapSelectorWheel(false);
		b1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				columns = Integer.parseInt(String.valueOf(np.getValue()));
				dialog.dismiss();
				Intent intent = new Intent(MainActivity.this, GridActivity.class);
				intent.putExtra("columnAmount", columns);
				startActivity(intent);
			}
		});

		b2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
		Toast toast = Toast.makeText(getApplicationContext(), "Found " + connections.size() + " devices", Toast.LENGTH_SHORT);
		toast.show();
	}

	@Override
	public void onConnect(MurtConnection conn) {
		Log.i(MurtConfiguration.TAG, "onConnect()");

		/* Keep track of connections and devices. */
		connections.put(conn.identifier, MainActivity.DEVICE_PREFIX + conn.identifier);
		Devices.deviceStrings.add(MainActivity.DEVICE_PREFIX + conn.identifier);
	}

	@Override
	public byte[] onSend(MurtConnection conn) {
		Log.i(MurtConfiguration.TAG, "onSend()");

		/* Send each client a part of the image. */

		if (imgs == null) {
			imgs = handler.splitImgToDevices(devicesPerRow);
		}

		/* Determine the index in the List of Device names. */
//		int index = Devices.deviceStrings.indexOf(MainActivity.DEVICE_PREFIX + conn.identifier);
//		Log.i(TAG, "index = " + index);
//
//		if (index == -1 || index >= imgs.length) {
//			return null;
//		}

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		imgs[0].compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] data = stream.toByteArray();
		Log.i(TAG, "Sending data array of length + " + data.length);
		return data;
	}

	@Override
	public void onReceive(byte[] data) {
		Log.i(MurtConfiguration.TAG, "onReceive()");

		/* Verify the received image, and set it to our own. */
		if (data == null || data.length <= 1) {
			Log.e(TAG, "Data is null or length <= 1!");
			return;
		}

		Log.i(TAG, "data is not null, length = " + data.length);

		Log.i(TAG, "Starting Bitmap decoding...");
		Bitmap temp = BitmapFactory.decodeByteArray(data, 0, data.length);

		if (temp == null) {
			Log.e(TAG, "temp is null!");
		}

		handler.setBitmap(temp);
		Log.i(TAG, "Bitmap decoding done?");

		if (handler.getImage() != null) {
			Log.i(TAG, "View needs to be updated!");
			updateView = true;
		}

		/* Save the image to file... */
		imgPath = "image.png";
		boolean status = saveImageToFile(imgPath, handler.getImage());

		if (!status) {
			Log.e(TAG, "Failed to save image!");
		}
	}

	public void onDisconnect(MurtConnection conn) {
		Log.i(MurtConfiguration.TAG, "onDisconnect()");

		connections.remove(conn.identifier);
		Devices.deviceStrings.remove(MainActivity.DEVICE_PREFIX + conn.identifier);
	}
}
