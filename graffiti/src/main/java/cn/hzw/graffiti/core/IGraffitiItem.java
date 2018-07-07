package cn.hzw.graffiti.core;

import android.graphics.Canvas;
import android.graphics.PointF;

import cn.hzw.graffiti.GraffitiColor;

/**
 * Created on 27/06/2018.
 */

public interface IGraffitiItem {

    public void setGraffiti(IGraffiti graffiti);

    public IGraffiti getGraffiti();

    /**
     * 获取画笔
     * @return
     */
    public IGraffitiPen getPen();

    /**
     * 设置画笔
     * @param pen
     */
    public void setPen(IGraffitiPen pen);

    /**
     * 获取画笔形状
     * @return
     */
    public IGraffitiShape getShape();

    /**
     * 设置画笔形状
     * @param shape
     */
    public void setShape(IGraffitiShape shape);

    /**
     * 获取大小
     * @return
     */
    public float getSize();

    /**
     * 设置大小
     * @param size
     */
    public void setSize(float size);

    /**
     * 获取颜色
     * @return
     */
    public IGraffitiColor getColor();

    /**
     * 设置颜色
     * @param color
     */
    public void setColor(IGraffitiColor color);

    /**
     * 绘制item
     * @param canvas
     */
    public void draw(Canvas canvas);

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
     * 设置item的旋转值
     * @param degree
     */
    public void setItemRotate(float degree);

    /**
     * 获取item的旋转值
     * @return
     */
    public float getItemRotate();

    /**
     * 是否需要裁剪图片区域外的部分
     * @return
     */
    public boolean isNeedClipOutside();

    /**
     * 设置是否需要裁剪图片区域外的部分
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
}
