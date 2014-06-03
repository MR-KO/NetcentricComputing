/*
 * mbedADKSketch
 * 
 * Written by p07gbar
 * 
 * This library allows the mbed to be used for a controller of an "etch-a-sketch" clone
 * 
 * 
 */

package mbed.mbedsketch;


import mbed.adkPort.AdkPort;

import java.io.IOException;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;


public class MbedADKSketchActivity extends Activity {
		
	DisplayMetrics mMetrics;			// Screen size, DPI etc
	
	DrawView drawView;					// Instance of the display class
	
	AdkPort mbed;						// Instance of the ADK Port class
	
	int colcount = 0;					// A variable for changing the colour
	private static int statH = 48;		// The height of the bars (top and bottom) combined

	boolean mbed_attached = false;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		// Set full screen view
		 requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                         WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        drawView = new DrawView(this);
        //Imports the bitmap from the resource
        Resources res = getResources();
        BitmapFactory.Options conf = new BitmapFactory.Options();
        conf.inPreferredConfig = Config.ARGB_8888;
        drawView.setLogo(BitmapFactory.decodeResource(res, R.drawable.logo_android_sdk,conf));
        
        //Gets the size of the screen
        mMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
		// Tell the drawView how big the screen is, so it knows where to draw things
        drawView.informSize(mMetrics.widthPixels,mMetrics.heightPixels-statH);
        
        
       // Sets up the drawView which the user sees
        
        setContentView(drawView);
        drawView.requestFocus();
        
        
        // Initialises the instance of ADKPort with the context
		try {
			mbed = new AdkPort(this);
		} catch (IOException e) {
			return;
		}
		
		// Attaches a function which is called on new, as a MessageNotifier interface, onNew called when new bytes are recived
		mbed.attachOnNew(new AdkPort.MessageNotifier(){ 
			@Override
			public void onNew()
			{
				byte[] in = mbed.readB();
				switch(in[0])
				{
				case 'P':
					
					int x = bytetoint(in[1]) + (bytetoint(in[2]) *256);						// Re-asemble the numbers from the byte packing
					int y = (bytetoint(in[3]) + (bytetoint(in[4]) *256));					// For y too

					x = (int)((float)x * (float)mMetrics.widthPixels / 10000);				// Scale the values to fit the screen
					y = (int)((float)y * (float)(mMetrics.heightPixels-statH) / 10000);		// Scale the values to fit the available screen space
					float[] hsv = new float[3];												// To contstruct the colour
					hsv[0] = colcount;														// Hue
					hsv[1] = 1;
					hsv[2] = 1;
					int col = Color.HSVToColor(hsv);										// Make the hsv into a colour
 					drawView.newCoords(x, y,col);											// Add a point to the draw view
					colcount++;																// Increase the colours hue by one
					if(colcount >360) colcount = 0;											// Wrap the hue
					//mbed.sendString("C : " + drawView.numCords());						  // Commented out to demonstrate a way of sending back data, can be used for debugging
					break;
					default:

						break;
				}
			}
		});
		

		Thread thread = new Thread(mbed);			// Set up an instance of the mbed thread
		thread.start();								// start it
		mbed.sendString("GO");						// Tell it to send "go" to the mbed, so the mbed starts sending pot values
	}

	@Override
	public void onDestroy() {
		if (mbed != null)
			mbed.onDestroy(this);

		super.onDestroy();

	}
	
	@Override
	public void onPause() {
		if (mbed != null)
			mbed.closeAccessory();

		super.onPause();
	}
	
	@Override
	public void onResume() {
		if (mbed != null)
			mbed.openAccessory();
		
		super.onResume();
	}
	
	private int bytetoint(byte b) {				// Deals with the twos complement problem that bit packing presents
		int t = ((Byte)b).intValue();
		if(t < 0)
		{
			t += 256;
		}
		return t;
	}



}
