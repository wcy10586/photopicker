# photopicker
一款图片选择器，模仿微信的ui，实现了 图片选取，图片预览 ，拍照 功能，可以自定义一些基本的配置.使用方便。

##优化功能，添加自定义相机拍照，添加了颜色自定义设置。

##使用方式简单，采用链式调用：

 PhotoPicker.init().setMaxCount(5).setShowCamera(true).setUseSystemCamera(false).startPick(MainActivity.this, new                     OnPhotoPickListener() {
                    @Override
                    public void onPhotoPick(boolean userCancel, List<String> list) {
                        if (userCancel) {
                            return;
                        }
                        lists.clear();
                        lists.addAll(list);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onPhotoCapture(String path) {
                        lists.add(path);
                        adapter.notifyDataSetChanged();
                    }
                });


setShowCamera(true)//控制在相册中显示拍照功能。
setUseSystemCamera(false)//是否使用系统相机拍照。如果位false，则使用的是自定义实现的相机拍照。

##声明activity
        <activity
            android:name="com.photopicker.PhotoPickerActivity"
            android:configChanges="keyboardHidden|screenSize|orientation"
            android:screenOrientation="portrait"></activity>
        <activity
            android:name="com.photopicker.PhotoPagerActivity"
            android:screenOrientation="portrait"></activity>
            
            
            在自己的manifest 中声明activity，并设置activity的theme，就可以保持图片选取和预览与自己的主题统一。
