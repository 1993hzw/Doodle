
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
 * 图片选择页面，扫描系统数据库中记录的图片，分批次加载
 */
public class ImageSelectorActivity extends Activity implements View.OnClickListener {
    /**
     * 启动图片选择页面
     * @param requestCode startActivityForResult的请求码
     * @param activity
     * @param pathList 已选择的图片,可传空
     * @param multipleChoice 是否多选
     * @param maxCount 多选时，可选的最大数量
     */
    public static void startActivityForResult(int requestCode, Activity activity, ArrayList<String> pathList, boolean multipleChoice, int maxCount);
}
```
### 界面

 ![IMG](http://s1.sinaimg.cn/orignal/003eBWOtzy757JQaYiA00&690)
 ![IMG](http://s16.sinaimg.cn/orignal/003eBWOtzy757JPIatxbf&690)
 ![IMG](http://s1.sinaimg.cn/orignal/003eBWOtzy757JQ7CG470&690)

### 相关文章

  * 功能介绍：

   [android图片涂鸦，具有设置画笔，撤销，缩放移动等功能(一)](http://blog.csdn.net/u012964944/article/details/52661940)


  *　原理介绍：

  [android图片涂鸦，具有设置画笔，撤销，缩放移动等功能(二)](http://blog.csdn.net/u012964944/article/details/52769273)