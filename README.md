
## [Android图片涂鸦](http://blog.csdn.net/u012964944/article/details/52661940)

### 主要功能

  * 设置画笔及形状

    画笔可以选择手绘、仿制、橡皮擦，其中仿制功能跟PS中的类似，复制图片中的某处地方。形状可以选择手绘、箭头、直线、圆、矩形等。画笔的底色可以选择颜色，或者选择一张画布。

  * 撤销及清屏

    可每一步的操作都可以撤销，清屏时将清除所有的操作。

  * 放缩与移动

    在涂鸦的过程中，可以自由地通过手势缩放和移动图片，以便更细微地涂鸦，同时在多次缩放后，可点击“O”按钮，快速居中图片。

### 使用

```
/**
 * 涂鸦界面，根据GraffitiView的接口，提供页面交互
 *
 * （这边代码和ui比较粗糙，主要目的是告诉大家GraffitiView的接口具体能实现什么功能，实际需求中的ui和交互需另提别论）
 */
public class GraffitiActivity extends Activity {
	/**
	* 启动涂鸦界面
	* @param activity
	* @param imagePath 图片路径
	* @param requestCode startActivityForResult的请求码
	*/
	public static void startActivityForResult(Activity activity, String imagePath, int requestCode);
}
```

```
/**
     * 启动涂鸦界面
     *
     * @param activity
     * @param params      参数
     * @param requestCode startActivityForResult的请求码
     */
    public static void startActivityForResult(Activity activity, GraffitiParams params, int requestCode) {
        Intent intent = new Intent(activity, GraffitiActivity.class);
        intent.putExtra(GraffitiActivity.KEY_PARAMS, params);
        activity.startActivityForResult(intent, requestCode);
    }
```

```
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
         * {@link GraffitiView#GraffitiView(Context, Bitmap, String, boolean, GraffitiView.GraffitiListener)}
         */
        public String mEraserPath;

        /**
         * 橡皮擦底图是否调整大小，如果可以则调整到跟当前涂鸦图片一样的大小．
         * 默认为true
         */
        public boolean mEraserImageIsResizeable = true;

        /**
         * 触摸时，图片区域外是否绘制涂鸦轨迹
         */
        public boolean mIsDrawableOutside;
    }
```

### 依赖
  * [Androids](https://github.com/1993hzw/Androids)
  * [ImageSelector](https://github.com/1993hzw/ImageSelector)
###

### 界面

 ![IMG](http://s1.sinaimg.cn/orignal/003eBWOtzy757JQaYiA00&690)
 ![IMG](http://s16.sinaimg.cn/orignal/003eBWOtzy757JPIatxbf&690)
 ![IMG](http://s1.sinaimg.cn/orignal/003eBWOtzy757JQ7CG470&690)



### 相关文章

  * 功能介绍：

   [android图片涂鸦，具有设置画笔，撤销，缩放移动等功能(一)](http://blog.csdn.net/u012964944/article/details/52661940)


  *　原理介绍：

  [android图片涂鸦，具有设置画笔，撤销，缩放移动等功能(二)](http://blog.csdn.net/u012964944/article/details/52769273)