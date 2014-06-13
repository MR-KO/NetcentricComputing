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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class MainActivity extends Activity {
	public static String TAG = "MainActivity";


	public final static String INTENT_TYPE = "com.example.murt.app.type";
	public final static String INTENT_ORIGINAL_IMAGE = "com.example.murt.app.original";
	public final static String INTENT_SPLITTED_IMGS = "com.example.murt.app.imgs";
	public final static String INTENT_ROWS = "com.example.murt.app.rows";
	public final static String INTENT_COLS = "com.example.murt.app.cols";

	public final static String SPLIITED_IMGS_PREFIX = "split_";
	public final static String SPLITTED_IMGS_EXT = "png";

	public final static int TYPE_RES = 1;
	public final static int TYPE_FILE = 2;

    private boolean imageIsOpen = false;
	private int imgType = TYPE_RES;
	private String imgPath = "";

    private ImageView image;
    private ImageHandler handler;
    private Bitmap[] imgs;
    private int index = 0;
	private int rows = 2;
	private int cols = 2;

    // Used for selecting image
    private final static int REQ_CODE_PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

	    Button gridButton = (Button)findViewById(R.id.gridButton);
        gridButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launch = new Intent(MainActivity.this, GridActivity.class);
                startActivity(launch);
            }
        });

        image = (ImageView)findViewById(R.id.imageView);
        image.setImageResource(R.drawable.prepare);

        handler = new ImageHandler();
        Drawable drawable = getResources().getDrawable(R.drawable.prepare);
        handler.open(drawable);

        if (handler.getImage() != null) {
            imageIsOpen = true;
        }

        imgs = handler.splitImg(rows, cols);

        Button openImageButton = (Button)findViewById(R.id.openImageButton);
        openImageButton.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View view) {
		        openNewImage();
	        }
        });

        Button showOriginalButton = (Button)findViewById(R.id.showOriginalButton);
        showOriginalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* Check if we have an open image... */
                if (imageIsOpen) {
                    /* Reset imageview. */
                    image.setImageBitmap(handler.getImage());
                    index = 0;
                } else {
                    openNewImage();
                }
            }
        });

        Button rotateButton = (Button)findViewById(R.id.rotateButton);
        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* Check if we have an open image... */
                if (imageIsOpen) {
                    /* Set imageview to the next splitted image. */
                    image.setImageBitmap(imgs[index]);
                    index = (index + 1) % imgs.length;
                } else {
                    openNewImage();
                }
            }
        });

	    Button fullscreenButton = (Button)findViewById(R.id.fullscreenButton);
	    fullscreenButton.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View view) {
			    Intent intent = new Intent(MainActivity.this, FullscreenActivity.class);

			    /*
			        2 Types of intents, one with resource, one with file path.
			        Pass the file path for the original image, or the resource id.
			    */
			    if (imgType == TYPE_RES) {
				    intent.putExtra(INTENT_TYPE, TYPE_RES);
				    /* The following isn't actually used, its also hardcoded in FullscreenActivity. */
				    intent.putExtra(INTENT_ORIGINAL_IMAGE, R.drawable.prepare);
			    } else {
				    intent.putExtra(INTENT_TYPE, TYPE_FILE);
				    intent.putExtra(INTENT_ORIGINAL_IMAGE, imgPath);
			    }

			    /* Add the amount of rows and cols splitted. */
			    intent.putExtra(INTENT_ROWS, rows);
			    intent.putExtra(INTENT_COLS, cols);

			    /* We do not send the splitted imgs, as they are saved in temporary files or recreated in the new activity. */
			    startActivity(intent);
		    }
	    });
    }

	@Override
	public void finish() {
		super.finish();

		/* TODO: Remove all created temporary files, if any. */
	}

	private void openNewImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQ_CODE_PICK_IMAGE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case REQ_CODE_PICK_IMAGE:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(
                            selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();

                    handler.open(filePath);

                    if (handler.getImage() != null) {
	                    imageIsOpen = true;
	                    image.setImageBitmap(handler.getImage());
	                    index = 0;
	                    imgs = handler.splitImg(rows, cols);

	                    /* Save splitted images to temporary files. */
	                    if (imgs != null) {
		                    File outputDir = getCacheDir();

		                    for (int i = 0; i < imgs.length; i++) {
		                        try {
			                        File outputFile = File.createTempFile(SPLIITED_IMGS_PREFIX + i, SPLITTED_IMGS_EXT, outputDir);
			                        OutputStream outStream = new FileOutputStream(outputFile);
			                        imgs[i].compress(Bitmap.CompressFormat.PNG, 100, outStream);
			                        outStream.flush();
			                        outStream.close();
			                    } catch (IOException e) {
				                    Log.e(TAG, e.getMessage());
			                    }
		                    }
	                    }

	                    imgPath = filePath;
	                    imgType = TYPE_FILE;
                    }
                }
        }
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();

		/* Remove all created temporary files, if any. */
		boolean status = true;
		int i = 0;

		/* Status becomes false if we failed to delete the file. */
		while (status) {
			status = deleteFile(MainActivity.SPLIITED_IMGS_PREFIX + i + "." + MainActivity.SPLITTED_IMGS_EXT);
			i++;
		}
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
