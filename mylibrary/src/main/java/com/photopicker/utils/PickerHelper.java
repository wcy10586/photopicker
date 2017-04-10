package com.photopicker.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.photopicker.PhotoPicker;
import com.photopicker.PhotoPreview;
import com.photopicker.PickerConfig;
import com.photopicker.R;
import com.photopicker.entity.Photo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.widget.Toast.LENGTH_LONG;

/**
 * Created by wuchangyou on 2016/10/20.
 */
public class PickerHelper {

    private static PickerHelper helper;
    private List<Photo> selectedList;
    private List<Photo> currentPagePhotos;
    private List<OnSelectedPhotoCountChangeListener> listeners;
    private List<OnSelectedStateChangeListener> stateChangeListeners;

    private List<Activity> activities;
    private PickerConfig config;


    private PickerHelper() {
        activities = new ArrayList<>();
        selectedList = new ArrayList<>();
        currentPagePhotos = new ArrayList<>();
        listeners = new ArrayList<>();
        stateChangeListeners = new ArrayList<>();
    }

    public static PickerHelper init() {
        helper = new PickerHelper();
        return helper;
    }

    public static void destroy() {
//        helper.config = null;
//        helper.listeners.clear();
//        helper.selectedList.clear();
//        helper.currentPagePhotos.clear();
//        helper.stateChangeListeners.clear();
//        helper = null;
//        PhotoPreview.destroy();
//        PhotoPicker.destroy();
    }

    public static PickerHelper getHelper() {
        return helper;
    }

    public List<Photo> getSelectedList() {
        return selectedList;
    }

    public void addSelected(Photo photo) {
        if (!selectedList.contains(photo)) {
            selectedList.add(photo);
            onCountChange();
        }
    }

    private void onCountChange() {
        for (OnSelectedPhotoCountChangeListener l : listeners) {
            l.selectedCount(selectedList.size());
        }
    }

    public void removeUnselected(Photo photo) {
        selectedList.remove(photo);
        onCountChange();
    }

    public void addSelectedChangeListener(OnSelectedPhotoCountChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeSelectedChangeListener(OnSelectedPhotoCountChangeListener listener) {
        listeners.remove(listener);
    }

    public List<Photo> getCurrentPagePhotos() {
        return currentPagePhotos;
    }

    public void setCurrentPagePhotos(List<Photo> currentPagePhotos) {
        this.currentPagePhotos = currentPagePhotos;
    }

    public void addStateChangeListener(OnSelectedStateChangeListener l) {
        if (!stateChangeListeners.contains(l)) {
            stateChangeListeners.add(l);
        }
    }

    public void removeStateChangeListener(OnSelectedStateChangeListener l) {
        stateChangeListeners.remove(l);
    }

    /**
     * @param c
     * @param photo
     * @return 是否又发生状态改变
     */
    public boolean toggleSelection(Context c, Photo photo) {
        int maxCount = 0;
        if (PhotoPicker.getCurrentPhotoPicker() != null) {
            maxCount = PhotoPicker.getCurrentPhotoPicker().getMaxCount();
        } else {
            maxCount = PhotoPreview.getCurrentPhotoPreview().getMaxCount();
        }
        if (maxCount <= 0) {
            return false;
        }
        if (maxCount == 1) {
            if (selectedList.contains(photo)) {
                removeUnselected(photo);
                callStateChange(photo);
            } else {
                callStateChange(photo);
                for (int i = 0; i < selectedList.size(); i++) {
                    Photo photo1 = selectedList.get(i);
                    callStateChange(photo1);
                }
                selectedList.clear();
                addSelected(photo);
            }
            return true;
        }
        int currentCount = selectedList.size();
        boolean selected = photo.isSelected();
        int change = selected ? -1 : 1;
        if (currentCount + change > maxCount) {
            Toast.makeText(c, c.getString(R.string.__picker_over_max_count_tips, maxCount),
                    LENGTH_LONG).show();
            return false;
        }
        callStateChange(photo);
        if (selected) {
            removeUnselected(photo);
        } else {
            addSelected(photo);
        }

        return true;
    }

    private void callStateChange(Photo photo) {
        for (OnSelectedStateChangeListener l : stateChangeListeners) {
            l.onSelectedChanged(photo);
        }
    }

    public void addAll(List<Photo> list) {
        selectedList.clear();
        selectedList.addAll(list);
    }

    public void addActivity(Activity activity) {
        if (!activities.contains(activity)) {
            activities.add(activity);
        }
    }

    public PickerConfig getConfig() {
        return config;
    }

    public void setConfig(PickerConfig config) {
        this.config = config;
    }

    public boolean isSelected(Photo photo) {
        return selectedList.contains(photo);
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    public void finishPick(boolean cancel) {
        List<String> paths = new ArrayList<>();
        if (!cancel) {
            for (Photo photo : selectedList) {
                paths.add(photo.getPath());
            }
        }
        if (PhotoPicker.getCurrentPhotoPicker() != null) {
            if (PhotoPicker.getCurrentPhotoPicker().getListener() != null) {
                PhotoPicker.getCurrentPhotoPicker().getListener().onPhotoPick(cancel, paths);
            }
        } else if (PhotoPreview.getCurrentPhotoPreview() != null) {
            if (PhotoPreview.getCurrentPhotoPreview().getListener() != null) {
                PhotoPreview.getCurrentPhotoPreview().getListener().onPhotoPick(cancel, paths);
            }
        }
        finishPick();
    }

    private void finishPick() {
        for (Iterator<Activity> iterator = activities.iterator(); iterator.hasNext(); ) {
            Activity activity = iterator.next();
            activity.finish();
        }
        activities.clear();
        Log.i("ssss","==finish==  " + helper);
        destroy();
    }

    public void capturePhotoFinish(String path) {
        if (PhotoPicker.getCurrentPhotoPicker() != null) {
            if (PhotoPicker.getCurrentPhotoPicker().getListener() != null) {
                PhotoPicker.getCurrentPhotoPicker().getListener().onPhotoCapture(path);
            }
        }
        finishPick();
    }

    public interface OnSelectedPhotoCountChangeListener {
        public void selectedCount(int count);
    }

    public interface OnSelectedStateChangeListener {
        public void onSelectedChanged(Photo photo);
    }

}
