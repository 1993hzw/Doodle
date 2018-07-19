package cn.hzw.doodle;

import cn.hzw.doodle.core.IDoodleColor;
import cn.hzw.doodle.core.IDoodlePen;
import cn.hzw.doodle.core.IDoodleShape;

/**
 * 画笔属性
 * Created on 29/06/2018.
 */

public class DoodlePaintAttrs {
    private IDoodlePen mPen; // 画笔类型
    private IDoodleShape mShape; // 画笔形状
    private float mSize; // 大小
    private IDoodleColor mColor; // 颜色

    public IDoodlePen pen() {
        return mPen;
    }

    public DoodlePaintAttrs pen(IDoodlePen pen) {
        mPen = pen;
        return this;
    }

    public IDoodleShape shape() {
        return mShape;
    }

    public DoodlePaintAttrs shape(IDoodleShape shape) {
        mShape = shape;
        return this;
    }

    public float size() {
        return mSize;
    }

    public DoodlePaintAttrs size(float size) {
        mSize = size;
        return this;
    }

    public IDoodleColor color() {
        return mColor;
    }

    public DoodlePaintAttrs color(IDoodleColor color) {
        mColor = color;
        return this;
    }

    public static DoodlePaintAttrs create() {
        return new DoodlePaintAttrs();
    }
}
