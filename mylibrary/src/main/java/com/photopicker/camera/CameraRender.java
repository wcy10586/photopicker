package com.photopicker.camera;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.photopicker.utils.camera.CameraHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by liepin on 2017/4/6.
 */

public class CameraRender {
    private Activity context;
    private ViewGroup surfaceContainer;

    private SurfaceView surfaceView;
    private TextureView textureView;

    private CameraHelper mCameraHelper;
    private Camera.Parameters parameters = null;
    private Camera cameraInst = null;
    private int screenWidth;
    private int screenHeight;
    static final int FOCUS = 1;            // 聚焦
    static final int ZOOM = 2;            // 缩放
    private int mode;                      //0是聚焦 1是放大
    private float dist;
    private int mCurrentCameraId = 0;  //1是前置 0是后置

    private Camera.Size adapterSize = null;
    private Camera.Size previewSize = null;
    private String currentFlashMode;
    private boolean hasZoom;
    private boolean canSwitch;

    private CameraListener cameraListener;
    private TakePictureCallback pictureCallback;

    public CameraRender(Activity context, ViewGroup surfaceContainer) {
        this.context = context;
        this.surfaceContainer = surfaceContainer;
    }

    public void setCameraListener(CameraListener listener) {
        cameraListener = listener;
    }

    public void setTakePictureCallback(TakePictureCallback pictureCallback) {
        this.pictureCallback = pictureCallback;
    }

    public void create() {
        mCameraHelper = new CameraHelper(context);
        try {
            canSwitch = mCameraHelper.hasFrontCamera() && mCameraHelper.hasBackCamera();
        } catch (Exception e) {
            //获取相机信息失败
        }
        if (cameraListener != null) {
            cameraListener.onCameraChange(canSwitch, mCurrentCameraId);
        }
        init();
    }

    @SuppressWarnings("NewApi")
    private void init() {
        surfaceContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                surfaceContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                screenWidth = surfaceContainer.getWidth();
                screenHeight = surfaceContainer.getHeight();
                return false;
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            textureView = new TextureView(context);
            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    if (null == cameraInst) {
                        try {
                            cameraInst = getCameraInstance(mCurrentCameraId);
                            cameraInst.setPreviewTexture(surface);
                            initCamera();
                            cameraInst.startPreview();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                    autoFocus();
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    try {
                        if (cameraInst != null) {
                            cameraInst.stopPreview();
                            cameraInst.release();
                            cameraInst = null;
                        }
                    } catch (Exception e) {
                        //相机已经关了
                    }
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                    autoFocus();
                }
            });
            surfaceContainer.addView(textureView);
        } else {
            surfaceView = new SurfaceView(context);
            final SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            surfaceHolder.setKeepScreenOn(true);
            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    if (null == cameraInst) {
                        try {
                            cameraInst = getCameraInstance(mCurrentCameraId);
                            cameraInst.setPreviewDisplay(holder);
                            initCamera();
                            cameraInst.startPreview();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    autoFocus();
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    try {
                        if (cameraInst != null) {
                            cameraInst.stopPreview();
                            cameraInst.release();
                            cameraInst = null;
                        }
                    } catch (Exception e) {
                        //相机已经关了
                    }
                }
            });
            surfaceContainer.addView(surfaceView);
        }

        surfaceContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    // 主点按下
                    case MotionEvent.ACTION_DOWN:
                        mode = FOCUS;
                        hasZoom = false;
                        break;
                    // 副点按下
                    case MotionEvent.ACTION_POINTER_DOWN:
                        dist = spacing(event);
                        // 如果连续两点距离大于10，则判定为多点模式
                        if (spacing(event) > 10f) {
                            mode = ZOOM;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        doFocus(event.getX(), event.getY());
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = FOCUS;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mode == ZOOM) {
                            hasZoom = true;
                            doZoom(event);
                        }
                        break;
                }
                return true;
            }
        });

    }

    //切换前后置摄像头
    public void switchCamera() {
        mCurrentCameraId = (mCurrentCameraId + 1) % mCameraHelper.getNumberOfCameras();
        releaseCamera();
        setUpCamera(mCurrentCameraId);
        setCurrentFlashMode(cameraInst);
        setCurrentFlashMode(cameraInst);
        if (cameraListener != null) {
            cameraListener.onCameraChange(canSwitch, mCurrentCameraId);
        }
    }

    private void releaseCamera() {
        if (cameraInst != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                try {
                    cameraInst.setPreviewTexture(null);
                } catch (Exception e) {

                }
            }
            cameraInst.setPreviewCallback(null);
            cameraInst.release();
            cameraInst = null;
        }
        adapterSize = null;
        previewSize = null;
    }

    /**
     * @param mCurrentCameraId2
     */
    private void setUpCamera(int mCurrentCameraId2) {
        cameraInst = getCameraInstance(mCurrentCameraId2);
        if (cameraInst != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    cameraInst.setPreviewTexture(textureView.getSurfaceTexture());
                } else if (textureView != null) {
                    cameraInst.setPreviewDisplay(surfaceView.getHolder());
                }
                initCamera();
                cameraInst.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
//            ToastUtils.showDefault(CameraActivity.this, "切换失败，请重试！", Toast.LENGTH_LONG);

        }
    }

    private Camera getCameraInstance(final int id) {
        Camera c = null;
        try {
            c = mCameraHelper.openCamera(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }


    private void initCamera() {
        parameters = cameraInst.getParameters();
        if (TextUtils.isEmpty(currentFlashMode) && parameters.getSupportedFlashModes() != null) {
            currentFlashMode = Camera.Parameters.FLASH_MODE_AUTO;
            setCurrentFlashMode(cameraInst);
        }
        parameters.setPictureFormat(PixelFormat.JPEG);
        setUpPicSize();
        setUpPreviewSize();
        if (adapterSize != null) {
            parameters.setPictureSize(adapterSize.width, adapterSize.height);
        }
        if (previewSize != null) {
            parameters.setPreviewSize(previewSize.width, previewSize.height);
        }
        parameters.setJpegQuality(50);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1连续对焦
        } else {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        cameraInst.setDisplayOrientation(90);
        try {
            cameraInst.setParameters(parameters);
        } catch (Exception e) {
            Log.i("camera", "set params ! ", e);
        }
        cameraInst.startPreview();
        cameraInst.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上
    }

    private void setUpPicSize() {

        if (adapterSize != null) {
            return;
        } else {
            adapterSize = findBestPictureResolution();
            return;
        }
    }

    private void setUpPreviewSize() {

        if (previewSize != null) {
            return;
        } else {
            previewSize = findBestPreviewResolution();
        }
    }

    /**
     * 最小预览界面的分辨率
     */
    private static final int MIN_PREVIEW_PIXELS = 480 * 320;
    /**
     * 最大宽高比差
     */
    private static final double MAX_ASPECT_DISTORTION = 0.15;
    private static final String TAG = "Camera";

    /**
     * 找出最适合的预览界面分辨率
     *
     * @return
     */
    private Camera.Size findBestPreviewResolution() {
        Camera.Parameters cameraParameters = cameraInst.getParameters();
        Camera.Size defaultPreviewResolution = cameraParameters.getPreviewSize();

        List<Camera.Size> rawSupportedSizes = cameraParameters.getSupportedPreviewSizes();
        if (rawSupportedSizes == null) {
            return defaultPreviewResolution;
        }

        // 按照分辨率从大到小排序
        List<Camera.Size> supportedPreviewResolutions = new ArrayList<Camera.Size>(rawSupportedSizes);
        Collections.sort(supportedPreviewResolutions, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });

        StringBuilder previewResolutionSb = new StringBuilder();
        for (Camera.Size supportedPreviewResolution : supportedPreviewResolutions) {
            previewResolutionSb.append(supportedPreviewResolution.width).append('x').append(supportedPreviewResolution.height)
                    .append(' ');
        }
        Log.v(TAG, "Supported preview resolutions: " + previewResolutionSb);


        // 移除不符合条件的分辨率
        double screenAspectRatio = (double) screenWidth
                / (double) screenHeight;
        Iterator<Camera.Size> it = supportedPreviewResolutions.iterator();
        while (it.hasNext()) {
            Camera.Size supportedPreviewResolution = it.next();
            int width = supportedPreviewResolution.width;
            int height = supportedPreviewResolution.height;
            // 移除低于下限的分辨率，尽可能取高分辨率
            if (width * height < MIN_PREVIEW_PIXELS) {
                it.remove();
                continue;
            }

            // 在camera分辨率与屏幕分辨率宽高比不相等的情况下，找出差距最小的一组分辨率
            // 由于camera的分辨率是width>height，我们设置的portrait模式中，width<height
            // 因此这里要先交换然preview宽高比后在比较
            boolean isCandidatePortrait = width > height;
            int maybeFlippedWidth = isCandidatePortrait ? height : width;
            int maybeFlippedHeight = isCandidatePortrait ? width : height;
            double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
            double distortion = Math.abs(aspectRatio - screenAspectRatio);
            if (distortion > MAX_ASPECT_DISTORTION) {
                it.remove();
                continue;
            }

            // 找到与屏幕分辨率完全匹配的预览界面分辨率直接返回
            if (maybeFlippedWidth == screenWidth
                    && maybeFlippedHeight == screenHeight) {
                return supportedPreviewResolution;
            }
        }

        // 如果没有找到合适的，并且还有候选的像素，则设置其中最大比例的，对于配置比较低的机器不太合适
        if (!supportedPreviewResolutions.isEmpty()) {
            Camera.Size largestPreview = supportedPreviewResolutions.get(0);
            return largestPreview;
        }

        // 没有找到合适的，就返回默认的

        return defaultPreviewResolution;
    }

    private Camera.Size findBestPictureResolution() {
        Camera.Parameters cameraParameters = cameraInst.getParameters();
        List<Camera.Size> supportedPicResolutions = cameraParameters.getSupportedPictureSizes(); // 至少会返回一个值

        StringBuilder picResolutionSb = new StringBuilder();
        for (Camera.Size supportedPicResolution : supportedPicResolutions) {
            picResolutionSb.append(supportedPicResolution.width).append('x')
                    .append(supportedPicResolution.height).append(" ");
        }
        Log.d(TAG, "Supported picture resolutions: " + picResolutionSb);

        Camera.Size defaultPictureResolution = cameraParameters.getPictureSize();
        Log.d(TAG, "default picture resolution " + defaultPictureResolution.width + "x"
                + defaultPictureResolution.height);

        // 排序
        List<Camera.Size> sortedSupportedPicResolutions = new ArrayList<Camera.Size>(
                supportedPicResolutions);
        Collections.sort(sortedSupportedPicResolutions, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });

        // 移除不符合条件的分辨率
        double screenAspectRatio = (double) screenWidth
                / (double) screenHeight;
        Iterator<Camera.Size> it = sortedSupportedPicResolutions.iterator();
        while (it.hasNext()) {
            Camera.Size supportedPreviewResolution = it.next();
            int width = supportedPreviewResolution.width;
            int height = supportedPreviewResolution.height;
            // 在camera分辨率与屏幕分辨率宽高比不相等的情况下，找出差距最小的一组分辨率
            // 由于camera的分辨率是width>height，我们设置的portrait模式中，width<height
            // 因此这里要先交换然后在比较宽高比
            boolean isCandidatePortrait = width > height;
            int maybeFlippedWidth = isCandidatePortrait ? height : width;
            int maybeFlippedHeight = isCandidatePortrait ? width : height;
            double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
            double distortion = Math.abs(aspectRatio - screenAspectRatio);
            if (distortion > MAX_ASPECT_DISTORTION) {
                it.remove();
                continue;
            }
        }

        // 如果没有找到合适的，并且还有候选的像素，对于照片，则取其中最大比例的，而不是选择与屏幕分辨率相同的
        if (!sortedSupportedPicResolutions.isEmpty()) {
            return sortedSupportedPicResolutions.get(0);
        }

        // 没有找到合适的，就返回默认的
        return defaultPictureResolution;
    }

    public void destory() {
        releaseCamera();
    }


    public void turnLight() {
        turnLight(cameraInst);
    }

    /**
     * 闪光灯开关   开->关->自动
     *
     * @param mCamera
     */
    private void turnLight(Camera mCamera) {
        if (mCamera == null || mCamera.getParameters() == null
                || mCamera.getParameters().getSupportedFlashModes() == null) {
            return;
        }
        String flashMode = mCamera.getParameters().getFlashMode();
        List<String> supportedModes = mCamera.getParameters().getSupportedFlashModes();
        if (Camera.Parameters.FLASH_MODE_OFF.equals(flashMode)
                && supportedModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {//关闭状态
            currentFlashMode = Camera.Parameters.FLASH_MODE_TORCH;
        } else if (Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode)) {//开启状态
            if (supportedModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                currentFlashMode = Camera.Parameters.FLASH_MODE_AUTO;
            } else if (supportedModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                currentFlashMode = Camera.Parameters.FLASH_MODE_OFF;
            }
        } else if (Camera.Parameters.FLASH_MODE_AUTO.equals(flashMode)
                && supportedModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
            currentFlashMode = Camera.Parameters.FLASH_MODE_OFF;
        }
        setCurrentFlashMode(mCamera);
    }

    private void setCurrentFlashMode(Camera mCamera) {
        if (mCamera == null || mCamera.getParameters() == null
                || mCamera.getParameters().getSupportedFlashModes() == null) {
            if (cameraListener != null) {
                cameraListener.onFlashLigChange(false, currentFlashMode);
            }
            return;
        }
        if (cameraListener != null) {
            cameraListener.onFlashLigChange(true, currentFlashMode);
        }
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFlashMode(currentFlashMode);
        mCamera.setParameters(parameters);
    }

    //实现自动对焦
    private void autoFocus() {
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (cameraInst == null) {
                    return;
                }
                cameraInst.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {

                    }
                });
            }
        };
    }


    private void doZoom(MotionEvent event) {
        float newDist = spacing(event);
        if (newDist > 10f) {
            float tScale = (newDist - dist) / dist;
            if (tScale < 0) {
                tScale = tScale * 10;
            }
            addZoomIn((int) tScale);
        }
    }

    /**
     * 两点的距离
     */
    private float spacing(MotionEvent event) {
        if (event == null && event.getPointerCount() > 1) {
            return 0;
        }
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    //放大缩小
    int curZoomValue = 0;

    private void addZoomIn(int delta) {

        try {
            Camera.Parameters params = cameraInst.getParameters();
            if (!params.isZoomSupported()) {
                return;
            }
            curZoomValue += delta;
            if (curZoomValue < 0) {
                curZoomValue = 0;
            } else if (curZoomValue > params.getMaxZoom()) {
                curZoomValue = params.getMaxZoom();
            }

            if (!params.isSmoothZoomSupported()) {
                params.setZoom(curZoomValue);
                cameraInst.setParameters(params);
                return;
            } else {
                cameraInst.startSmoothZoom(curZoomValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doFocus(float x, float y) {
        if (mode == ZOOM || hasZoom) {
            return;
        }
        try {
            pointFocus((int) x, (int) y);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cameraListener != null) {
            cameraListener.onFocusIndex(x, y);
        }
    }


    //定点对焦的代码
    private void pointFocus(int x, int y) {
        cameraInst.cancelAutoFocus();
        parameters = cameraInst.getParameters();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            showPoint(x, y);
        }
        cameraInst.setParameters(parameters);
        autoFocus();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void showPoint(int x, int y) {
        if (parameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> areas = new ArrayList<Camera.Area>();
            //xy变换了
            int rectY = -x * 2000 / screenWidth + 1000;
            int rectX = y * 2000 / screenHeight - 1000;

            int left = rectX < -900 ? -1000 : rectX - 100;
            int top = rectY < -900 ? -1000 : rectY - 100;
            int right = rectX > 900 ? 1000 : rectX + 100;
            int bottom = rectY > 900 ? 1000 : rectY + 100;
            Rect area1 = new Rect(left, top, right, bottom);
            areas.add(new Camera.Area(area1, 800));
            parameters.setMeteringAreas(areas);
        }

        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
    }

    public void takePicture() {
        if (pictureCallback != null) {
            pictureCallback.prepareTake();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            Bitmap bitmap = textureView.getBitmap();
            if (pictureCallback != null) {
                pictureCallback.onTake(bitmap);
                cameraInst.stopPreview();
            }
        } else {
            try {
                cameraInst.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        if (pictureCallback != null) {
                            pictureCallback.onTake(data);
                            camera.stopPreview();
                        }
                    }
                });
            } catch (Throwable t) {
                t.printStackTrace();
                Toast.makeText(context, "拍照失败，请重试！", Toast.LENGTH_SHORT).show();
                try {
                    cameraInst.startPreview();
                } catch (Throwable e) {

                }
            }
        }
    }

    public void reset() {
        try {
            cameraInst.startPreview();
        } catch (Exception e) {

        }

    }

    public Camera.Size getAdapterSize() {
        return adapterSize;
    }

    public Camera.Size getPreviewSize() {
        return previewSize;
    }

    public int getisplayOrientation() {
        return mCameraHelper.getCameraDisplayOrientation(context, mCurrentCameraId) % 360;
    }

}
