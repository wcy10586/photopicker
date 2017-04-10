package com.photopicker.camera;

/**
 * Created by liepin on 2017/4/6.
 */

public interface CameraListener {
    public void onFlashLigChange(boolean show, String mode);

    public void onCameraChange(boolean chanSwitch, int cameraId);

    public void onFocusIndex(float x,float y);
}
