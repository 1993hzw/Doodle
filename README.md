
## [Android图片涂鸦](http://blog.csdn.net/u012964944/article/details/52661940)

### 主要功能

  * 设置画笔及形状

    画笔可以选择手绘、仿制、橡皮擦、文字，其中仿制功能跟PS中的类似，复制图片中的某处地方。形状可以选择手绘、箭头、直线、圆、矩形等。画笔的底色可以选择颜色，或者选择一张画布。

  * 撤销及清屏

    可每一步的操作都可以撤销，清屏时将清除所有的操作。

  * 放缩、移动及旋转

    在涂鸦的过程中，可以自由地通过手势缩放和移动图片，同时在多次缩放后，可快速居中图片；支持旋转图片。

  * 放大器

    为了更细微地涂鸦，涂鸦过程中可以设置出现放大器．

### 使用

```java
/**
 * 涂鸦界面，根据GraffitiView的接口，提供页面交互
 *
 */
public class GraffitiActivity extends Activity {
/**
     * 启动涂鸦界面
     *
     * @param activity
     * @param params      涂鸦参数
     * @param requestCode startActivityForResult的请求码
     */
    public static void startActivityForResult(Activity activity, GraffitiParams params, int requestCode) {
        Intent intent = new Intent(activity, GraffitiActivity.class);
        intent.putExtra(GraffitiActivity.KEY_PARAMS, params);
        activity.startActivityForResult(intent, requestCode);
    }
 }
```

```java
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
   if (requestCode == REQ_CODE_GRAFFITI) {
        if (resultCode == GraffitiActivity.RESULT_OK) {
        // 获取涂鸦后的图片
            String path = data.getStringExtra(GraffitiActivity.KEY_IMAGE_PATH);
            ...
        } else if (resultCode == GraffitiActivity.RESULT_ERROR) {
           ...
        }
    }
}
```

```java
 /**
 * 涂鸦参数
 */
public static class GraffitiParams implements Parcelable {
    /**
     * 图片路径
     */
    public String mImagePath;
    /**
     * 　保存路径，如果为null，则图片保存在根目录下/DCIM/Graffiti/
     */
    public String mSavePath;
    /**
     * 　保存路径是否为目录，如果为目录，则在该目录生成由时间戳组成的图片名称
     */
    public boolean mSavePathIsDir;
    /**
     * 　橡皮擦底图，如果为null，则底图为当前图片路径
     */
    public String mEraserPath;
    /**
     * 橡皮擦底图是否调整大小，如果为true则调整到跟当前涂鸦图片一样的大小．
     * 默认为true
     */
    public boolean mEraserImageIsResizeable = true;
    /**
     * 触摸时，图片区域外是否绘制涂鸦轨迹
     */
    public boolean mIsDrawableOutside;
    /**
     * 涂鸦时（手指按下）隐藏设置面板的延长时间(ms)，当小于等于0时则为不尝试隐藏面板（即保持面板当前状态不变）;当大于0时表示需要触摸屏幕超过一定时间后才隐藏
     * 或者手指抬起时展示面板的延长时间(ms)，或者表示需要离开屏幕超过一定时间后才展示
     * 默认为800ms
     */
    public long mChangePanelVisibilityDelay = 800; //ms
    /**
     * 设置放大镜的倍数，当小于等于0时表示不使用放大器功能
     * 放大器只有在设置面板被隐藏的时候才会出现
     * 默认为2.5倍
     */
    public float mAmplifierScale = 2.5f;
    /**
     * 初始化的画笔大小
     * 默认为30px
     */
    public float mPaintSize = 30;
}
```

### 依赖
```
dependencies {
    compile 'com.forward.androids:androids:1.1.4'
}
```

  * [Androids](https://github.com/1993hzw/Androids)
  * [ImageSelector](https://github.com/1993hzw/ImageSelector)


### 界面

 ![IMG](https://raw.githubusercontent.com/1993hzw/common/master/Graffiti/01.png)
 ![IMG](https://raw.githubusercontent.com/1993hzw/common/master/Graffiti/02.png)
 ![IMG](https://raw.githubusercontent.com/1993hzw/common/master/Graffiti/03.png)



### 相关文章

  * 功能介绍：

   [android图片涂鸦，具有设置画笔，撤销，缩放移动等功能(一)](http://blog.csdn.net/u012964944/article/details/52661940)


  * 原理介绍：

  [android图片涂鸦，具有设置画笔，撤销，缩放移动等功能(二)](http://blog.csdn.net/u012964944/article/details/52769273)

  [android图片涂鸦——旋转与文字功能的实现原理](http://blog.csdn.net/u012964944/article/details/62889219)


### 更新日志

  * 2017-03-16 v4.1(5)

  (1)加入文字功能，支持输入文字并对文字进行编辑、旋转等．


  * 2017-02-18 v4.0(4)

  (1)加入旋转图片的功能，并且支持撤销旋转前的涂鸦操作．


  * 2017-01-24 v3.0(3)

  (1)合并图片坐标系和画布坐标系，简化原理．
  (2)更新原理介绍的文章．


  * 2016-12-23 v2.0(2)

  (1)增加放大器功能．

  (2)新增接口：涂鸦时面板自动隐藏．

  (3)修复：使用长图，缩放移动时不能自由移动．
