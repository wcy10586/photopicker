package com.photopicker;

import java.util.List;

/**
 * Created by wuchangyou on 2016/10/20.
 */
public interface OnPhotoPickListener {
    public void onPhotoPick(boolean userCancel, List<String> list);

    public void onPhotoCapture(String path);
}
