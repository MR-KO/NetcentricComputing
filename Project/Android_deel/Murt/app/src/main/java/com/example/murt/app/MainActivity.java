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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
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
import java.util.Arrays;

import nl.uva.netcentric.murt.protocol.AndroidMurtClient;
import nl.uva.netcentric.murt.protocol.AndroidMurtServer;
import nl.uva.netcentric.murt.protocol.DeviceConfig;
import nl.uva.netcentric.murt.protocol.MurtConfiguration;
import nl.uva.netcentric.murt.protocol.MurtConnection;
import nl.uva.netcentric.murt.protocol.MurtConnectionListener;


public class MainActivity extends Activity implements MurtConnectionListener, View.OnTouchListener {

	public static final String TAG = "MainActivity";

	public static final String INTENT_TYPE = "com.example.murt.app.type";
	public static final String INTENT_IMAGE = "com.example.murt.app.image";
	public static final String INTENT_DEVICES_PER_ROW = "com.example.murt.app.dev_per_rows";
	public static final String INTENT_MODE = "com.example.murt.app.mode";

	public static final String SPLIITED_IMGS_PREFIX = "split_";
	public static final String SPLITTED_IMGS_EXT = ".png";

	public static final int TYPE_RES = 1;
	private int imgType = TYPE_RES;
	public static final int TYPE_FILE = 2;
	public static final int DEFAULT_RES = R.drawable.tap;

	/* Used for server/client stuff */
	public static final int MODE_NONE = 0;
	private int mode = MODE_NONE;
	public static final int MODE_CLIENT = 1;
	public static final int MODE_SERVER = 2;
	public static final String DEVICE_PREFIX = "MurtDevice ";
	public static final String DEVICE_MASTER = DEVICE_PREFIX + "Master";

	/* Start indicates the original imageView, [0, imgs.length - 1] indicates the splitted images. */
	private int rowIndex = 0;

	/* Used for selecting imageView */
	private final static int REQ_CODE_PICK_IMAGE = 1;
	private static MainActivity instance;
	private String imgPath = "";
	private ImageView imageView = null;
	private ImageHandler handler = null;
	private Bitmap[] imgs = null;
	private int[] devicesPerRow = {1, 1};
	private boolean layoutChosen = false;
	private boolean[] layoutNeedsUpdate;
	private int columns = -1;
	private AndroidMurtServer server;
	private AndroidMurtClient client;
	private NsdManager nsdManager;
	private boolean updateView = false;

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
			Log.i(TAG, "Cache dir.listFiles() is empty array!");
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
		for (int i = 0; i < files.length; i++) {
			boolean status = files[i].delete();

			if (!status) {
				Log.e(TAG, "Failed to delete file: " + files[i].getAbsolutePath());
			}
		}
	}

	public static void toast(String text) {
		toast(text, Toast.LENGTH_LONG);
	}

	// Allows other threads to toast as well
	public static void toast(final String text, final int duration) {
		instance.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(instance, text, duration).show();
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		instance = this;
		Log.i(TAG, "MainActivity.onCreate()!");

		if (MurtConfiguration.USE_NSD) {
			nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
		} else {
			toast("You device does not support nsd, server mode/dynamic client mode disabled!");
		}

		Log.i(TAG, "Mainactivity nsdManager done!");

		Button gridButton = (Button) findViewById(R.id.gridButton);
		gridButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showDialog();
			}
		});

		Button tapGridButton = (Button) findViewById(R.id.tapGridButton);
		tapGridButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, TapGridActivity.class);
//                intent.putExtra("connections", connections);
				startActivity(intent);
			}
		});

		Button masterButton = (Button) findViewById(R.id.masterButton);

		masterButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (MurtConfiguration.USE_NSD) {
					cleanup();
					mode = MODE_SERVER;
					server = new AndroidMurtServer(nsdManager, MainActivity.this, MurtConfiguration.DEBUG_PORT);
				}
			}
		});

		Button clientButton = (Button) findViewById(R.id.clientButton);
		clientButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				initClient(DeviceConfig.OLDSKOOL);
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
				// todo debug stuff
				//rotateSplittedImages();
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

		imageView.setOnTouchListener(this);

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

		/* Add ourself to the Devices list. */
		if (!Devices.initialized) {
			Devices.deviceStrings.clear();
			Devices.deviceStrings.add(MainActivity.DEVICE_PREFIX + "Master");
			Devices.connections.clear();
			Devices.connections.put(-1, MainActivity.DEVICE_PREFIX + "Master");
			Devices.initialized = true;
		}

		printDevicesAndConnections();

		Log.i(TAG, "MainActivity onCreate done!");
	}

	private void initClient(Integer config) {
		cleanup();
		mode = MODE_CLIENT;

		client = new AndroidMurtClient(nsdManager, MainActivity.this, config);
	}

	private void openNewImage() {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, REQ_CODE_PICK_IMAGE);
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

	/* Used for selecting an image from gallery. */
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

	protected void cleanup() {
		if (mode == MODE_SERVER && server != null) {
			server.stop();
		} else if (mode == MODE_CLIENT && client != null) {
			client.stop();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		deleteTempImageFiles(getCacheDir());
		cleanup();
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

		return (id == R.id.action_settings) || super.onOptionsItemSelected(item);
	}

	private boolean setDevicesPerRow(int numDevices, int columns) {
		if (numDevices < 0 || columns < 1) {
			return false;
		}

		/* Fill the devicesPerRow array with the right amount of devices per row. */
		if (numDevices % columns == 0) {
			int rows = numDevices / columns;
			devicesPerRow = new int[rows];

			for (int i = 0; i < rows; i++) {
				devicesPerRow[i] = columns;
			}
		} else {
			int rows = numDevices / columns + 1;
			devicesPerRow = new int[rows];

			for (int i = 0; i < rows - 1; i++) {
				devicesPerRow[i] = columns;
			}

			devicesPerRow[rows - 1] = numDevices % columns;
		}

		return true;
	}

	private int getIndex(int identifier) {
		int index = Devices.deviceStrings.indexOf(MainActivity.DEVICE_PREFIX + identifier);
//		Log.i(TAG, "index = " + index);

		if (index == -1 || index >= imgs.length) {
			return -1;
		}

		return index;
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
				layoutChosen = true;
				resetLayoutNeedsUpdate(true);

				/* Set the devicesPerRow array. */
				setDevicesPerRow(Devices.deviceStrings.size(), columns);
				imgs = handler.splitImgToDevices(devicesPerRow);

				deleteTempImageFiles(getCacheDir());
				saveImagesToFile(imgs);

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
		Toast toast = Toast.makeText(getApplicationContext(), "Found " + Devices.connections.size() + " devices", Toast.LENGTH_SHORT);
		toast.show();
	}

	public void printDevicesAndConnections() {
		Log.e(TAG, "Devices.deviceStrings: ");
		Log.e(TAG, Devices.deviceStrings.toString());

		Log.e(TAG, "Connections: ");
		Log.e(TAG, Devices.connections.toString());
	}

	private void resetLayoutNeedsUpdate(boolean value) {
		int size = Devices.connections.size();

		if (size > 0) {
			layoutNeedsUpdate = new boolean[size];

			for (int i = 0; i < size; i++) {
				layoutNeedsUpdate[i] = value;
			}
		}
	}

	private void addToDevicesRow(int config) {
		if (config == DeviceConfig.DEFAULT) {
			/* The order of the devices is done above... */
			layoutChosen = true;
			devicesPerRow[rowIndex]++;

			/* Re-split the images. */
			imgs = handler.splitImgToDevices(devicesPerRow);
		} else if (config == DeviceConfig.END_ROW) {
			/* "Resize" the array such that the next connect will be on the next row. */
			layoutChosen = true;
			devicesPerRow[rowIndex]++;
			devicesPerRow = Arrays.copyOf(devicesPerRow, devicesPerRow.length + 1);
			rowIndex++;
			devicesPerRow[rowIndex] = 0;

			/* Re-split the images. */
			imgs = handler.splitImgToDevices(devicesPerRow);
		} else {
			/* Old-skool method. Do nothing. */
		}
	}

	@Override
	public void onConnect(MurtConnection conn, Integer config) {
		Log.i(MurtConfiguration.TAG, "onConnect()");
		toast("Device " + conn.identifier + " connected with config " + config, Toast.LENGTH_SHORT);
		printDevicesAndConnections();

		/* Keep track of connections and devices. */
		Devices.connections.put(conn.identifier, MainActivity.DEVICE_PREFIX + conn.identifier);
		Devices.deviceStrings.add(MainActivity.DEVICE_PREFIX + conn.identifier);

		/* TODO: set the layout according to config. */
		resetLayoutNeedsUpdate(true);
		addToDevicesRow(config);

		/* Log the current connections and devices. */
		printDevicesAndConnections();
	}

	@Override
	public byte[] onSend(MurtConnection conn) {
//		Log.i(MurtConfiguration.TAG, "onSend()");

		/* Only send a new Bitmap if we have chosen the layout. */
		if (!layoutChosen) {
			return null;
		}

		/* Send each client a part of the image. */
		if (imgs == null) {
			imgs = handler.splitImgToDevices(devicesPerRow);
		}

		/* Determine the index in the List of Device names. */
		int index = getIndex(conn.identifier);

		if (index == -1) {
			return null;
		}

		/* Only send an updated bitmap if the layout needed to be updated. */
		if (!layoutNeedsUpdate[index]) {
			return null;
		}

		layoutNeedsUpdate[index] = false;

		/* Convert the bitmap to a byte array. */
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		imgs[index].compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] data = stream.toByteArray();
		Log.i(TAG, "Index = " + index + ". Sending data array of length + " + data.length + " to connection id " + conn.identifier);
		return data;
	}

	@Override
	public void onReceive(byte[] data) {
		Log.i(MurtConfiguration.TAG, "onReceive()");
		/* Log the current connections and devices. */
		printDevicesAndConnections();

		/* Verify the received image, and set it to our own. */
		if (data == null || data.length <= 1) {
			Log.e(TAG, "Data is null or length <= 1!");
			return;
		}

		Log.i(TAG, "data is not null, length = " + data.length);
//		int n = 4;
//		String temp1 = "";
//		String temp2 = "";
//
//		for (int i = 0; i < n; i++) {
//			temp1 += data[i] + ", ";
//			temp2 += data[data.length - i - 1] + ", ";
//		}
//
//		Log.i(TAG, "first " + n + " bytes of data: " + temp1 + ", last " + n + " bytes of data: " + temp2);

//		int foundConsecutiveZeroes = 0;
//
//		for (int i = 0; i < data.length; i++) {
//			if (data[i] == 0) {
//				Log.i(TAG, "data is 0 at index " + i);
//				foundConsecutiveZeroes++;
//
//				if (foundConsecutiveZeroes > 25) {
//					break;
//				}
//			} else {
//				foundConsecutiveZeroes = 0;
//			}
//		}

		Log.i(TAG, "Starting Bitmap decoding...");
		Bitmap temp = BitmapFactory.decodeByteArray(data, 0, data.length);

		if (temp == null) {
			Log.e(TAG, "temp is null!");
		}

		handler.setBitmap(temp);
		Log.i(TAG, "Bitmap decoding done!");

		if (handler.getImage() != null) {
			Log.i(TAG, "View needs to be updated!");
			updateView = true;
		}

		/* Save the image to file... */
		imgPath = "image.png";
		imgType = TYPE_FILE;
		boolean status = saveImageToFile(imgPath, handler.getImage());
		imgPath = getCacheDir() + "/" + imgPath;

		if (!status) {
			Log.e(TAG, "Failed to save image!");
		}

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (handler.getImage() != null) {
					imageView.setImageBitmap(handler.getImage());
				}
			}
		});
	}

	public void onDisconnect(MurtConnection conn) {
		Log.i(MurtConfiguration.TAG, "onDisconnect()");

		if (mode == MODE_SERVER) {
			toast("Device " + conn.identifier + " disconnected", Toast.LENGTH_SHORT);
			printDevicesAndConnections();

			if (Devices.connections.containsKey(conn.identifier)) {
				int deviceIndex = Devices.deviceStrings.indexOf(MainActivity.DEVICE_PREFIX + conn.identifier);

				Devices.connections.remove(conn.identifier);
				Devices.deviceStrings.remove(MainActivity.DEVICE_PREFIX + conn.identifier);

				/* Entire layout needs to be reset. */
				resetLayoutNeedsUpdate(true);

				/*
					Change the layout such that the device gets removed and the other devices
					take its part. Remove the device from the devicesPerRow array.
				*/
				int sum = 0;

				for (int i = 0; i < devicesPerRow.length; i++) {
					sum += devicesPerRow[i];

					/* The device was in this row. */
					if (deviceIndex < sum) {
						devicesPerRow[i]--;
					}
				}

				/* Re-split the images. */
				imgs = handler.splitImgToDevices(devicesPerRow);
			} else {
				Log.d(TAG, "onDisconnect unknown connection!");
			}

			/* Log the current connections and devices. */
			printDevicesAndConnections();
		} else {
			toast("Server disconnected!", Toast.LENGTH_SHORT);
		}
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {

		// Get the pointer ID
		int mActivePointerId = event.getPointerId(0);

		// Use the pointer ID to find the index of the active pointer
		// and fetch its position
		int pointerIndex = event.findPointerIndex(mActivePointerId);

		// Get the pointer's current position
		float x = event.getX(pointerIndex);
		float y = event.getY(pointerIndex);

		if (mode == MODE_NONE) {
			if (x >= imageView.getWidth() / 2) {
				initClient(DeviceConfig.END_ROW);
			} else {
				initClient(DeviceConfig.DEFAULT);
			}
		}

		if (mode == MODE_SERVER) {
			/* Remove and re-append ourself from the deviceStrings array... */
			Devices.deviceStrings.remove(MainActivity.DEVICE_MASTER);
			Devices.deviceStrings.add(MainActivity.DEVICE_MASTER);

			if (x >= imageView.getWidth() / 2) {
				addToDevicesRow(DeviceConfig.END_ROW);
			} else {
				addToDevicesRow(DeviceConfig.DEFAULT);
			}
		}

		Log.i(MurtConfiguration.TAG, "id=" + pointerIndex + ", x=" + x + ", y=" + y);

		return false;
	}
}
