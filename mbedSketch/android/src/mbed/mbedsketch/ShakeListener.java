package mbed.mbedsketch;

/* The following code was written by Matthew Wiggins and subsequently modified by p07gbar to work with the new library, rather than the older simpler one.
 * and is released under the APACHE 2.0 license
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Context;
import java.lang.UnsupportedOperationException;

public class ShakeListener implements SensorEventListener 
{
  private static final int FORCE_THRESHOLD = 350;
  private static final int TIME_THRESHOLD = 100;
  private static final int SHAKE_TIMEOUT = 500;
  private static final int SHAKE_DURATION = 300;
  private static final int SHAKE_COUNT = 3;

  private SensorManager mSensorMgr;
  private Sensor mAccel;
  private float mLastX=-1.0f, mLastY=-1.0f, mLastZ=-1.0f;
  private long mLastTime;
  private OnShakeListener mShakeListener;
  private int mShakeCount = 0;
  private long mLastShake;
  private long mLastForce;

  public interface OnShakeListener
  {
    public void onShake();
  }

  public ShakeListener(Context context) 
  { 
    resume(context);
  }

  public void setOnShakeListener(OnShakeListener listener)
  {
    mShakeListener = listener;
  }

  public void resume(Context context) {
	  mSensorMgr = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
      mAccel = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    if (mSensorMgr == null) {
      throw new UnsupportedOperationException("Sensors not supported");
    }
    boolean supported = mSensorMgr.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_GAME);
    if (!supported) {
      mSensorMgr.unregisterListener(this, mAccel);
      throw new UnsupportedOperationException("Accelerometer not supported");
    }
  }

  public void pause() {
    if (mSensorMgr != null) {
      mSensorMgr.unregisterListener(this, mAccel);
      mSensorMgr = null;
    }
  }


  public void onSensorChanged(int sensor, float[] values) 
  {
    
  }

@Override
public void onAccuracyChanged(Sensor arg0, int arg1) {
	// TODO Auto-generated method stub
	
}

@Override
public void onSensorChanged(SensorEvent sensorE) {
	// TODO Auto-generated method stub
	if (sensorE.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;
    long now = System.currentTimeMillis();

    if ((now - mLastForce) > SHAKE_TIMEOUT) {
      mShakeCount = 0;
    }

    if ((now - mLastTime) > TIME_THRESHOLD) {
      long diff = now - mLastTime;
      float speed = Math.abs(sensorE.values[SensorManager.DATA_X] + sensorE.values[SensorManager.DATA_Y] + sensorE.values[SensorManager.DATA_Z] - mLastX - mLastY - mLastZ) / diff * 10000;
      if (speed > FORCE_THRESHOLD) {
        if ((++mShakeCount >= SHAKE_COUNT) && (now - mLastShake > SHAKE_DURATION)) {
          mLastShake = now;
          mShakeCount = 0;
          if (mShakeListener != null) { 
            mShakeListener.onShake(); 
          }
        }
        mLastForce = now;
      }
      mLastTime = now;
      mLastX = sensorE.values[SensorManager.DATA_X];
      mLastY = sensorE.values[SensorManager.DATA_Y];
      mLastZ = sensorE.values[SensorManager.DATA_Z];
    }
}

}

