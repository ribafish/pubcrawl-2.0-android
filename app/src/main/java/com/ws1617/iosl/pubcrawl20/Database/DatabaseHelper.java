package com.ws1617.iosl.pubcrawl20.Database;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * Created by Gasper Kojek on 20. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class DatabaseHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "PubCrawl20.db";
    private static final String YES = "Y";

    public static byte[] bitmapToBytes (Bitmap bmp) {
        if (bmp == null) return null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    public static Bitmap bytesToBitmap (byte[] image) {
        if (image == null) return null;
        else if (image.length == 0) return null;
        else return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

}
