package com.photopicker.utils.camera;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by liepin on 2017/4/5.
 */

public class ImageUtils {
    //保存图片文件
    public static String saveToFile(Context context, Bitmap croppedImage) {
        ContentResolver resolver = context.getContentResolver();
        String url = MediaStore.Images.Media.insertImage(resolver, croppedImage, null, null);
        if (!TextUtils.isEmpty(url)) {
            Uri uri = Uri.parse(url);
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = null;
            try {
                cursor = resolver.query(uri, proj, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            } catch (Exception e) {

            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

        }
        return "";
    }
}
