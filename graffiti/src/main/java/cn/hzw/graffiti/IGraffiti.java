package cn.hzw.graffiti;

import android.graphics.Bitmap;

import java.util.List;

/**
 * Created on 27/06/2018.
 */

public interface IGraffiti {

    /**
     * 画笔
     */
    public enum Pen {
        HAND, // 手绘
        COPY, // 仿制
        ERASER, // 橡皮擦
        TEXT(true), // 文本
        BITMAP(true); // 贴图

        private boolean mIsSelectable = false; // 画笔绘制的item是否可选

        Pen() {
            this(false);
        }

        Pen(boolean isSelectable) {
            mIsSelectable = isSelectable;
        }

        /**
         * 画笔制作的item是否可选，用于旋转、移动等特定操作
         * @return
         */
        public boolean isSelectable() {
            return mIsSelectable;
        }
    }

    /**
     * 图形
     */
    public enum Shape {
        HAND_WRITE, //
        ARROW, // 箭头
        LINE, // 直线
        FILL_CIRCLE, // 实心圆
        HOLLOW_CIRCLE, // 空心圆
        FILL_RECT, // 实心矩形
        HOLLOW_RECT, // 空心矩形
    }

    /**
     * 获取当前涂鸦坐标系中的单位大小，该单位参考dp，独立于图片
     *
     * @return
     */
    public float getSizeUnit();

    /**
     * 设置图片旋转值
     *
     * @param degree
     */
    public void setRotate(int degree);

    /**
     * 获取图片旋转值
     *
     * @return
     */
    public int getRotate();

    /**
     * 设置图片缩放倍数
     *
     * @param scale
     * @param pivotX
     * @param pivotY
     */
    public void setScale(float scale, float pivotX, float pivotY);

    /**
     * 获取图片缩放倍数
     */
    public float getScale();

    /**
     * 设置画笔
     *
     * @param pen
     */
    public void setPen(Pen pen);

    /**
     * 获取画笔
     */
    public IGraffiti.Pen getPen();

    /**
     * 设置画笔形状
     *
     * @param shape
     */
    public void setShape(Shape shape);

    /**
     * 获取画笔形状
     */
    public Shape getShape();

    /**
     * 设置图片偏移量x
     *
     * @param transX
     */
    public void setTrans(float transX, float transY);


    /**
     * 设置图片偏移量x
     *
     * @param transX
     */
    public void setTransX(float transX);

    /**
     * 获取图片偏移量x
     *
     * @return
     */
    public float getTransX();

    /**
     * 设置图片偏移量y
     *
     * @param transY
     */
    public void setTransY(float transY);

    /**
     * 获取图片偏移量y
     *
     * @return
     */
    public float getTransY();

    /**
     * 设置大小
     *
     * @param paintSize
     */
    public void setSize(float paintSize);

    /**
     * 获取大小
     *
     * @return
     */
    public float getSize();

    /**
     * 设置颜色
     *
     * @param color
     */
    public void setColor(GraffitiColor color);

    /**
     * 获取颜色
     *
     * @return
     */
    public GraffitiColor getColor();

    /**
     * 最小缩放倍数限制
     *
     * @param minScale
     */
    public void setMinScale(float minScale);

    /**
     * 最小缩放倍数限制
     *
     * @return
     */
    public float getMinScale();

    /**
     * 最大缩放倍数限制
     *
     * @param maxScale
     */
    public void setMaxScale(float maxScale);

    /**
     * 最大缩放倍数限制
     *
     * @return
     */
    public float getMaxScale();

    /**
     * 添加item
     *
     * @param graffitiItem
     */
    public void addItem(IGraffitiItem graffitiItem);

    /**
     * 移除item
     *
     * @param graffitiItem
     */
    public void removeItem(IGraffitiItem graffitiItem);

    /**
     * 获取所有的涂鸦
     *
     * @return
     */
    public List<IGraffitiItem> getAllItem();

    /**
     * 是否
     *
     * @return
     */
    public boolean isSelectedItem();

    /**
     * 设置当前选中的item
     *
     * @param item
     */
    public void setSelectedItem(IGraffitiSelectableItem item);

    public IGraffitiSelectableItem getSelectedItem();

    /**
     * 设置放大镜倍数
     *
     * @param amplifierScale
     */
    public void setAmplifierScale(float amplifierScale);

    /**
     * 获取放大镜倍数
     *
     * @return
     */
    public float getAmplifierScale();


    /**
     * 是否允许涂鸦显示在图片边界之外
     *
     * @param isDrawableOutside
     */
    public void setIsDrawableOutside(boolean isDrawableOutside);

    /**
     * 是否允许涂鸦显示在图片边界之外
     */
    public boolean isDrawableOutside();

    /**
     * 是否显示原图
     *
     * @param justDrawOriginal
     */
    public void setShowOriginal(boolean justDrawOriginal);

    /**
     * 是否显示原图
     */
    public boolean isShowOriginal();

    /**
     * 保存当前涂鸦图片
     */
    public void save();

    /**
     * 清楚所有涂鸦
     */
    public void clear();

    /**
     * 置顶item
     *
     * @param item
     */
    public void topItem(IGraffitiItem item);

    /**
     * 置底item
     *
     * @param item
     */
    public void bottomItem(IGraffitiItem item);

    /**
     * 撤销一步
     *
     * @return
     */
    public boolean undo();

    /**
     * 指定撤销的步数
     *
     * @param step
     * @return
     */
    public boolean undo(int step);

    /**
     * 获取当前显示的图片(无涂鸦)
     *
     * @return
     */
    public Bitmap getBitmap();

    /**
     * 获取当前显示的图片(包含涂鸦)
     *
     * @return
     */
    public Bitmap getGraffitiBitmap();

    /**
     * 刷新
     */
    public void invalidate();

}
