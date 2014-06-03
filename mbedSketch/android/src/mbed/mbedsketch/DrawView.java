/*
 * mbedADKSketch
 * 
 * Written by p07gbar
 * 
 * This library allows the mbed to be used for a controller of an "etch-a-sketch" clone
 */

package mbed.mbedsketch;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class DrawView extends View implements OnTouchListener {
	
    private static int THRESH = 50;
    private static final String[] idleMsg = {"This program was developed with the ADKPort libarary, developed at ARM.","It is a simple example of what could be done with the ADK and mbed"};
    
    
    private int count = 0;

    List<Point> points = new ArrayList<Point>(10000);
    Paint paint = new Paint();
    
    private ShakeListener mShaker;
    private int prevx, prevy;
    private Bitmap logo;
    private int logoW, logoH;
    private int width = 0;
    private int height = 0;

	private boolean logodef = false;

    public DrawView(Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setOnTouchListener(this);
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        logo = null;
        mShaker = new ShakeListener(context);
        mShaker.setOnShakeListener(new ShakeListener.OnShakeListener () {

			@Override
			public void onShake() {
				// TODO Auto-generated method stub
				points.clear();
			}
        	
        });
    }

    @Override
    public void onDraw(Canvas canvas) {
    	canvas.drawARGB(255, 0, 0, 0);
    	
    	if(logodef && width != 0)canvas.drawBitmap(logo,width-logoW,(height - logoH),paint);
    	if(count < THRESH) 
    		{
    		paint.setColor(Color.argb((int)((THRESH-count)*2.55*(100/THRESH)),255,255,255));
        	paint.setTextSize(20);
    		canvas.drawText(idleMsg[0], 20,height - 70 , paint);
    		canvas.drawText(idleMsg[1], 20,height - 40 , paint);
    		}
    	count -= 5;
        for (int i = 1; i<points.size(); i++) {
       
        	if(points.get(i).colset)
        	{
        		paint.setColor(points.get(i).color);
        	}
            canvas.drawLine(points.get(i).x, points.get(i).y, points.get(i-1).x, points.get(i-1).y, paint);
            
        }
    }

    
    
    public void newCoords(int x, int y)
    {
    	if(points.size() > 1)
    	{
    	if(abs(prevx - x) < 300 && abs(prevy - y) < 300)
    	{
    		Point point = new Point();
    		point.x = x;
    		point.y = y;
    		point.colset = false;
    		points.add(point);
    		count += (abs(x-prevx) + abs(y-prevy));
    		count = constrain(count,0,100);
    		invalidate();
    		prevx = x;
    		prevy = y;
    		
    	}
    	else
    	{
    		prevx = x;
    		prevy = y;
    	}
    	}
    	else
    	{
    		Point point = new Point();
    		point.x = x;
    		point.y = y;
    		point.colset = false;
    		points.add(point);
    		invalidate();
    		prevx = x;
    		prevy = y;
    		count++;
    	}
    }
    
    public void setLogo(Bitmap logoin)
    {
    	logo = logoin;
    	logoW = logo.getWidth();
    	logoH = logo.getHeight();
    	logodef  = true;
    }

	private int abs(int f) {
		if(f < 0)
		{
			f = -1*f;
		}
		// TODO Auto-generated method stub
		return f;
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		points.clear();
		return true;
	}
	
	public int numCords()
	{
		return count;
		//return points.size();
	}

	public void newCoords(int x, int y, int colour) {
		// TODO Auto-generated method stub
		if(points.size() > 1)
    	{
    	if(abs(prevx - x) < 300 && abs(prevy - y) < 300)
    	{
    		Point point = new Point();
    		point.x = x;
    		point.y = y;
    		point.color = colour;
    		point.colset = true;
    		points.add(point);
    		invalidate();
    		count += (abs(x-prevx) + abs(y-prevy));
    		count = constrain(count,0,100);
    		prevx = x;
    		prevy = y;
    	}
    	else
    	{
    		prevx = x;
    		prevy = y;
    	}
    	}
    	else
    	{
    		Point point = new Point();
    		point.x = x;
    		point.y = y;
    		point.color = colour;
    		point.colset = true;
    		points.add(point);
    		invalidate();
    		prevx = x;
    		prevy = y;
    	}
	}

	public void informSize(int widthPixels, int heightPixels) {
		width = widthPixels;
		height = heightPixels;
	}
	
	private int constrain(int in, int hi, int low) {
		if (hi < low) {
			int temp = hi;
			
			hi = low;
			low = temp;
		}
		if (in > hi)
			in = hi;
		if (in < low)
			in = low;
		return in;
	}
}

class Point {
    float x, y;
    int color;
    boolean colset;
    @Override
    public String toString() {
        return x + ", " + y;
    }
}