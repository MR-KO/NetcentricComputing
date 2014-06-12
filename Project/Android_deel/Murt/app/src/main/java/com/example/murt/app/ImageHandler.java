package com.example.murt.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.*;

/**
 * Handles image opening, splitting and assembling.
 */

public class ImageHandler {
    public static String TAG = "ImageHandler";
    private Bitmap img = null;
    private String imgName = null;
    private int width = -1;
    private int height = -1;

    /* Returns null if no image has been opened. */
    public Bitmap getImage() { return img; }

    /* Returns -1 if no image has been opened. */
    public int getWidth() {
        return width;
    }

    /* Returns -1 if no image has been opened. */
    public int getHeight() {
        return height;
    }

    /* Opens an image from a drawable. */
    public boolean open(Drawable drawable) {
        img = ((BitmapDrawable) drawable).getBitmap();
        width = img.getWidth();
        height = img.getHeight();
        return true;
    }

    /* Opens an image from a given path. */
    public boolean open(String path) {
        Log.i(TAG, "Opening file from path: " + path + "...");
        File imgFile = new File(path);

        if (imgFile.exists()) {
            Log.i(TAG, "File exists!");
            img = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            width = img.getWidth();
            height = img.getHeight();
            return true;
        }

        return false;
    }

    /*
        Splits the image in a number of smaller images, which combined together
        appropriately, make the original image. The minimum values for width and
        height are 1, and the maximum values are the width and height of the
        image, respectively. Columns are interpreted as being the height, and
        rows being the width.
    */
    public Bitmap[] splitImg(int cols, int rows) {
        Log.i(TAG, "splitImg");

        /* Bounds check... */
        if (cols < 1 || cols > height || rows < 1 || rows > width || img == null) {
            return null;
        }

		/* What if the width and height are not evenly divisible by cols and rows? */
        // TODO: MURT!
        int parts = cols * rows;
        int partWidth = width / cols;
        int partHeight = height / rows;

        Log.i(TAG, "Checks passed, parts = " + parts + ", w = " + partWidth + ", h = " + partHeight);

		/*
			Create an array of Bitmaps and store a part of each image in
			each element of it, then return that array.
		*/
        int index = 0;
        Bitmap[] splittedImages = new Bitmap[parts];

        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
				/* Split the image into parts and display them. */
                splittedImages[index] = Bitmap.createBitmap(img, partWidth * x, partHeight * y,
                        partWidth, partHeight);
                index++;
            }
        }

        Log.i(TAG, "Returning splittedImages...");
        return splittedImages;
    }

    /* Assembles a split image back into its original version. */
    public static Bitmap assemble(Bitmap[] imgs, int cols, int rows) {
		/* Checks... */
        if (imgs == null || imgs.length != cols * rows || cols < 1 || rows < 1) {
            return null;
        }

		/* What if the width and height are not evenly divisible by cols and rows? */
        // TODO: MURT!
        int parts = cols * rows;
        int partWidth = imgs[0].getWidth();
        int partHeight = imgs[0].getHeight();

		/* Re-create the final image with the correct size. */
        Bitmap finalImg = Bitmap.createBitmap(parts * partWidth, parts * partHeight, imgs[0].getConfig());

		/* Fill the final image with all the parts. */
        int index = 0;

		/* Make sure to reverse the order used in splitting... */
        for (int y = 0; y < cols; y++) {
            for (int x = 0; x < rows; x++) {
                //
                index++;
            }
        }

        return finalImg;
    }
}
