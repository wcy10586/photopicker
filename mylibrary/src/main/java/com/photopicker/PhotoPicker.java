package com.photopicker;

import android.content.Context;
import android.content.Intent;


import com.photopicker.entity.Photo;
import com.photopicker.utils.PickerHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuchangyou on 2016/10/20.
 */
public class PhotoPicker {

    private static PhotoPicker photoPicker;

    private final int DEFAULT_MAX_COUNT = 9;
    private final int DEFAULT_COLUMN_NUMBER = 3;

    private int maxCount = DEFAULT_MAX_COUNT;
    private int column = DEFAULT_COLUMN_NUMBER;
    private boolean showCamera;
    private boolean showGif;
    private boolean previewEnable;
    private OnPhotoPickListener listener;
    private boolean isOnlyPreview;
    private List<Photo> selectedList;
    private PickerConfig config;

    private PhotoPicker() {
        showCamera = true;
        showGif = false;
        previewEnable = true;
        selectedList = new ArrayList<>();
    }

    /**
     * 实例化一个新的photoPicker
     */
    public static PhotoPicker init() {
        photoPicker = new PhotoPicker();
        return photoPicker;
    }

    public static void destroy() {
        photoPicker = null;
    }

    /**
     * 获取当前已有的photoPicker
     *
     * @return
     */
    public static PhotoPicker getCurrentPhotoPicker() {
        return photoPicker;
    }


    public PhotoPicker setSelectedList(List<String> paths) {
        this.selectedList.clear();
        for (int i = 0; i < paths.size(); i++) {
            Photo photo = new Photo(i, paths.get(i));
            selectedList.add(photo);
        }
        return this;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public PhotoPicker setMaxCount(int maxCount) {
        this.maxCount = maxCount;
        return this;
    }

    public int getColumn() {
        return column;
    }

    public PhotoPicker setColumn(int column) {
        this.column = column;
        return this;
    }

    public boolean isShowCamera() {
        return showCamera;
    }

    public PhotoPicker setShowCamera(boolean showCamera) {
        this.showCamera = showCamera;
        return this;
    }

    public boolean isShowGif() {
        return showGif;
    }

    public PhotoPicker setShowGif(boolean showGif) {
        this.showGif = showGif;
        return this;
    }


    public boolean isPreviewEnable() {
        return previewEnable;
    }

    public PhotoPicker setPreviewEnable(boolean previewEnable) {
        this.previewEnable = previewEnable;
        return this;
    }

    public boolean isOnlyPreview() {
        return isOnlyPreview;
    }

    public PhotoPicker setOnlyPreview(boolean onlyPreview) {
        isOnlyPreview = onlyPreview;
        return this;
    }

    public OnPhotoPickListener getListener() {
        return listener;
    }

    public PickerConfig getConfig() {
        return config;
    }

    public PhotoPicker setConfig(PickerConfig config) {
        this.config = config;
        return this;
    }

    public void startPick(Context activity, OnPhotoPickListener listener) {
        this.listener = listener;
        PickerHelper.init();
        PickerHelper.getHelper().setConfig(config);
        PickerHelper.getHelper().addAll(selectedList);
        Intent intent = new Intent(activity, PhotoPickerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }


}
