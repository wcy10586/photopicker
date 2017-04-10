package com.photopicker.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.photopicker.PhotoPicker;
import com.photopicker.PhotoPickerActivity;
import com.photopicker.PhotoPreview;
import com.photopicker.PickerConfig;
import com.photopicker.R;
import com.photopicker.adapter.PhotoGridAdapter;
import com.photopicker.adapter.PopupDirectoryListAdapter;
import com.photopicker.entity.Photo;
import com.photopicker.entity.PhotoDirectory;
import com.photopicker.event.OnPhotoClickListener;
import com.photopicker.utils.ImageCaptureManager;
import com.photopicker.utils.MediaStoreHelper;
import com.photopicker.utils.PickerHelper;
import com.photopicker.utils.Utils;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PhotoPickerFragment extends Fragment implements PickerHelper.OnSelectedPhotoCountChangeListener, PickerHelper.OnSelectedStateChangeListener {

    private ImageCaptureManager captureManager;
    private PhotoGridAdapter photoGridAdapter;

    private PopupDirectoryListAdapter listAdapter;
    //所有photos的路径
    private List<PhotoDirectory> directories;
    private int SCROLL_THRESHOLD = 30;
    //目录弹出框的一次最多显示的目录数目
    public static int COUNT_MAX = 5;
    private ListPopupWindow listPopupWindow;
    private RequestManager mGlideRequestManager;
    private Context mContext;

    private Button btnPreview;
    private Button btSwitchDirectory;
    private PhotoPicker photoPicker;
    private View borromBar;
    private PickerHelper helper;

    private PhotoPreview preview;

    private int navigationBarHeight;
    private Animation enterAnim;
    private Animation exitAnim;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getApplicationContext();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        photoPicker = PhotoPicker.getCurrentPhotoPicker();
        helper = PickerHelper.getHelper();
        helper.addSelectedChangeListener(this);
        helper.addStateChangeListener(this);

        preview = PhotoPreview.getCurrentPhotoPreview();
        if (preview == null) {
            preview = PhotoPreview.init();
        }
        preview.setPreviewOnly(false);

        mGlideRequestManager = Glide.with(this);
        directories = new ArrayList<>();

        photoGridAdapter = new PhotoGridAdapter(mContext, mGlideRequestManager, directories, photoPicker.getColumn());
        photoGridAdapter.setPreviewEnable(photoPicker.isPreviewEnable());

        photoGridAdapter.setShowCamera(photoPicker.isShowCamera());
        Bundle mediaStoreArgs = new Bundle();
        MediaStoreHelper.getPhotoDirs(getActivity(), mediaStoreArgs,
                new MediaStoreHelper.PhotosResultCallback() {
                    @Override
                    public void onResultCallback(List<PhotoDirectory> dirs) {
                        directories.clear();
                        directories.addAll(dirs);
                        photoGridAdapter.notifyDataSetChanged();
                        listAdapter.notifyDataSetChanged();
                        adjustHeight();

                    }
                });

        captureManager = new ImageCaptureManager(getActivity());

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.__picker_fragment_photo_picker, container, false);
        View bottomNav = rootView.findViewById(R.id.bottom_nav);
        navigationBarHeight = Utils.getNavigationBarHeight(getActivity());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bottomNav.getLayoutParams();
            if (params == null) {
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, navigationBarHeight);
            }
            params.height = navigationBarHeight;
            bottomNav.setLayoutParams(params);
        }

        btnPreview = (Button) rootView.findViewById(R.id.btn_preview);
        btSwitchDirectory = (Button) rootView.findViewById(R.id.button);
        borromBar = rootView.findViewById(R.id.picker_bottom_bar);
        setRecyclerView(rootView);
        initPopupWindow(rootView);
        setListener();
        setSelectedCount(PickerHelper.getHelper().getSelectedList().size());
        setConfig();
        return rootView;
    }

    private void setConfig() {
        PickerConfig config = PickerHelper.getHelper().getConfig();
        if (config != null) {
            btnPreview.setTextColor(config.getAllPictureTextColor());
            btnPreview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, config.getAllPictureTextSize());

            btSwitchDirectory.setTextColor(config.getAllPictureTextColor());
            btSwitchDirectory.setTextSize(TypedValue.COMPLEX_UNIT_DIP, config.getAllPictureTextSize());
            Drawable drawable = getResources().getDrawable(config.getAllPictureIcon());
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            btSwitchDirectory.setCompoundDrawables(null, null, drawable, null);

            borromBar.setBackgroundColor(config.getBottomBarColor());
        }
    }

    private void setRecyclerView(View rootView) {
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_photos);
        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) recyclerView.getLayoutParams();
        if (params2 == null) {
            params2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        params2.topMargin = Utils.getStateBarHeight(getActivity());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            params2.bottomMargin = navigationBarHeight;
        }
        recyclerView.setLayoutParams(params2);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(photoPicker.getColumn(), OrientationHelper.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(photoGridAdapter);

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (Math.abs(dy) > SCROLL_THRESHOLD) {
                    mGlideRequestManager.pauseRequests();
                } else {
                    mGlideRequestManager.resumeRequests();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mGlideRequestManager.resumeRequests();
                }
            }
        });
    }


    private void initPopupWindow(View rootView) {
        btSwitchDirectory.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (listPopupWindow.isShowing()) {
                    dismissPopupWindow();
                } else if (!getActivity().isFinishing()) {
                    adjustHeight();
                    showPopupWindow();
                    listPopupWindow.getListView().setVerticalScrollBarEnabled(false);
                }
            }
        });
        listAdapter = new PopupDirectoryListAdapter(mGlideRequestManager, directories);

        listPopupWindow = new ListPopupWindow(getActivity());

        listPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//替换背景
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        int widths = wm.getDefaultDisplay().getWidth();
        listPopupWindow.setWidth(widths);//ListPopupWindow.MATCH_PARENT还是会有边距，直接拿到屏幕宽度来设置也不行，因为默认的background有左右padding值。
        listPopupWindow.setAnchorView(rootView.findViewById(R.id.bottom_bar));
        listPopupWindow.setAdapter(listAdapter);

        listPopupWindow.setDropDownGravity(Gravity.BOTTOM);
        listPopupWindow.setAnimationStyle(0);
        listPopupWindow.setModal(true);
        listPopupWindow.setForceIgnoreOutsideTouch(true);

        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listPopupWindow.dismiss();

                PhotoDirectory directory = directories.get(position);

                btSwitchDirectory.setText(directory.getName().toLowerCase());//默认会大写，这里要改成小写

                photoGridAdapter.setCurrentDirectoryIndex(position);
                photoGridAdapter.notifyDataSetChanged();
            }
        });

    }

    private void showPopupWindow() {
        listPopupWindow.show();
        if (enterAnim == null) {
            enterAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.__picker_dialog_enter);
        }
        listPopupWindow.getListView().startAnimation(enterAnim);
    }

    private void dismissPopupWindow() {
        if (exitAnim == null) {
            exitAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.__picker_dialog_exit);
            exitAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    listPopupWindow.dismiss();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
        listPopupWindow.getListView().startAnimation(exitAnim);
    }

    private void setListener() {
        photoGridAdapter.setOnPhotoClickListener(new OnPhotoClickListener() {
            @Override
            public void onClick(View v, int position, boolean showCamera) {
                final int index = showCamera ? position - 1 : position;

                preview.setCurrentPos(index);
                preview.setPhotos(helper.getCurrentPagePhotos());

                ((PhotoPickerActivity) getActivity()).addImagePagerFragment();
            }
        });

        photoGridAdapter.setOnCameraClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (PhotoPicker.getCurrentPhotoPicker().isUseSystemCamera()) {
                        Intent intent = captureManager.dispatchTakePictureIntent();
                        startActivityForResult(intent, ImageCaptureManager.REQUEST_TAKE_PHOTO);
                    } else {
                        Intent intent = captureManager.dispatchTakePictureIntent(getActivity());
                        startActivityForResult(intent, ImageCaptureManager.REQUEST_TAKE_PHOTO);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        //预览按钮
        btnPreview.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PickerHelper.getHelper().getSelectedList().size() > 0) {
                    preview.setMaxCount(photoPicker.getMaxCount())
                            .setPhotos(helper.getSelectedList())
                            .setCurrentPos(0).startPreview(getActivity(), null);

                } else {
                    Toast.makeText(getActivity(), "还没有选择图片", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ImageCaptureManager.REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            captureManager.galleryAddPic();
            if (PhotoPicker.getCurrentPhotoPicker().isUseSystemCamera()) {
                String path = captureManager.getCurrentPhotoPath();
                PickerHelper.getHelper().capturePhotoFinish(path);
            } else {
                if (data != null) {
                    String path = data.getStringExtra(Utils.EXTRA_IMAGE);
                    PickerHelper.getHelper().capturePhotoFinish(path);
                } else {
                    PickerHelper.getHelper().capturePhotoFinish("");
                }
            }
        }
    }

    @Override
    public void selectedCount(int count) {
        setSelectedCount(count);
    }

    private void setSelectedCount(int count) {
        if (count > 0) {
            btnPreview.setText(getString(R.string.__picker_preview) + "(" + count + ")");
        } else {
            btnPreview.setText(R.string.__picker_preview);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        captureManager.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        captureManager.onRestoreInstanceState(savedInstanceState);
        super.onViewStateRestored(savedInstanceState);
    }


    public void adjustHeight() {
        if (listAdapter == null) return;
        int count = listAdapter.getCount();
        count = count < COUNT_MAX ? count : COUNT_MAX;
        if (listPopupWindow != null) {
            int height = count * getResources().getDimensionPixelSize(R.dimen.__picker_item_directory_height);
            int centerHeight = getResources().getDisplayMetrics().heightPixels - getResources().getDimensionPixelSize(R.dimen.__bottom_navi_height) * 2;
            height = Math.min(height, (int) (centerHeight * 0.8f));
            listPopupWindow.setHeight(height);
            int verticalOffset = height + navigationBarHeight + getResources().getDimensionPixelSize(R.dimen.__bottom_navi_height);
            listPopupWindow.setVerticalOffset(-verticalOffset);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        helper.removeSelectedChangeListener(this);
        helper.removeStateChangeListener(this);
    }

    public void clearDirectories() {
        if (directories != null) {
            for (PhotoDirectory directory : directories) {
                directory.getPhotoPaths().clear();
                directory.getPhotos().clear();
                directory.setPhotos(null);
            }
            directories.clear();
            directories = null;
        }
    }

    @Override
    public void onSelectedChanged(Photo photo) {
        photoGridAdapter.notifyChange(photo);
    }
}
