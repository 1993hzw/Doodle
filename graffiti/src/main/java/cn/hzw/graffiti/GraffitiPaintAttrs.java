package cn.hzw.graffiti;

import cn.hzw.graffiti.core.IGraffitiColor;
import cn.hzw.graffiti.core.IGraffitiPen;
import cn.hzw.graffiti.core.IGraffitiShape;

/**
 * 画笔属性
 * Created on 29/06/2018.
 */

public class GraffitiPaintAttrs {
    private IGraffitiPen mPen; // 画笔类型
    private IGraffitiShape mShape; // 画笔形状
    private float mSize; // 大小
    private IGraffitiColor mColor; // 颜色

    public IGraffitiPen pen() {
        return mPen;
    }

    public GraffitiPaintAttrs pen(IGraffitiPen pen) {
        mPen = pen;
        return this;
    }

    public IGraffitiShape shape() {
        return mShape;
    }

    public GraffitiPaintAttrs shape(IGraffitiShape shape) {
        mShape = shape;
        return this;
    }

    public float size() {
        return mSize;
    }

    public GraffitiPaintAttrs size(float size) {
        mSize = size;
        return this;
    }

    public IGraffitiColor color() {
        return mColor;
    }

    public GraffitiPaintAttrs color(IGraffitiColor color) {
        mColor = color;
        return this;
    }

    public static GraffitiPaintAttrs create() {
        return new GraffitiPaintAttrs();
    }
}
