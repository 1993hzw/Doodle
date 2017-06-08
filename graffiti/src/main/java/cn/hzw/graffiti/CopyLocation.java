package cn.hzw.graffiti;

import android.graphics.Canvas;
import android.graphics.Paint;

import static cn.hzw.graffiti.DrawUtil.drawCircle;
import static cn.hzw.graffiti.DrawUtil.rotatePointInGraffiti;

/**
 * 仿制的定位器
 */
public class CopyLocation {

    private float mCopyStartX, mCopyStartY; // 仿制的坐标
    private float mTouchStartX, mTouchStartY; // 开始触摸的坐标
    private float mX, mY; // 当前位置

    private Paint mPaint;

    private boolean mIsRelocating = true; // 正在定位中
    private boolean mIsCopying = false; // 正在仿制绘图中

    public CopyLocation(float copyStartX, float copyStartY, float touchStartX, float touchStartY) {
        this.mCopyStartX = copyStartX;
        this.mCopyStartY = copyStartY;
        this.mTouchStartX = touchStartX;
        this.mTouchStartY = touchStartY;
    }

    public CopyLocation(float x, float y) {
        mX = x;
        mY = y;
        mTouchStartX = x;
        mTouchStartY = y;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
    }


    public float getTouchStartX() {
        return mTouchStartX;
    }

    public float getTouchStartY() {
        return mTouchStartY;
    }

    public float getCopyStartX() {
        return mCopyStartX;
    }

    public float getCopyStartY() {
        return mCopyStartY;
    }

    public boolean isCopying() {
        return mIsCopying;
    }

    public boolean isRelocating() {
        return mIsRelocating;
    }

    public void setCopying(boolean copying) {
        mIsCopying = copying;
    }

    public void setRelocating(boolean relocating) {
        mIsRelocating = relocating;
    }

    public void updateLocation(float x, float y) {
        mX = x;
        mY = y;
    }

    public void setStartPosition(float x, float y) {
        mCopyStartX = mX;
        mCopyStartY = mY;
        mTouchStartX = x;
        mTouchStartY = y;
    }

    public void drawItSelf(Canvas canvas, float mPaintSize) {
        mPaint.setStrokeWidth(mPaintSize / 4);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(0xaa666666); // 灰色
        drawCircle(canvas, mX, mY, mPaintSize / 2 + mPaintSize / 8, mPaint);

        mPaint.setStrokeWidth(mPaintSize / 16);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(0xaaffffff); // 白色
        drawCircle(canvas, mX, mY, mPaintSize / 2 + mPaintSize / 32, mPaint);

        mPaint.setStyle(Paint.Style.FILL);
        if (!mIsCopying) {
            mPaint.setColor(0x44ff0000); // 红色
            drawCircle(canvas, mX, mY, mPaintSize / 2, mPaint);
        } else {
            mPaint.setColor(0x44000088); // 蓝色
            drawCircle(canvas, mX, mY, mPaintSize / 2, mPaint);
        }
    }

    /**
     * 判断是否点中
     */
    public boolean isInIt(float x, float y, float mPaintSize) {
        if ((mX - x) * (mX - x) + (mY - y) * (mY - y) <= mPaintSize * mPaintSize) {
            return true;
        }
        return false;
    }

    public CopyLocation copy() {
        CopyLocation copyLocation = new CopyLocation(mCopyStartX, mCopyStartY, mTouchStartX, mTouchStartY);
        return copyLocation;
    }

    public void rotatePosition(int originalDegree, int mRotateDegree, float mOriginalPivotX, float mOriginalPivotY) {
        // 旋转仿制图标的位置
        float[] coords = rotatePointInGraffiti(mRotateDegree, originalDegree, this.mX,
                this.mY, mOriginalPivotX, mOriginalPivotY);
        this.mX = coords[0];
        this.mY = coords[1];

        coords = rotatePointInGraffiti(mRotateDegree, originalDegree, this.mCopyStartX,
                this.mCopyStartY, mOriginalPivotX, mOriginalPivotY);
        this.mCopyStartX = coords[0];
        this.mCopyStartY = coords[1];

        coords = rotatePointInGraffiti(mRotateDegree, originalDegree, this.mTouchStartX,
                this.mTouchStartY, mOriginalPivotX, mOriginalPivotY);
        this.mTouchStartX = coords[0];
        this.mTouchStartY = coords[1];
    }

}

