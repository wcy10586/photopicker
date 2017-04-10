package com.photopicker.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.photopicker.PhotoPreview;
import com.photopicker.PickerConfig;
import com.photopicker.R;
import com.photopicker.utils.PickerHelper;


public class Titlebar extends FrameLayout {
    private RelativeLayout rootView;
    private TextView tvLeft;
    private ImageView ivLeft;
    private TextView tvTitle;
    private TextView tvRight;
    private ImageView ivRight;

    private OnClickListener leftOnclickListener;
    private OnClickListener rightOnclickListener;

    private Activity mActivity;


    public TextView getTvTitle() {
        return tvTitle;
    }

    public ImageView getIvLeft() {
        return ivLeft;
    }

    public TextView getTvLeft() {
        return tvLeft;
    }

    public TextView getTvRight() {
        return tvRight;
    }

    public ImageView getIvRight() {
        return ivRight;
    }

    @Override
    public RelativeLayout getRootView() {
        return rootView;
    }


    public Titlebar(Context context) {
        this(context, null);
    }

    public Titlebar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Titlebar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        initData(context, attrs, defStyleAttr);
        initEvent(context, attrs, defStyleAttr);
        setConfig();
    }

    public void init(Activity activity) {
        mActivity = activity;
        leftOnclickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.finish();
            }
        };
        ivLeft.setOnClickListener(leftOnclickListener);

    }

    private void initEvent(Context context, AttributeSet attrs, int defStyleAttr) {

        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            leftOnclickListener = new OnClickListener() {
                @Override
                public void onClick(View view) {
                    activity.finish();
                }
            };
        }


    }

    public void setLeftOnclickListener(OnClickListener listener) {
        if (listener != null) {
            leftOnclickListener = listener;
            ivLeft.setOnClickListener(leftOnclickListener);
            tvLeft.setOnClickListener(leftOnclickListener);
        }

    }

    public void setRightOnclickListener(OnClickListener listener) {
        if (listener != null) {
            rightOnclickListener = listener;
            ivRight.setOnClickListener(rightOnclickListener);
            tvRight.setOnClickListener(rightOnclickListener);
        }
    }

    public void setTitle(String title) {
        tvTitle.setText(title);
        if (!TextUtils.isEmpty(title)) {
            tvTitle.setVisibility(VISIBLE);
        } else {
            tvTitle.setVisibility(GONE);
        }
    }

    public void setLeft(Drawable leftDrawable, String leftTxt, OnClickListener listener) {
        if (leftDrawable != null) {
            ivLeft.setVisibility(VISIBLE);
            ivLeft.setImageDrawable(leftDrawable);
            tvLeft.setVisibility(GONE);
        } else if (!TextUtils.isEmpty(leftTxt)) {
            tvLeft.setVisibility(VISIBLE);
            tvLeft.setText(leftTxt);
            ivLeft.setVisibility(GONE);
        } else {//all not set,default

        }

        if (listener != null) {
            leftOnclickListener = listener;
        }


    }

    public void setRight(Drawable rightDrawable, String rightTxt, OnClickListener listener) {
        if (!TextUtils.isEmpty(rightTxt)) {
            tvRight.setVisibility(VISIBLE);
            tvRight.setText(rightTxt);
            ivRight.setVisibility(GONE);
            if (listener != null) {
                rightOnclickListener = listener;
                tvRight.setOnClickListener(rightOnclickListener);
            }
        } else if (rightDrawable != null) {
            ivRight.setVisibility(VISIBLE);
            tvRight.setVisibility(GONE);
            ivRight.setImageDrawable(rightDrawable);
            if (listener != null) {
                rightOnclickListener = listener;
                ivRight.setOnClickListener(rightOnclickListener);
            }
        }

        if (listener != null) {
            rightOnclickListener = listener;
            ivRight.setOnClickListener(rightOnclickListener);
        }
    }

    private void initData(Context context, AttributeSet attrs, int defStyleAttr) {

        TypedArray typedArray = null;
        try {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.PickerTitleBar);
            String leftTxt = typedArray.getString(R.styleable.PickerTitleBar_mtb_leftTxt);
            String title = typedArray.getString(R.styleable.PickerTitleBar_mtb_title);
            String rightTxt = typedArray.getString(R.styleable.PickerTitleBar_mtb_rightTxt);

            Drawable leftDrawable = typedArray.getDrawable(R.styleable.PickerTitleBar_mtb_left_icon);
            Drawable rightDrawable = typedArray.getDrawable(R.styleable.PickerTitleBar_mtb_right_icon);

            //left:drawable first
            setLeft(leftDrawable, leftTxt, null);

            //center
            setTitle(title);


            //right: text first
            setRight(rightDrawable, rightTxt, null);


        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }

    }

    private void initView(Context context) {
        rootView = (RelativeLayout) View.inflate(context, R.layout.__picker_view_titlebar, null);
        ivLeft = (ImageView) rootView.findViewById(R.id.iv_left);
        tvLeft = (TextView) rootView.findViewById(R.id.tv_left);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_title);

        ivRight = (ImageView) rootView.findViewById(R.id.iv_right);
        tvRight = (TextView) rootView.findViewById(R.id.tv_right);
        this.addView(rootView);

    }

    private void setConfig() {
        PickerConfig config = PickerHelper.getHelper().getConfig();
        if (config != null) {
            ivLeft.setImageResource(config.getBackImgRes());
            tvLeft.setTextColor(config.getFinishTextColor());
            tvLeft.setTextSize(TypedValue.COMPLEX_UNIT_DIP,config.getFinishTextSize());
            tvTitle.setTextColor(config.getTitleColor());
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP,config.getTitleSize());
            tvRight.setTextColor(config.getFinishTextColor());
            tvRight.setTextSize(TypedValue.COMPLEX_UNIT_DIP,config.getFinishTextSize());
            if (config.getTitleBarColor()!= Integer.MAX_VALUE){
                setBackgroundColor(config.getTitleBarColor());
            }
            ivRight.setImageResource(config.getDeleteImgRes());
        }


    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Titlebar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr);
    }
}
