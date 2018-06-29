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
        TEXT, // 文本
        BITMAP, // 贴图
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

    public float getSizeUnit();

    public boolean isRotatingItem();

    public int getRotate();

    public void setRotate(int degree);

    public void setScale(float scale, float pivotX, float pivotY);

    public float getScale();

    public boolean undo();

    public boolean undo(int step);


    public void setPen(Pen pen);

    public IGraffiti.Pen getPen();

    /**
     * 设置画笔形状
     *
     * @param shape
     */
    public void setShape(Shape shape);

    public Shape getShape();


    public void setTrans(float transX, float transY);

    public void setTransX(float transX);

    public float getTransX();

    public void setTransY(float transY);

    public float getTransY();


    public void setSize(float paintSize);

    public float getSize();

    public void setColor(GraffitiColor color);

    public GraffitiColor getColor();

    public void setMinScale(float minScale);

    public float getMinScale();

    public void setMaxScale(float maxScale);

    public float getMaxScale();

    public void addItem(IGraffitiItem graffitiItem);

    public void removeItem(IGraffitiItem graffitiItem);

    public List<IGraffitiItem> getAllItem();

    public void invalidate();

    public boolean isSelectedItem();

    public IGraffitiSelectableItem getSelectedItem();

    public void setAmplifierScale(float amplifierScale);

    public float getAmplifierScale();

    public void save();

    public void clear();

    public void topItem();

    public void setIsDrawableOutside(boolean isDrawableOutside);

    public boolean isDrawableOutside();

    public void setShowOriginal(boolean justDrawOriginal);

    public boolean isShowOriginal();

    public float getBitmapWidth();

    public float getBitmapHeight();

    public Bitmap getBitmap();


}
