package com.photopicker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.text.TextUtils;
import android.util.TypedValue;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by wuchangyou on 2016/11/11.
 */
public class Utils {
    public static final String ANDROID_RESOURCE = "android.resource://";

    public static final String EXTRA_IMAGE = "image_path";

    public static int getNavigationBarHeight(Context context) {
        int navigationbarHeight = 0;
        boolean hasNavigationBar = hasNavigationBar(context);
        Resources resources = context.getResources();
        if (hasNavigationBar) {
            int resIdNavigationBar = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resIdNavigationBar > 0) {
                navigationbarHeight = resources.getDimensionPixelSize(resIdNavigationBar);//navigationBar高度
            }
        }

        return navigationbarHeight;
    }

    public static boolean hasNavigationBar(Context context) {
        Resources resources = context.getResources();
        int resIdShow = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        boolean hasNavigationBar = false;
        if (resIdShow > 0) {
            hasNavigationBar = resources.getBoolean(resIdShow);//是否显示底部navigationBar
        }

        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {

        }
        return hasNavigationBar;
    }

    public static int getStateBarHeight(Context context) {
        int statusBarHeight = 0;
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
        }
        return statusBarHeight;
    }

    public static int getActionBarHeight(Context context) {
        int actionBarHeigh = 0;
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeigh = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }
        return actionBarHeigh;
    }

    public static void putBooleanSp(Context context, String name, boolean value) {
        SharedPreferences sp = getSp(context);
        sp.edit().putBoolean(name, value).commit();
    }

    public static boolean getBooleanSp(Context context, String name) {
        SharedPreferences sp = getSp(context);
        return sp.getBoolean(name, true);
    }

    private static SharedPreferences getSp(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences("sp_photopicker", Context.MODE_PRIVATE);
    }

    public static Uri getUri(String path) {
        if (TextUtils.isEmpty(path)) {
            return Uri.EMPTY;
        }
        if (path.startsWith("http") || path.startsWith("https")) {
            return Uri.parse(path);
        } else if (path.startsWith(ANDROID_RESOURCE)) {
            return Uri.parse(path);
        } else {
            return Uri.fromFile(new File(path));
        }
    }

}
