package cn.hzw.doodle.core;

import android.graphics.Bitmap;

import java.util.List;

/**
 * Created on 27/06/2018.
 */

public interface IDoodle {
    /**
     * Gets the unit size in the current doodle coordinate system, which refers to the dp, independent of the image
     * 获取当前涂鸦坐标系中的单位大小，该单位参考dp，独立于图片
     *
     * @return
     */
    public float getUnitSize();

    /**
     * 设置图片旋转值
     *
     * @param degree
     */
    public void setDoodleRotation(int degree);

    /**
     * 获取图片旋转值
     *
     * @return
     */
    public int getDoodleRotation();

    /**
     * 设置图片缩放倍数
     *
     * @param scale
     * @param pivotX
     * @param pivotY
     */
    public void setDoodleScale(float scale, float pivotX, float pivotY);

    /**
     * 获取图片缩放倍数
     */
    public float getDoodleScale();

    /**
     * 设置画笔
     *
     * @param pen
     */
    public void setPen(IDoodlePen pen);

    /**
     * 获取画笔
     */
    public IDoodlePen getPen();

    /**
     * 设置画笔形状
     *
     * @param shape
     */
    public void setShape(IDoodleShape shape);

    /**
     * 获取画笔形状
     */
    public IDoodleShape getShape();

    /**
     * 设置图片偏移量x
     *
     * @param transX
     */
    public void setDoodleTranslation(float transX, float transY);


    /**
     * 设置图片偏移量x
     *
     * @param transX
     */
    public void setDoodleTranslationX(float transX);

    /**
     * 获取图片偏移量x
     *
     * @return
     */
    public float getDoodleTranslationX();

    /**
     * 设置图片偏移量y
     *
     * @param transY
     */
    public void setDoodleTranslationY(float transY);

    /**
     * 获取图片偏移量y
     *
     * @return
     */
    public float getDoodleTranslationY();

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
    public void setColor(IDoodleColor color);

    /**
     * 获取颜色
     *
     * @return
     */
    public IDoodleColor getColor();

    /**
     * 最小缩放倍数限制
     *
     * @param minScale
     */
    public void setDoodleMinScale(float minScale);

    /**
     * 最小缩放倍数限制
     *
     * @return
     */
    public float getDoodleMinScale();

    /**
     * 最大缩放倍数限制
     *
     * @param maxScale
     */
    public void setDoodleMaxScale(float maxScale);

    /**
     * 最大缩放倍数限制
     *
     * @return
     */
    public float getDoodleMaxScale();

    /**
     * 添加item
     *
     * @param doodleItem
     */
    public void addItem(IDoodleItem doodleItem);

    /**
     * 移除item
     *
     * @param doodleItem
     */
    public void removeItem(IDoodleItem doodleItem);

    /**
     * total item count
     *
     * @return
     */
    public int getItemCount();

    /**
     * 获取所有的涂鸦
     *
     * @return
     */
    public List<IDoodleItem> getAllItem();

    /**
     * 设置放大镜倍数
     *
     * @param scale
     */
    public void setZoomerScale(float scale);

    /**
     * 获取放大镜倍数
     *
     * @return
     */
    public float getZoomerScale();


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
    public void topItem(IDoodleItem item);

    /**
     * 置底item
     *
     * @param item
     */
    public void bottomItem(IDoodleItem item);

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
    public Bitmap getDoodleBitmap();

    /**
     * 刷新
     */
    public void refresh();

}
