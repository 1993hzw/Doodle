package cn.hzw.graffiti;

import android.graphics.Canvas;
import android.graphics.PointF;

import static cn.hzw.graffiti.util.DrawUtil.restoreRotatePointInGraffiti;
import static cn.hzw.graffiti.util.DrawUtil.rotatePointInGraffiti;

/**
 * Created on 29/06/2018.
 */

public abstract class GraffitiItemBase implements IGraffitiItem {

    private float mItemRotate; // item的旋转角度
    private int mGraffitiRotate; // 涂鸦图片的旋转角度
    private float mOriginalX, mOriginalY; // 在原图中的起始位置

    private IGraffiti mGraffiti;
    private float mOriginalPivotX, mOriginalPivotY; // // 原图的中心位置

    private PointF mLocationTemp = new PointF();

    private GraffitiView.Pen mPen; // 画笔类型
    private GraffitiView.Shape mShape; // 画笔形状
    private float mSize; // 大小
    private GraffitiColor mColor; // 颜色

    public GraffitiItemBase(IGraffiti graffiti) {
        this(graffiti, null);
    }

    public GraffitiItemBase(IGraffiti graffiti, GraffitiPaintAttrs attrs) {
        setGraffiti(graffiti);
        if (attrs != null) {
            mPen = attrs.pen();
            mShape = attrs.shape();
            mSize = attrs.size();
            mColor = attrs.color();
        }
    }

    @Override
    public void setGraffiti(IGraffiti graffiti) {
        mGraffiti = graffiti;
        if (graffiti == null) {
            return;
        }
        mGraffitiRotate = graffiti.getRotate();
        mOriginalPivotX = graffiti.getOriginalBitmapWidth() / 2;
        mOriginalPivotY = graffiti.getOriginalBitmapHeight() / 2;
    }

    @Override
    public IGraffiti getGraffiti() {
        return mGraffiti;
    }

    @Override
    public void setItemRotate(float textRotate) {
        mItemRotate = textRotate;
    }

    @Override
    public float getItemRotate() {
        return mItemRotate;
    }

    @Override
    public void setLocation(float x, float y) {
        mLocationTemp = restoreRotatePointInGraffiti(mLocationTemp, mGraffiti.getRotate(), mGraffitiRotate, x, y, mOriginalPivotX, mOriginalPivotY);
        mOriginalX = mLocationTemp.x;
        mOriginalY = mLocationTemp.y;

        // 使用下面的代码 旋转后移动异常
//        mOriginalX = x;
//        mOriginalY = y;
    }

    @Override
    public PointF getLocation() {
        return rotatePointInGraffiti(mLocationTemp, mGraffiti.getRotate(), mGraffitiRotate, mOriginalX, mOriginalY, mOriginalPivotX, mOriginalPivotY);
    }

    public float getOriginalPivotX() {
        return mOriginalPivotX;
    }

    public float getOriginalPivotY() {
        return mOriginalPivotY;
    }

    public int getGraffitiRotate() {
        return mGraffitiRotate;
    }

    public GraffitiView.Pen getPen() {
        return mPen;
    }

    public void setPen(GraffitiView.Pen pen) {
        mPen = pen;
    }

    public GraffitiView.Shape getShape() {
        return mShape;
    }

    public void setShape(GraffitiView.Shape shape) {
        mShape = shape;
    }

    public float getSize() {
        return mSize;
    }

    public void setSize(float size) {
        mSize = size;
    }

    public GraffitiColor getColor() {
        return mColor;
    }

    public void setColor(GraffitiColor color) {
        mColor = color;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();

        mLocationTemp = getLocation(); // 获取旋转后的起始坐标
        canvas.translate(mLocationTemp.x, mLocationTemp.y); // 把坐标系平移到文字矩形范围
        canvas.rotate(mGraffiti.getRotate() - mGraffitiRotate + mItemRotate, 0, 0); // 旋转坐标系

        if (mGraffiti.getSelectedItem() == this) {
            ((IGraffitiSelectableItem) this).drawSelectedBackground(mGraffiti, canvas);
        }

        doDraw(canvas);

        canvas.restore();

    }

    protected abstract void doDraw(Canvas canvas);
}
