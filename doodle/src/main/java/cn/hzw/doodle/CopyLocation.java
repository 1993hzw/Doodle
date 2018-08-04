package cn.hzw.doodle;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import static cn.hzw.doodle.util.DrawUtil.drawCircle;

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

    private PointF mTemp = new PointF();

    public CopyLocation() {
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

    public float getX() {
        return mX;
    }

    public float getY() {
        return mY;
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

    public void setStartPosition(float touchStartX, float touchStartY) {
        setStartPosition(touchStartX, touchStartY, mX, mY);
    }

    public void setStartPosition( float touchStartX, float touchStartY, float copyStartX, float copyStartY) {
        mCopyStartX = copyStartX;
        mCopyStartY = copyStartY;
        mTouchStartX = touchStartX;
        mTouchStartY = touchStartY;
    }

    public void drawItSelf(Canvas canvas, float size) {
        mPaint.setStrokeWidth(size / 4);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(0xaa666666); // 灰色
        drawCircle(canvas, mX, mY, size / 2 + size / 8, mPaint);

        mPaint.setStrokeWidth(size / 16);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(0xaaffffff); // 白色
        drawCircle(canvas, mX, mY, size / 2 + size / 32, mPaint);

        mPaint.setStyle(Paint.Style.FILL);
        if (!mIsCopying) {
            mPaint.setColor(0x44ff0000); // 红色
            drawCircle(canvas, mX, mY, size / 2, mPaint);
        } else {
            mPaint.setColor(0x44000088); // 蓝色
            drawCircle(canvas, mX, mY, size / 2, mPaint);
        }
    }

    /**
     * 判断是否点中
     */
    public boolean contains(float x, float y, float mPaintSize) {
        return (mX - x) * (mX - x) + (mY - y) * (mY - y) <= mPaintSize * mPaintSize;
    }

    public CopyLocation copy() {
        CopyLocation copyLocation = new CopyLocation();
        copyLocation.mCopyStartX = mCopyStartX;
        copyLocation.mCopyStartY = mCopyStartY;
        copyLocation.mTouchStartX = mTouchStartX;
        copyLocation.mTouchStartY = mTouchStartY;
        copyLocation.mX = mX;
        copyLocation.mY = mY;
        return copyLocation;
    }

    public void reset() {
        mCopyStartX = mCopyStartY = mTouchStartX = mTouchStartY = mX = mY = 0;
        mIsRelocating = true; // 正在定位中
        mIsCopying = false; // 正在仿制绘图中
    }

}

