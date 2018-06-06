package cn.hzw.graffiti;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import static cn.hzw.graffiti.DrawUtil.GRAFFITI_PIXEL_UNIT;
import static cn.hzw.graffiti.DrawUtil.restoreRotatePointInGraffiti;
import static cn.hzw.graffiti.DrawUtil.rotatePoint;
import static cn.hzw.graffiti.DrawUtil.rotatePointInGraffiti;

/**
 * 可选择的涂鸦条目，例如文字、图片
 * Created by huangziwei on 2017/7/16.
 */

public abstract class GraffitiSelectableItem implements Undoable {

    public final static int ITEM_CAN_ROTATE_BOUND = 80;

    private GraffitiView.Pen mPen;
    private float mSize;
    private GraffitiColor mColor;
    private float mItemRotate; // item的旋转角度
    private int mGraffitiDegree; // 涂鸦图片的旋转角度
    float mPivotX, mPivotY;
    private float mX, mY;

    private Rect mRect = new Rect();

    public GraffitiSelectableItem(GraffitiView.Pen pen, float size, GraffitiColor color, int itemRotate, int graffitiDegree, float x, float y, float px, float py) {
        this.mPen = pen;
        this.mSize = size;
        this.mColor = color;
        this.mItemRotate = itemRotate;
        this.mGraffitiDegree = graffitiDegree;
        this.mX = x;
        this.mY = y;
        this.mPivotX = px;
        this.mPivotY = py;

        resetBounds(mRect);
    }

    public Rect getBounds() {
        return mRect;
    }

    public float getSize() {
        return mSize;
    }

    public void setSize(float size) {
        mSize = size;
        resetBounds(mRect);
    }

    public void setXy(int currentRotate, float x, float y) {
        float[] xy = restoreRotatePointInGraffiti(currentRotate, mGraffitiDegree, x, y, mPivotX, mPivotY);
        mX = xy[0];
        mY = xy[1];
    }

    public float[] getXy(int currentDegree) {
        return rotatePointInGraffiti(currentDegree, mGraffitiDegree, mX, mY, mPivotX, mPivotY);
    }

    public GraffitiColor getColor() {
        return mColor;
    }

    public void setColor(GraffitiColor color) {
        mColor = color;
    }

    public Rect getBounds(int currentRotate) {
        return mRect;
    }

    public void setItemRotate(float textRotate) {
        mItemRotate = textRotate;
    }

    public float getItemRotate() {
        return mItemRotate;
    }

    public int getGraffitiRotate() {
        return mGraffitiDegree;
    }

    /**
     * 是否击中
     */
    public boolean isInIt(int currentRotate, float x, float y, GraffitiView.Pen pen) {
        if (pen != mPen) {
            return false;
        }
        float[] xy = getXy(currentRotate);
        // 把触摸点转换成在文字坐标系（即以文字起始点作为坐标原点）内的点
        x = x - xy[0];
        y = y - xy[1];
        // 把变换后相对于矩形的触摸点，还原回未变换前的点，然后判断是否矩形中
        float[] rectXy = rotatePoint((int) -(currentRotate - mGraffitiDegree + mItemRotate), x, y, 0, 0);
        return mRect.contains((int) rectXy[0], (int) rectXy[1]);
    }

    /**
     * 是否可以旋转
     */
    public boolean isCanRotate(int currentRotate, float x, float y) {
        float[] xy = getXy(currentRotate);
        // 把触摸点转换成在item坐标系（即以item起始点作为坐标原点）内的点
        x = x - xy[0];
        y = y - xy[1];
        // 把变换后矩形中的触摸点，还原回未变换前矩形中的点，然后判断是否矩形中
        float[] rectXy = rotatePoint((int) -(currentRotate - mGraffitiDegree + mItemRotate), x, y, 0, 0);

        return rectXy[0] >= mRect.right && rectXy[0] <= mRect.right + ITEM_CAN_ROTATE_BOUND * GRAFFITI_PIXEL_UNIT
                && rectXy[1] >= mRect.top && rectXy[1] <= mRect.bottom;
    }

    public abstract void resetBounds(Rect rect);

    public abstract void draw(Canvas canvas, GraffitiView graffitiView, Paint paint);
}
