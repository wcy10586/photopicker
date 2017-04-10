package com.photopicker;

import android.graphics.Color;

/**
 * Created by wuchangyou on 2016/11/30.
 * <p/>
 * 顶部titleBar的颜色默认与主题颜色保持一致，使用主题的 colorPrimary
 * 状态栏颜色默认与主题保持一致，使用主题的  colorPrimaryDark
 */
public class PickerConfig {

    private int cameraRes;
    private int imageSelectorRes;
    private int titleBarColor;
    private int statusColor;
    private int backImgRes;
    private int titleSize;
    private int titleColor;
    private int finishTextSize;
    private int finishTextColor;
    private int allPictureTextSize;
    private int allPictureTextColor;
    private int allPictureIcon;
    private int previewTextSize;
    private int previewTextColor;
    private int bottomBarColor;
    private int chooseTextSize;
    private int chooseTextColor;
    private int deleteImgRes;

    public PickerConfig() {
        init();
    }

    private void init() {
        cameraRes = R.drawable.__picker_alumnus_camera_selector;
        imageSelectorRes = R.drawable.__picker_checkbox_bg;
        titleBarColor = Integer.MAX_VALUE;
        statusColor = Integer.MAX_VALUE;
        backImgRes = R.drawable.__picker_btn_menu_back;
        titleSize = 18;
        titleColor = Color.WHITE;
        finishTextSize = 15;
        finishTextColor = Color.WHITE;
        allPictureTextSize = 14;
        allPictureTextColor = Color.WHITE;
        allPictureIcon = R.drawable.__picker_icon_list_start;
        previewTextSize = 14;
        previewTextColor = Color.WHITE;
        bottomBarColor = Color.parseColor("#e5292929");
        chooseTextSize = 15;
        chooseTextColor = Color.WHITE;
        deleteImgRes = R.drawable.__preview_delete;
    }

    public int getCameraRes() {
        return cameraRes;
    }

    /**
     * 相机图片
     *
     * @param cameraRes
     */
    public PickerConfig setCameraRes(int cameraRes) {
        this.cameraRes = cameraRes;
        return this;
    }

    public int getImageSelectorRes() {
        return imageSelectorRes;
    }

    /**
     * 设置选择框
     *
     * @param imageSelectorRes
     */
    public PickerConfig setImageSelectorRes(int imageSelectorRes) {
        this.imageSelectorRes = imageSelectorRes;
        return this;
    }

    public int getTitleBarColor() {
        return titleBarColor;
    }

    /**
     * titleBar的颜色
     *
     * @param titleBarColor
     */
    public PickerConfig setTitleBarColor(int titleBarColor) {
        this.titleBarColor = titleBarColor;
        return this;
    }

    public int getStatusColor() {
        return statusColor;
    }

    public PickerConfig setStatusColor(int statusColor) {
        this.statusColor = statusColor;
        return this;
    }

    public int getBackImgRes() {
        return backImgRes;
    }

    /**
     * 设置返回按钮
     *
     * @param bacckImgRes
     */
    public PickerConfig setBackImgRes(int bacckImgRes) {
        this.backImgRes = bacckImgRes;
        return this;
    }

    public int getTitleSize() {
        return titleSize;
    }

    /**
     * \设置标题文字打大小，单位 dp
     *
     * @param titleSize
     */
    public PickerConfig setTitleSize(int titleSize) {
        this.titleSize = titleSize;
        return this;
    }

    public int getTitleColor() {
        return titleColor;
    }

    /**
     * 设置标题文字的颜色
     *
     * @param titleColor
     */
    public PickerConfig setTitleColor(int titleColor) {
        this.titleColor = titleColor;
        return this;
    }

    public int getFinishTextSize() {
        return finishTextSize;
    }

    /**
     * 设置完成按钮的文字大小
     *
     * @param finishTextSize
     */
    public PickerConfig setFinishTextSize(int finishTextSize) {
        this.finishTextSize = finishTextSize;
        return this;
    }

    public int getFinishTextColor() {
        return finishTextColor;
    }


    /**
     * 设置完成按钮的文字颜色
     *
     * @param finishTextColor
     */
    public PickerConfig setFinishTextColor(int finishTextColor) {
        this.finishTextColor = finishTextColor;
        return this;
    }

    public int getAllPictureTextSize() {
        return allPictureTextSize;
    }

    /**
     * 所有图片文字的大小 单位dp
     *
     * @param allPictureTextSize
     */
    public PickerConfig setAllPictureTextSize(int allPictureTextSize) {
        this.allPictureTextSize = allPictureTextSize;
        return this;
    }

    public int getAllPictureTextColor() {
        return allPictureTextColor;
    }

    /**
     * 所有图片文字的颜色
     *
     * @param allPictureTextColor
     */
    public PickerConfig setAllPictureTextColor(int allPictureTextColor) {
        this.allPictureTextColor = allPictureTextColor;
        return this;
    }

    public int getPreviewTextSize() {
        return previewTextSize;
    }

    public int getAllPictureIcon() {
        return allPictureIcon;
    }

    /**
     * 所有图片的icon
     *
     * @param allPictureIcon
     */
    public PickerConfig setAllPictureIcon(int allPictureIcon) {
        this.allPictureIcon = allPictureIcon;
        return this;
    }

    /**
     * 设置  预览 的字号  单位dp
     *
     * @param previewTextSize
     */
    public PickerConfig setPreviewTextSize(int previewTextSize) {
        this.previewTextSize = previewTextSize;
        return this;
    }

    public int getPreviewTextColor() {
        return previewTextColor;
    }

    /**
     * 设置预览的字号
     *
     * @param previewTextColor
     */
    public PickerConfig setPreviewTextColor(int previewTextColor) {
        this.previewTextColor = previewTextColor;
        return this;
    }

    public int getBottomBarColor() {
        return bottomBarColor;
    }

    /**
     * 底部条的颜色
     *
     * @param bottomBarColor
     */
    public PickerConfig setBottomBarColor(int bottomBarColor) {
        this.bottomBarColor = bottomBarColor;
        return this;
    }

    public int getChooseTextSize() {
        return chooseTextSize;
    }

    /**
     * 设置选择钮的文字大小 单位dp
     *
     * @param chooseTextSize
     */
    public PickerConfig setChooseTextSize(int chooseTextSize) {
        this.chooseTextSize = chooseTextSize;
        return this;
    }

    public int getChooseTextColor() {
        return chooseTextColor;
    }

    /**
     * 设置选择文字颜色
     *
     * @param chooseTextColor
     */
    public PickerConfig setChooseTextColor(int chooseTextColor) {
        this.chooseTextColor = chooseTextColor;
        return this;
    }

    public int getDeleteImgRes() {
        return deleteImgRes;
    }

    /**
     * 设置删除按钮的图片
     *
     * @param deleteImgRes
     */
    public PickerConfig setDeleteImgRes(int deleteImgRes) {
        this.deleteImgRes = deleteImgRes;
        return this;
    }
}
