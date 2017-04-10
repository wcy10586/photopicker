package com.photopicker;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.animation.AnimatorCompatHelper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;


import com.nineoldandroids.animation.ObjectAnimator;
import com.photopicker.entity.Photo;
import com.photopicker.fragment.ImagePagerFragment;
import com.photopicker.fragment.PhotoPickerFragment;
import com.photopicker.utils.PickerHelper;
import com.photopicker.utils.Utils;
import com.photopicker.widget.Titlebar;

import java.util.List;


public class PhotoPickerActivity extends AppCompatActivity implements PickerHelper.OnSelectedPhotoCountChangeListener {

    private PhotoPickerFragment pickerFragment;
    private ImagePagerFragment imagePagerFragment;


    private Titlebar titlebar;
    private View statusBgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.__picker_activity_photo_picker);
        PickerHelper.getHelper().addSelectedChangeListener(this);
        titlebar = (Titlebar) findViewById(R.id.titlebar);
        titlebar.init(this);
        statusBgView = findViewById(R.id.status_bg_view);
        setTitlebar();
        setStatusBgView();
        addFragment();
        PickerHelper.getHelper().addActivity(this);
    }

    private void setTitlebar() {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) titlebar.getLayoutParams();
        if (params == null) {
            params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.getActionBarHeight(this));
        }
        params.topMargin = Utils.getStateBarHeight(this);
        titlebar.setLayoutParams(params);
        //右边的点击事件
        titlebar.getTvRight().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Photo> photos = PickerHelper.getHelper().getSelectedList();
                if (photos != null && photos.size() > 0) {
                    PickerHelper.getHelper().finishPick(false);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.__picker_no_photo), Toast.LENGTH_SHORT).show();
                }
            }
        });

        titlebar.getIvLeft().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setRightText(PickerHelper.getHelper().getSelectedList().size());
    }

    private void setStatusBgView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) statusBgView.getLayoutParams();
            if (params == null) {
                params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.getStateBarHeight(this));
            }
            params.height = Utils.getStateBarHeight(this);
            statusBgView.setLayoutParams(params);
        }

        PickerConfig config = PickerHelper.getHelper().getConfig();
        if (config != null && config.getStatusColor() != Integer.MAX_VALUE) {
            statusBgView.setBackgroundColor(config.getStatusColor());
        }
    }

    public void setTvLeft(int current) {
        titlebar.getTvLeft().setText(current + " / " + PickerHelper.getHelper().getCurrentPagePhotos().size());
    }

    private void addFragment() {
        if (pickerFragment == null) {
            pickerFragment = new PhotoPickerFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_grid, pickerFragment, "tag")
                    .commit();
            getSupportFragmentManager().executePendingTransactions();
        }
    }

    private void setRightText(int currentCount) {
        int maxCount = PhotoPicker.getCurrentPhotoPicker().getMaxCount();
        if (maxCount <= 1) {
            titlebar.getTvRight().setText(R.string.__picker_done);
        } else {
            titlebar.getTvRight().setText(getString(R.string.__picker_done_with_count, currentCount, maxCount));
        }
    }

    /**
     * Overriding this method allows us to run our exit animation first, then exiting
     * the activity when it complete.
     */
    @Override
    public void onBackPressed() {
        if (imagePagerFragment != null && imagePagerFragment.isVisible()) {
            imagePagerFragment.runExitAnimation(new Runnable() {
                public void run() {
                    if (imagePagerFragment != null && imagePagerFragment.isAdded()) {
                        titlebar.getTvLeft().setVisibility(View.GONE);
                        getSupportFragmentManager().beginTransaction().remove(imagePagerFragment).commit();
                    }
                    imagePagerFragment = null;
//                    titlebar.setAlpha(1f);
                    setAlpha(0.8f, 1f);

                }
            });
        } else {
            if (PickerHelper.getHelper() != null) {
                PickerHelper.getHelper().finishPick(true);
            } else {
                super.onBackPressed();
            }
        }
    }


    public void addImagePagerFragment() {
        if (imagePagerFragment == null) {
            imagePagerFragment = new ImagePagerFragment();
        } else if (imagePagerFragment.isAdded()) {
            return;
        }
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container_page, this.imagePagerFragment)
                .addToBackStack(null)
                .commit();
//        titlebar.setAlpha(0.8f);
        setAlpha(1f, 0.8f);
        titlebar.bringToFront();
        titlebar.getTvLeft().setVisibility(View.VISIBLE);
    }

    private void setAlpha(float from, float to) {
        ObjectAnimator animator = new ObjectAnimator();
        animator.setTarget(titlebar);
        animator.setDuration(200);
        animator.setPropertyName("alpha");
        animator.setFloatValues(from, to);
        animator.start();
    }

    @Override
    public void selectedCount(int count) {
        setRightText(count);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (PickerHelper.getHelper() != null) {
            PickerHelper.getHelper().removeSelectedChangeListener(this);
            PickerHelper.getHelper().removeActivity(this);
        }
        if (pickerFragment != null) {
            pickerFragment.clearDirectories();
        }
        PhotoPicker.setIsOpening(false);
    }
}


