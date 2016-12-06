package com.photopicker.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.photopicker.utils.PickerHelper;

public class Photo implements Parcelable {

    private int id;
    private String path;

    public Photo(int id, String path) {
        this.id = id;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isSelected() {
        return  PickerHelper.getHelper().isSelected(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.path);
    }

    protected Photo(Parcel in) {
        this.id = in.readInt();
        this.path = in.readString();
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel source) {
            return new Photo(source);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Photo photo = (Photo) o;

        return path != null ? path.equals(photo.path) : photo.path == null;

    }

    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : 0;
    }
}
