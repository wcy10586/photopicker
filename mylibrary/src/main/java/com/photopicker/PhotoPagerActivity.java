package com.photopicker;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.nineoldandroids.animation.ObjectAnimator;
import com.photopicker.entity.Photo;
import com.photopicker.fragment.ImagePagerFragment;
import com.photopicker.utils.PickerHelper;
import com.photopicker.utils.Utils;
import com.photopicker.widget.Titlebar;

import java.util.List;

public class PhotoPagerActivity extends AppCompatActivity implements PickerHelper.OnSelectedPhotoCountChangeListener {

    private ImagePagerFragment pagerFragment;
    private Titlebar titlebar;

    private PhotoPreview photoPreview;
    private PickerHelper helper;
    private View statusBgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.__picker_activity_photo_pager);
        initFragment();
        photoPreview = PhotoPreview.getCurrentPhotoPreview();
        helper = PickerHelper.getHelper();
        helper.addSelectedChangeListener(this);
        titlebar = (Titlebar) findViewById(R.id.titlebar);
        titlebar.init(this);
        statusBgView = findViewById(R.id.status_bg_view);
        setAlpha(1f, 0.8f);
        setTitlebar();
        setStatusBgView();
        helper.addActivity(this);
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

    private void setAlpha(float from, float to) {
        ObjectAnimator animator = new ObjectAnimator();
        animator.setTarget(titlebar);
        animator.setDuration(200);
        animator.setPropertyName("alpha");
        animator.setFloatValues(from, to);
        animator.start();
    }

    private void initFragment() {
        if (pagerFragment == null) {
            pagerFragment =
                    (ImagePagerFragment) getSupportFragmentManager().findFragmentById(R.id.photoPagerFragment);
        }

    }

    private void setTitlebar() {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) titlebar.getLayoutParams();
        if (params == null) {
            params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.getActionBarHeight(this));
        }

        params.topMargin = Utils.getStateBarHeight(this);
        titlebar.setLayoutParams(params);
        titlebar.setTitle("");
        if (photoPreview.isPreviewOnly()) {
            titlebar.getTvRight().setVisibility(View.GONE);
        } else {
            if (photoPreview.isShowDelete()) {
                titlebar.getTvLeft().setVisibility(View.GONE);
                titlebar.getTvRight().setVisibility(View.GONE);
                titlebar.getIvRight().setVisibility(View.VISIBLE);
                titlebar.getIvRight().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int current = pagerFragment.getmViewPager().getCurrentItem();
                        Photo photo = photoPreview.getPhotos().get(current);
                        photoPreview.getPhotos().remove(photo);
                        pagerFragment.getmPagerAdapter().notifyDataSetChanged();
                        if (current > 0) {
                            pagerFragment.getmViewPager().setCurrentItem(current - 1);
                        }
                        if (current > 0) {
                            setTvLeft(current);
                        }
                        if (photoPreview.getOnPhotoDeleteListener() != null) {
                            photoPreview.getOnPhotoDeleteListener().onPhotoDelete(photo.getPath());
                        }
                        if (photoPreview.getPhotos().isEmpty()) {
                            helper.finishPick(false);
                        }

                    }
                });
            } else {
                titlebar.getTvRight().setVisibility(View.VISIBLE);
                titlebar.getIvRight().setVisibility(View.GONE);
                setTvRightText();
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
            }
        }
        titlebar.getIvLeft().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PickerHelper.getHelper().finishPick(true);
            }
        });

        titlebar.getTvLeft().setVisibility(View.VISIBLE);
    }

    public void setTvLeft(int current) {
        titlebar.getTvLeft().setText(current + " / " + photoPreview.getPhotos().size());
    }

    @Override
    public void onBackPressed() {
        if (PickerHelper.getHelper() == null) {
            super.onBackPressed();
            return;
        }
        if (PickerHelper.getHelper().getActivities().size() > 1) {
            super.onBackPressed();
            return;
        }
        PickerHelper.getHelper().finishPick(true);
    }

    private void setTvRightText() {
        if (photoPreview.getMaxCount() <= 1) {
            titlebar.getTvRight().setText(R.string.__picker_done);
        } else {
            titlebar.getTvRight().setText(getString(R.string.__picker_done_with_count, helper.getSelectedList().size(), photoPreview.getMaxCount()));
        }
    }

    @Override
    public void selectedCount(int count) {
        if (photoPreview.getMaxCount() > 1) {
            titlebar.getTvRight().setText(getString(R.string.__picker_done_with_count, count, photoPreview.getMaxCount()));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (PickerHelper.getHelper() != null) {
            helper.removeSelectedChangeListener(this);
            helper.removeActivity(this);
        }
        PhotoPreview.setOpening(false);
    }

}
