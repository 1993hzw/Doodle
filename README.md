# Doodle

[![](https://jitpack.io/v/1993hzw/Doodle.svg)](https://jitpack.io/#1993hzw/Doodle)

Image doodle for Android. You can undo, zoom, move, add text, textures, etc. Also, a powerful, customizable and extensible doodle framework & multi-function drawing board.

***Android图片涂鸦，具有撤消、缩放、移动、添加文字，贴图等功能。还是一个功能强大，可自定义和可扩展的涂鸦框架、多功能画板。***



![01.gif](https://raw.githubusercontent.com/1993hzw/common/master/Doodle/01.gif)

![01](https://raw.githubusercontent.com/1993hzw/common/master/Doodle/01.png)
![02](https://raw.githubusercontent.com/1993hzw/common/master/Doodle/02.png)
![03](https://raw.githubusercontent.com/1993hzw/common/master/Doodle/03_2.png)

# Feature 特性

  * Brush and shape ***画笔及形状***

    The brush can choose hand-painted, mosaic, imitation, eraser, text, texture, and the imitation function is similar to that in PS, copying somewhere in the picture. Shapes can be selected from hand-drawn, arrows, lines, circles, rectangles, and so on. The background color of the brush can be selected as a color, or an image.

    ***画笔可以选择手绘、马赛克、仿制、橡皮擦、文字、贴图，其中仿制功能跟PS中的类似，复制图片中的某处地方。形状可以选择手绘、箭头、直线、圆、矩形等。画笔的底色可以选择颜色，或者一张图片。***

  * Undo ***撤销***

    Each step of the doodle operation can be undone.
    
    ***每一步的涂鸦操作都可以撤销。***

  * Zoom, move, and rotate ***放缩、移动及旋转***

    In the process of doodle, you can freely zoom, move and rotate the picture with gestures. Also, you can move，rotate and scale the doodle item.
    
    ***在涂鸦的过程中，可以自由地通过手势缩放、移动、旋转图片。可对涂鸦移动、旋转、缩放等。***

  * Zoomer ***放大器***

    In order to doodle more finely, an zoomer can be set up during the doodle process.
    
    ***为了更细微地涂鸦，涂鸦过程中可以设置出现放大器。***

# Usage 用法

#### Gradle 

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
 
dependencies {
    compile 'com.github.1993hzw:Doodle:5.5'
}
```

There are two ways to use the Doodle library:

***这里有两种方式使用Doodle涂鸦库***

  * A. Launch DoodleActivity directly (the layout is like demo images above). If you need to customize more interactions, please use another method (Way B)．

  ***使用写好的涂鸦界面，直接启动.启动的页面可参看上面的演示图片。如果需要自定义更多的交互方式，则请使用另一种方式(即B方式)。***
```java
DoodleParams params = new DoodleParams(); // 涂鸦参数
params.mImagePath = imagePath; // the file path of image
DoodleActivity.startActivityForResult(MainActivity.this, params, REQ_CODE_DOODLE);
```
See [DoodleParams](https://github.com/1993hzw/Doodle/blob/master/doodle/src/main/java/cn/hzw/doodle/DoodleParams.java) for more details.

***查看[DoodleParams](https://github.com/1993hzw/Doodle/blob/master/doodle/src/main/java/cn/hzw/doodle/DoodleParams.java)获取更多涂鸦参数信息。***

  * B. Recommend, use DoodleView and customize your layout. 
    mDoodle
    ***推荐的方法：使用DoodleView，便于拓展，灵活性高，自定义自己的交互界面.***
```java
/*
Whether or not to optimize drawing, it is suggested to open, which can optimize the drawing speed and performance.
Note: When item is selected for editing after opening, it will be drawn at the top level, and not at the corresponding level until editing is completed.
是否优化绘制，建议开启，可优化绘制速度和性能.
注意：开启后item被选中编辑时时会绘制在最上面一层，直到结束编辑后才绘制在相应层级
 */
boolean optimizeDrawing = true;
DoodleView mDoodleView = new DoodleView(this, bitmap, optimizeDrawing, new IDoodleListener() {
            /*
            called when save the doodled iamge. 
            保存涂鸦图像时调用
             */
            @Override
            public void onSaved(IDoodle doodle, Bitmap bitmap, Runnable callback) {
               //do something
            }

            /*
             called when it is ready to doodle because the view has been measured. Now, you can set size, color, pen, shape, etc. 
             此时view已经测量完成，涂鸦前的准备工作已经完成，在这里可以设置大小、颜色、画笔、形状等。
             */
            @Override
            public void onReady(IDoodle doodle) {
                //do something
            }
        });

mTouchGestureListener = new DoodleOnTouchGestureListener(mDoodleView, new DoodleOnTouchGestureListener.ISelectionListener() {
    /*
     called when the item(such as text, texture) is selected/unselected.
     item（如文字，贴图）被选中或取消选中时回调
     */
    @Override
    public void onSelectedItem(IDoodle doodle, IDoodleSelectableItem selectableItem, boolean selected) {
        //do something
    }

    /*
     called when you click the view to create a item(such as text, texture).
     点击View中的某个点创建可选择的item（如文字，贴图）时回调
     */
    @Override
    public void onCreateSelectableItem(IDoodle doodle, float x, float y) {
        //do something
        /*
if (mDoodleView.getPen() == DoodlePen.TEXT) {
        IDoodleSelectableItem item = new DoodleText(mDoodleView, "hello", 20 * mDoodleView.getUnitSize(), new DoodleColor(Color.RED), x, y);
        mDoodleView.addItem(item);
} else if (mDoodleView.getPen() == DoodlePen.BITMAP) {
        IDoodleSelectableItem item = new DoodleBitmap(mDoodleView, bitmap, 80 * mDoodle.getUnitSize(), x, y);
        mDoodleView.addItem(item);
}
        */
    }
});

// create touch detector, which dectects the gesture of scoll, scale, single tap, etc.
// 创建手势识别器，识别滚动，缩放，点击等手势
IDoodleTouchDetector detector = new DoodleTouchDetector(getApplicationContext(), mTouchGestureListener);
mDoodleView.setDefaultTouchDetector(detector);

// Setting parameters.设置参数
mDoodleView.setPen(DoodlePen.TEXT);
mDoodleView.setShape(DoodleShape.HAND_WRITE);
mDoodleView.setColor(new DoodleColor(Color.RED));


```
When turning off optimized drawing, you only need to call `addItem(IDoodleItem)` when you create it. When you start optimizing drawing, the created or selected item needs to call `markItemToOptimizeDrawing(IDoodleItem)`, and you should call `notifyItemFinishedDrawing(IDoodleItem)` when you finish drawing. So this is generally used in code:

***当关闭优化绘制时,只需要在创建时调用`addItem(IDoodleItem)`;而当开启优化绘制时，创建或选中的item需要调用`markItemToOptimizeDrawing(IDoodleItem)`,结束绘制时应调用`notifyItemFinishedDrawing(IDoodleItem)`。因此在代码中一般这样使用：***
```
// begin drawing
if (mDoodle.isOptimizeDrawing()) {
   mDoodle.markItemToOptimizeDrawing(item);
} else {
   mDoodle.addItem(item);
}

...

// finish drawing
if (mDoodle.isOptimizeDrawing()) {
   mDoodle.notifyItemFinishedDrawing(item);
}
```


Then, add the DoodleView to your layout. Now you can start doodling freely.

 ***把DoodleView添加到布局中，然后开始涂鸦。***

# Demo 实例

Here are other simple examples to teach you how to use the doodle framework.

1. **[Mosaic effect](https://github.com/1993hzw/Doodle/blob/master/app/src/main/java/cn/hzw/doodledemo/MosaicDemo.java)**
 ***[马赛克效果](https://github.com/1993hzw/Doodle/blob/master/app/src/main/java/cn/hzw/doodledemo/MosaicDemo.java)***

2. **[Change text's size by scale gesture](https://github.com/1993hzw/Doodle/blob/master/app/src/main/java/cn/hzw/doodledemo/ScaleGestureItemDemo.java)**
 ***[手势缩放文本大小](https://github.com/1993hzw/Doodle/blob/master/app/src/main/java/cn/hzw/doodledemo/ScaleGestureItemDemo.java)***

More...

Now I think you should know that DoodleActivity has used DoodleView. You also can customize your layout like DoodleActivity. See [DoodleActivity](https://github.com/1993hzw/Doodle/blob/master/doodle/src/main/java/cn/hzw/doodle/DoodleActivity.java) for more details.

***现在你应该知道DoodleActivity就是使用了DoodleView实现涂鸦，你可以参照[DoodleActivity](https://github.com/1993hzw/Doodle/blob/master/doodle/src/main/java/cn/hzw/doodle/DoodleActivity.java)是怎么实现涂鸦界面的交互来实现自己的自定义页面。***

DoodleView has implemented IDoodle.

***DoodleView实现了IDoodle接口。***
```java
public interface IDoodle {
...
    public float getUnitSize();
    public void setDoodleRotation(int degree);
    public void setDoodleScale(float scale, float pivotX, float pivotY);
    public void setPen(IDoodlePen pen);
    public void setShape(IDoodleShape shape);
    public void setDoodleTranslation(float transX, float transY);
    public void setSize(float paintSize);
    public void setColor(IDoodleColor color);
    public void addItem(IDoodleItem doodleItem);
    public void removeItem(IDoodleItem doodleItem);
    public void save();
    public void topItem(IDoodleItem item);
    public void bottomItem(IDoodleItem item);
    public boolean undo(int step);
...
}
```
# Framework diagram 框架图

![structure](https://raw.githubusercontent.com/1993hzw/common/master/Doodle/structure.png)

### Doodle Coordinate 涂鸦坐标
![coordinate](https://raw.githubusercontent.com/1993hzw/common/master/Doodle/doodle_coordinate.png)

# Extend 拓展

You can create a customized item like [DoodlePath, DoodleText, DoodleBitmap](https://github.com/1993hzw/Doodle/tree/master/doodle/src/main/java/cn/hzw/doodle) which extend [DoodleItemBase](https://github.com/1993hzw/Doodle/blob/master/doodle/src/main/java/cn/hzw/doodle/DoodleItemBase.java) or implement [IDoodleItem](https://github.com/1993hzw/Doodle/blob/master/doodle/src/main/java/cn/hzw/doodle/core/IDoodleItem.java). 

***实现[IDoodleItem](https://github.com/1993hzw/Doodle/blob/master/doodle/src/main/java/cn/hzw/doodle/core/IDoodleItem.java)接口或基础[DoodleItemBase](https://github.com/1993hzw/Doodle/blob/master/doodle/src/main/java/cn/hzw/doodle/DoodleItemBase.java)，用于创建自定义涂鸦条目item，比如[DoodlePath, DoodleText, DoodleBitmap](https://github.com/1993hzw/Doodle/tree/master/doodle/src/main/java/cn/hzw/doodle)***

You can create a customized pen like DoodlePen which implements [IDoodlePen](https://github.com/1993hzw/Doodle/blob/master/doodle/src/main/java/cn/hzw/doodle/core/IDoodlePen.java).

***实现[IDoodlePen](https://github.com/1993hzw/Doodle/blob/master/doodle/src/main/java/cn/hzw/doodle/core/IDoodlePen.java)接口用于创建自定义画笔pen，比如DoodlePen***

You can create a customized shape like DoodleShape which implements [IDoodleShape](https://github.com/1993hzw/Doodle/blob/master/doodle/src/main/java/cn/hzw/doodle/core/IDoodleShape.java). 

***实现[IDoodleShape](https://github.com/1993hzw/Doodle/blob/master/doodle/src/main/java/cn/hzw/doodle/core/IDoodleShape.java)接口用于创建自定义形状shape，比如DoodleShape***

You can create a customized color like DoodleColor which implements [IDoodleColor](https://github.com/1993hzw/Doodle/blob/master/doodle/src/main/java/cn/hzw/doodle/core/IDoodleColor.java). 

***实现[IDoodleColor](https://github.com/1993hzw/Doodle/blob/master/doodle/src/main/java/cn/hzw/doodle/core/IDoodleColor.java)接口用于创建自定义颜色color，比如DoodleColor***

You can create a customized touch gesture detector like [DoodleTouchDetector](https://github.com/1993hzw/Doodle/blob/master/doodle/src/main/java/cn/hzw/doodle/DoodleTouchDetector.java)([GestureListener](https://github.com/1993hzw/Doodle/blob/master/doodle/src/main/java/cn/hzw/doodle/DoodleOnTouchGestureListener.java)) which implements [IDoodleTouchDetector](https://github.com/1993hzw/Doodle/blob/master/doodle/src/main/java/cn/hzw/doodle/core/IDoodleTouchDetector.java). 

***实现[IDoodleTouchDetector](https://github.com/1993hzw/Doodle/blob/master/doodle/src/main/java/cn/hzw/doodle/core/IDoodleTouchDetector.java)接口用于创建自定义手势识别器，比如DoodleTouchDetector***

***[Others](https://github.com/1993hzw/Doodle/blob/master/extend.md)***

# The developer 开发者

hzw19933@gmail.com

154330138@qq.com

Q&A <a target="_blank" href="//shang.qq.com/wpa/qunwpa?idkey=d225a990b29a135d4a601be7a198f04572f1dbd96ccd5be944ff2ef87dda5c11"><img border="0" src="//pub.idqqimg.com/wpa/images/group.png" alt="Doodle-涂鸦交流群" title="Doodle-涂鸦交流群"></a>  6762102

***欢迎大家访问[我的博客](https://blog.csdn.net/u012964944)，以便获取更多关于Doodle的文章哦.***

[《Android涂鸦画板原理详解——从初级到高级（一）》](https://blog.csdn.net/u012964944/article/details/82703684)

[《Android涂鸦画板原理详解——从初级到高级（二）》](https://blog.csdn.net/u012964944/article/details/83420038)

# License

  ```
  MIT License
  
  Copyright (c) 2018 huangziwei
  
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:
  
  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.
  
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  SOFTWARE.
  ```

# Donations 打赏

If this project helps you a lot and you want to support the project's development and maintenance of this project, feel free to scan the following QR code for donation. Your donation is highly appreciated. Thank you!

***如果这个项目对您有很大帮助，并且您想支持该项目的项目开发和维护，请扫描以下二维码进行捐赠。非常感谢您的支持！***

![donate](https://raw.githubusercontent.com/1993hzw/common/master/payment.png)
