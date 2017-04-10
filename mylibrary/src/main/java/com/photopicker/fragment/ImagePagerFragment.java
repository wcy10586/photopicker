package com.photopicker.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.photopicker.PhotoPagerActivity;
import com.photopicker.PhotoPickerActivity;
import com.photopicker.PhotoPreview;
import com.photopicker.PickerConfig;
import com.photopicker.R;
import com.photopicker.adapter.PhotoPagerAdapter;
import com.photopicker.entity.Photo;
import com.photopicker.utils.PickerHelper;
import com.photopicker.utils.Utils;



public class ImagePagerFragment extends Fragment implements PickerHelper.OnSelectedStateChangeListener {


    private ViewPager mViewPager;
    private PhotoPagerAdapter mPagerAdapter;

    public final static long ANIM_DURATION = 200L;

    private int currentItem = 0;

    private View checkView;
    private ImageView selectedView;
    private TextView checkTv;

    private PickerHelper helper;
    private PhotoPreview preview;


    private float scaleX;
    private float scaleY;
    private int translateX;
    private int translateY;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        preview = PhotoPreview.getCurrentPhotoPreview();
        View rootView = inflater.inflate(R.layout.__picker_picker_fragment_image_pager, container, false);
        preview = PhotoPreview.getCurrentPhotoPreview();
        helper = PickerHelper.getHelper();
        helper.addStateChangeListener(this);
        currentItem = preview.getCurrentPos();
        mPagerAdapter = new PhotoPagerAdapter(Glide.with(this), preview.getPhotos());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View bottomNav = rootView.findViewById(R.id.bottom_nav);
            int height = Utils.getNavigationBarHeight(getActivity());
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bottomNav.getLayoutParams();
            if (params == null) {
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
            }
            params.height = height;
            bottomNav.setLayoutParams(params);

        }

        mViewPager = (ViewPager) rootView.findViewById(R.id.vp_photos);
        ViewTreeObserver observer = mViewPager.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mViewPager.getViewTreeObserver().removeOnPreDrawListener(this);
                initArgs();
                runEnterAnimation();
                return true;
            }
        });


        setmViewPager();
        checkView = rootView.findViewById(R.id.check_view);
        selectedView = (ImageView) rootView.findViewById(R.id.v_selected);
        checkTv = (TextView) rootView.findViewById(R.id.pager_check_text);
        setSelectedView();
        setConfig();
        return rootView;
    }


    private void setConfig() {
        PickerConfig config = PickerHelper.getHelper().getConfig();
        if (config != null) {
            selectedView.setImageResource(config.getImageSelectorRes());
            checkTv.setTextColor(config.getChooseTextColor());
            checkTv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, config.getChooseTextSize());
            if (checkView != null){
                checkView.setBackgroundColor(config.getBottomBarColor());
            }
        }
    }

    private void setmViewPager() {

        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(currentItem);
        mViewPager.setOffscreenPageLimit(5);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int currentPos = mViewPager.getCurrentItem();
                Photo photo = preview.getPhotos().get(currentPos);
                selectedView.setSelected(photo.isSelected());
                setPagerIndicator(position);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setPagerIndicator(mViewPager.getCurrentItem());
    }

    private void setPagerIndicator(int current) {
        Activity activity = getActivity();
        if (activity != null) {
            if (activity instanceof PhotoPickerActivity) {
                ((PhotoPickerActivity) activity).setTvLeft(current + 1);
            } else if (activity instanceof PhotoPagerActivity) {
                ((PhotoPagerActivity) activity).setTvLeft(current + 1);
            }
        }
    }

    private void setSelectedView() {
         if (preview.isShowDelete()){
             checkView.setVisibility(View.GONE );
             return;
         }
        if (checkView != null) {
            checkView.setVisibility(preview.isPreviewOnly() ? View.GONE : View.VISIBLE);
            checkView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
        Photo photo = preview.getPhotos().get(currentItem);
        selectedView.setSelected(photo.isSelected());
        selectedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Photo photo = preview.getPhotos().get(mViewPager.getCurrentItem());
                helper.toggleSelection(getActivity(), photo);
                selectedView.setSelected(photo.isSelected());
            }
        });
    }

    private void initArgs() {
        scaleX = mViewPager.getWidth() / 3f / mViewPager.getWidth() * 2f;
        scaleY = mViewPager.getHeight() / 3f / mViewPager.getHeight() * 2f;
        translateX = mViewPager.getWidth() / 3 / 2;
        translateY = mViewPager.getHeight() / 3 / 2;
    }

    /**
     * The enter animation scales the picture in from its previous thumbnail
     * size/location, colorizing it in parallel. In parallel, the background of the
     * activity is fading in. When the pictue is in place, the text description
     * drops down.
     */
    private void runEnterAnimation() {
        final long duration = ANIM_DURATION;

        ViewHelper.setPivotX(mViewPager, 0);
        ViewHelper.setPivotY(mViewPager, 0);
        ViewHelper.setScaleX(mViewPager, scaleX);
        ViewHelper.setScaleY(mViewPager, scaleY);
        ViewHelper.setTranslationX(mViewPager, translateX);
        ViewHelper.setTranslationY(mViewPager, translateY);
        ViewHelper.setAlpha(mViewPager, 0);

        // Animate scale and translation to go from thumbnail to full size
        ViewPropertyAnimator.animate(mViewPager)
                .setDuration(duration)
                .scaleX(1)
                .scaleY(1)
                .translationX(0)
                .translationY(0)
                .alpha(255)
                .setInterpolator(new DecelerateInterpolator());

        // Fade in the black background
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mViewPager.getBackground(), "alpha", 0, 255);
        bgAnim.setDuration(duration);
        bgAnim.start();


    }


    /**
     * The exit animation is basically a reverse of the enter animation, except that if
     * the orientation has changed we simply scale the picture back into the center of
     * the screen.
     *
     * @param endAction This action gets run after the animation completes (this is
     *                  when we actually switch activities)
     */
    public void runExitAnimation(final Runnable endAction) {
        final long duration = ANIM_DURATION;

        ViewPropertyAnimator.animate(mViewPager)
                .setDuration(duration)
                .setInterpolator(new AccelerateInterpolator())
                .scaleX(scaleX)
                .scaleY(scaleY)
                .translationX(translateX)
                .translationY(translateY)
                .alpha(0)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        endAction.run();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });

        // Fade out background
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mViewPager.getBackground(), "alpha", 255, 0);
        bgAnim.setDuration(duration);
        bgAnim.start();

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        helper.removeStateChangeListener(this);
        if (mViewPager != null) {
            mViewPager.setAdapter(null);
        }
    }

    @Override
    public void onSelectedChanged(Photo photo) {
        if (photo.equals(preview.getPhotos().get(mViewPager.getCurrentItem()))) {
            selectedView.setSelected(photo.isSelected());
        }
    }

    public PhotoPagerAdapter getmPagerAdapter(){
        return mPagerAdapter;
    }

    public ViewPager getmViewPager(){
        return mViewPager;
    }
}
