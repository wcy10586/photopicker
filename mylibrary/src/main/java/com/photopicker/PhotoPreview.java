package com.photopicker;

import android.content.Context;
import android.content.Intent;


import com.photopicker.entity.Photo;
import com.photopicker.utils.PickerHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuchangyou on 2016/10/20.
 * <p/>
 */
public class PhotoPreview {
    private int currentPos;
    private List<Photo> photos;
    private boolean previewOnly;
    private int maxCount;
    private static PhotoPreview preview;
    private OnPhotoPickListener listener;
    private List<Photo> selectedList;
    private PickerConfig config;

    private PhotoPreview() {
        previewOnly = true;
        photos = new ArrayList<>();
        selectedList = new ArrayList<>();
    }

    public static PhotoPreview init() {
        preview = new PhotoPreview();
        return preview;
    }

    public static PhotoPreview getCurrentPhotoPreview() {
        return preview;
    }

    public static void destroy() {
        preview.photos.clear();
        preview.selectedList.clear();
        preview = null;
    }

    public int getCurrentPos() {
        return currentPos;
    }

    public PhotoPreview setCurrentPos(int currentPos) {
        this.currentPos = currentPos;
        return this;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public PhotoPreview setPhotoPaths(List<String> photos) {
        List<Photo> list = new ArrayList<>();
        for (int i = 0; i < photos.size(); i++) {
            Photo photo = new Photo(i, photos.get(i));
            list.add(photo);
        }
        setPhotos(list);
        return this;
    }

    public PhotoPreview setPhotos(List<Photo> photos) {
        this.photos.clear();
        this.photos.addAll(photos);

        return this;
    }

    public boolean isPreviewOnly() {
        return previewOnly;
    }

    public PhotoPreview setPreviewOnly(boolean previewOnly) {
        this.previewOnly = previewOnly;
        return this;
    }

    public int getMaxCount() {
        return Math.max(maxCount, photos.size());
    }

    public PhotoPreview setMaxCount(int maxCount) {
        this.maxCount = maxCount;
        return this;
    }

    public OnPhotoPickListener getListener() {
        return listener;
    }

    public PhotoPreview setSelectedList(List<String> paths) {
        List<Photo> list = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            Photo photo = new Photo(i, paths.get(i));
            list.add(photo);
        }
        selectedList.clear();
        selectedList.addAll(list);
        return this;
    }

    public PickerConfig getConfig() {
        return config;
    }

    public PhotoPreview setConfig(PickerConfig config) {
        this.config = config;
        return this;
    }

    public void startPreview(Context activity, OnPhotoPickListener listener) {
        PickerHelper helper = PickerHelper.getHelper();
        if (helper == null) {
            helper = PickerHelper.init();
            helper.addAll(selectedList);
            helper.setConfig(config);
        }
        this.listener = listener;
        Intent intent = new Intent(activity, PhotoPagerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

}
