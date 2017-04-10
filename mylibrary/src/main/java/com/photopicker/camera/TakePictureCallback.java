package com.photopicker.camera;

import android.graphics.Bitmap;

/**
 * Created by liepin on 2017/4/6.
 */

public interface TakePictureCallback {
    public void prepareTake();

    public void onTake(byte[] data);

    public void onTake(Bitmap bitmap);
}
