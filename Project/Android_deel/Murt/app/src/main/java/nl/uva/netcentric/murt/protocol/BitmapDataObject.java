package nl.uva.netcentric.murt.protocol;

import java.io.Serializable;

/**
 * Created by Sjoerd on 22-6-2014.
 *
 * Credits to John Wordsworth
 * http://stackoverflow.com/questions/5871482/serializing-and-de-serializing-android-graphics-bitmap-in-java
 *
 *
 */
public class BitmapDataObject implements Serializable {


    public byte[] bitmapBytes;

    public BitmapDataObject(final byte[] bitmapBytes) {
        this.bitmapBytes = bitmapBytes;
    }


}
