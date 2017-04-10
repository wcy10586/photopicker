package com.photopicker.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.photopicker.PhotoPicker;
import com.photopicker.R;
import com.photopicker.entity.Photo;
import com.photopicker.entity.PhotoDirectory;
import com.photopicker.event.OnPhotoClickListener;
import com.photopicker.utils.MediaStoreHelper;
import com.photopicker.utils.PickerHelper;
import com.photopicker.utils.Utils;


import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class PhotoGridAdapter extends RecyclerView.Adapter<PhotoGridAdapter.PhotoViewHolder> {
    private Context context;
    private LayoutInflater inflater;
    private RequestManager glide;

    private OnPhotoClickListener onPhotoClickListener = null;
    private View.OnClickListener onCameraClickListener = null;
    private PhotoDirectory currentDirectory;
    public final static int ITEM_TYPE_CAMERA = 100;
    public final static int ITEM_TYPE_PHOTO = 101;
    private final static int COL_NUMBER_DEFAULT = 3;

    private boolean hasCamera = true;
    private boolean previewEnable = true;

    private int imageSize;
    private int columnNumber = COL_NUMBER_DEFAULT;

    protected List<PhotoDirectory> photoDirectories;

    public int currentDirectoryIndex = 0;

    public PhotoGridAdapter(Context context, RequestManager requestManager, List<PhotoDirectory> photoDirectories) {
        this.context = context;
        this.glide = requestManager;
        this.photoDirectories = photoDirectories;
        inflater = LayoutInflater.from(context);
        setColumnNumber(context, columnNumber);
    }

    public PhotoGridAdapter(Context context, RequestManager requestManager, List<PhotoDirectory> photoDirectories, int colNum) {
        this(context, requestManager, photoDirectories);
        setColumnNumber(context, colNum);
    }

    private void setColumnNumber(Context context, int columnNumber) {
        this.columnNumber = columnNumber;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        int widthPixels = metrics.widthPixels;
        imageSize = widthPixels / columnNumber;
    }

    @Override
    public int getItemViewType(int position) {
        return (showCamera() && position == 0) ? ITEM_TYPE_CAMERA : ITEM_TYPE_PHOTO;
    }


    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.__picker_item_photo, parent, false);
        PhotoViewHolder holder = new PhotoViewHolder(itemView);
        if (viewType == ITEM_TYPE_CAMERA) {
            holder.vSelected.setVisibility(View.GONE);
            holder.ivPhoto.setScaleType(ImageView.ScaleType.CENTER);

            holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onCameraClickListener != null) {
                        onCameraClickListener.onClick(view);
                    }
                }
            });
        }
        return holder;
    }


    @Override
    public void onBindViewHolder(final PhotoViewHolder holder, int position) {

        if (getItemViewType(position) == ITEM_TYPE_PHOTO) {
            holder.ivPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            List<Photo> photos = getCurrentPhotos();
            final Photo photo;

            if (showCamera()) {
                photo = photos.get(position - 1);
            } else {
                photo = photos.get(position);
            }

            Uri uri = Utils.getUri(photo.getPath());
            glide
                    .load(uri)
                    .centerCrop()
                    .dontAnimate()
                    .thumbnail(0.5f)
                    .override(imageSize, imageSize)
                    .placeholder(R.drawable.__picker_default_weixin)
                    .error(R.drawable.__picker_ic_broken_image_black_48dp)
                    .into(holder.ivPhoto);

            final boolean isChecked = photo.isSelected();

            holder.vSelected.setSelected(isChecked);
            holder.cover.setSelected(isChecked);

            holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onPhotoClickListener != null) {
                        int pos = holder.getAdapterPosition();
                        if (previewEnable) {
                            onPhotoClickListener.onClick(view, pos, showCamera());
                        } else {
                            holder.vSelected.performClick();
                        }
                    }
                }
            });
            holder.vSelected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (PickerHelper.getHelper().toggleSelection(context, photo)) {
                        notifyChange(photo);
                    }
                }
            });

        } else {
            holder.ivPhoto.setScaleType(ImageView.ScaleType.FIT_XY);
            if (PickerHelper.getHelper().getConfig() != null) {
                holder.ivPhoto.setImageResource(PickerHelper.getHelper().getConfig().getCameraRes());
            } else {
                holder.ivPhoto.setImageResource(R.drawable.__picker_alumnus_camera_selector);
            }
        }
    }

    public void notifyChange(Photo photo) {

        int pos = getCurrentPhotos().indexOf(photo);
        pos = showCamera() ? pos + 1 : pos;
        if (pos >= 0) {
            notifyItemChanged(pos);
        }
    }

    @Override
    public int getItemCount() {
        int photosCount = (photoDirectories == null || photoDirectories.isEmpty()) ? 0
                : (getCurrentPhotos() == null ? 0 : getCurrentPhotos().size());
        if (showCamera()) {
            return photosCount + 1;
        }
        return photosCount;
    }

    private List<Photo> getCurrentPhotos() {
        if (currentDirectory != null) {
            currentDirectory.setSelected(false);
        }
        currentDirectory = photoDirectories.get(currentDirectoryIndex);
        currentDirectory.setSelected(true);
        PickerHelper.getHelper().setCurrentPagePhotos(currentDirectory.getPhotos());
        return currentDirectory.getPhotos();
    }

    public void setCurrentDirectoryIndex(int currentDirectoryIndex) {
        this.currentDirectoryIndex = currentDirectoryIndex;
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhoto;
        private ImageView vSelected;
        private View cover;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            ivPhoto = (ImageView) itemView.findViewById(R.id.iv_photo);
            vSelected = (ImageView) itemView.findViewById(R.id.v_selected);
            if (PickerHelper.getHelper().getConfig() != null) {
                vSelected.setImageResource(PickerHelper.getHelper().getConfig().getImageSelectorRes());
            }
            cover = itemView.findViewById(R.id.cover);
        }
    }


    public void setOnPhotoClickListener(OnPhotoClickListener onPhotoClickListener) {
        this.onPhotoClickListener = onPhotoClickListener;
    }


    public void setOnCameraClickListener(View.OnClickListener onCameraClickListener) {
        this.onCameraClickListener = onCameraClickListener;
    }


    public void setShowCamera(boolean hasCamera) {
        this.hasCamera = hasCamera;
    }

    public void setPreviewEnable(boolean previewEnable) {
        this.previewEnable = previewEnable;
    }

    public boolean showCamera() {
        return (hasCamera && currentDirectoryIndex == MediaStoreHelper.INDEX_ALL_PHOTOS);
    }

    @Override
    public void onViewRecycled(PhotoViewHolder holder) {
        Glide.clear(holder.ivPhoto);
        super.onViewRecycled(holder);
    }

}
