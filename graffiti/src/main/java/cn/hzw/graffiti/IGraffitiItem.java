package cn.hzw.graffiti;

import android.graphics.Canvas;
import android.graphics.PointF;

/**
 * Created on 27/06/2018.
 */

public interface IGraffitiItem {

    public void setGraffiti(IGraffiti graffiti);

    public IGraffiti getGraffiti();

    public GraffitiView.Pen getPen();

    public void setPen(GraffitiView.Pen pen);

    public GraffitiView.Shape getShape();

    public void setShape(GraffitiView.Shape shape);

    public float getSize();

    public void setSize(float size);

    public GraffitiColor getColor();

    public void setColor(GraffitiColor color);

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

    public void setItemRotate(float degree);

    public float getItemRotate();
}
