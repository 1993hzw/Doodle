package cn.hzw.graffiti;

/**
 * 画布属性
 * Created on 29/06/2018.
 */

public class GraffitiPaintAttrs {
    private GraffitiView.Pen mPen; // 画笔类型
    private GraffitiView.Shape mShape; // 画笔形状
    private float mSize; // 大小
    private GraffitiColor mColor; // 颜色

    public GraffitiView.Pen pen() {
        return mPen;
    }

    public GraffitiPaintAttrs pen(GraffitiView.Pen pen) {
        mPen = pen;
        return this;
    }

    public GraffitiView.Shape shape() {
        return mShape;
    }

    public GraffitiPaintAttrs shape(GraffitiView.Shape shape) {
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

    public GraffitiColor color() {
        return mColor;
    }

    public GraffitiPaintAttrs color(GraffitiColor color) {
        mColor = color;
        return this;
    }

    public static GraffitiPaintAttrs create() {
        return new GraffitiPaintAttrs();
    }
}
