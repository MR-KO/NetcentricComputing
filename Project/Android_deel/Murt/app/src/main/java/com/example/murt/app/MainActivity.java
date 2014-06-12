package com.example.murt.app;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


public class MainActivity extends Activity {

    private Button gridButton;
    private Button openImageButton;
    private static boolean imageIsOpen = false;
    private Button showOriginalButton;
    private Button rotateButton;

    private ImageView image;
    private ImageHandler handler;
    private Bitmap[] imgs;
    private int index = 0;

    // Used for selecting image
    private final static int REQ_CODE_PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridButton = (Button)findViewById(R.id.gridButton);
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
        
        imgs = handler.splitImg(2, 2);

        openImageButton = (Button)findViewById(R.id.openImageButton);
        openImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNewImage();
            }
        });

        showOriginalButton = (Button)findViewById(R.id.showOriginalButton);
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

        rotateButton = (Button)findViewById(R.id.rotateButton);
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
                    }

                    imgs = handler.splitImg(2, 2);
                }
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
