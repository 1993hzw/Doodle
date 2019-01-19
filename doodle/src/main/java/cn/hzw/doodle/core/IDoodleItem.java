package cn.hzw.doodle.core;

import android.graphics.Canvas;
import android.graphics.PointF;

/**
 * Created on 27/06/2018.
 */

public interface IDoodleItem {

    public void setDoodle(IDoodle doodle);

    public IDoodle getDoodle();

    /**
     * 获取画笔
     *
     * @return
     */
    public IDoodlePen getPen();

    /**
     * 设置画笔
     *
     * @param pen
     */
    public void setPen(IDoodlePen pen);

    /**
     * 获取画笔形状
     *
     * @return
     */
    public IDoodleShape getShape();

    /**
     * 设置画笔形状
     *
     * @param shape
     */
    public void setShape(IDoodleShape shape);

    /**
     * 获取大小
     *
     * @return
     */
    public float getSize();

    /**
     * 设置大小
     *
     * @param size
     */
    public void setSize(float size);

    /**
     * 获取颜色
     *
     * @return
     */
    public IDoodleColor getColor();

    /**
     * 设置颜色
     *
     * @param color
     */
    public void setColor(IDoodleColor color);

    /**
     * 绘制item
     *
     * @param canvas
     */
    public void draw(Canvas canvas);

    /**
     * 画在所有item的上面
     * @param canvas
     */
    void drawAtTheTop(Canvas canvas);

    /**
     * 设置在当前涂鸦中的左上角位置
     *
     * @param x
     * @param y
     */
    public void setLocation(float x, float y);

    /**
     * 获取当前涂鸦中的起始坐标
     */
    public PointF getLocation();

    /**
     * item中心点x
     *
     * @param pivotX
     */
    public void setPivotX(float pivotX);

    /**
     * item中心点x
     */
    public float getPivotX();

    /**
     * item中心点y
     *
     * @param pivotY
     */
    public void setPivotY(float pivotY);

    /**
     * item中心点y
     */
    public float getPivotY();

    /**
     * 设置item的旋转值，围绕中心点Pivot旋转
     *
     * @param degree
     */
    public void setItemRotate(float degree);

    /**
     * 获取item的旋转值
     *
     * @return
     */
    public float getItemRotate();

    /**
     * 是否需要裁剪图片区域外的部分
     *
     * @return
     */
    public boolean isNeedClipOutside();

    /**
     * 设置是否需要裁剪图片区域外的部分
     *
     * @param clip
     */
    public void setNeedClipOutside(boolean clip);

    /**
     * 添加进涂鸦时回调
     */
    public void onAdd();

    /**
     * 移除涂鸦时回调
     */
    public void onRemove();

    /**
     * 刷新
     */
    public void refresh();

    /**
     * item是否可以编辑。用于编辑模式下对item的操作
     * @return
     */
    public boolean isDoodleEditable();

    /**
     * 缩放倍数，围绕(PivotX,PivotY)旋转
     */
    public void setScale(float scale);

    public float getScale();

    /**
     * 监听器
     * @param listener
     */
    public void addItemListener(IDoodleItemListener listener);

    public void removeItemListener(IDoodleItemListener listener);
}
