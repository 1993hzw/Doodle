package cn.hzw.graffiti;

import android.graphics.Canvas;
import android.graphics.PointF;

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
    public GraffitiView.Pen getPen();

    /**
     * 设置画笔
     * @param pen
     */
    public void setPen(GraffitiView.Pen pen);

    /**
     * 获取画笔形状
     * @return
     */
    public GraffitiView.Shape getShape();

    /**
     * 设置画笔形状
     * @param shape
     */
    public void setShape(GraffitiView.Shape shape);

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
    public GraffitiColor getColor();

    /**
     * 设置颜色
     * @param color
     */
    public void setColor(GraffitiColor color);

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
}
