package cn.hzw.graffiti;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import cn.hzw.graffiti.core.IGraffiti;

import static cn.hzw.graffiti.util.DrawUtil.drawCircle;
import static cn.hzw.graffiti.util.DrawUtil.rotatePointInGraffiti;

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
    private float mOriginalPivotX, mOriginalPivotY; // // 原图的中心位置

    private IGraffiti mIGraffiti;

    public CopyLocation(IGraffiti graffiti, float copyStartX, float copyStartY, float touchStartX, float touchStartY) {
        this.mCopyStartX = copyStartX;
        this.mCopyStartY = copyStartY;
        this.mTouchStartX = touchStartX;
        this.mTouchStartY = touchStartY;
        mIGraffiti = graffiti;

        init();
    }

    public CopyLocation(IGraffiti graffiti, float x, float y) {
        mX = x;
        mY = y;
        mTouchStartX = x;
        mTouchStartY = y;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mIGraffiti = graffiti;

        init();
    }

    private void init() {
        int bitmapWidth = mIGraffiti.getBitmap().getWidth();
        int bitmapHeight = mIGraffiti.getBitmap().getHeight();
        int degree = mIGraffiti.getRotate();
        if (Math.abs(degree) == 90 || Math.abs(degree) == 270) { // 获取原始图片的宽高
            int t = bitmapWidth;
            bitmapWidth = bitmapHeight;
            bitmapHeight = t;
        }
        mOriginalPivotX = bitmapWidth / 2;
        mOriginalPivotY = bitmapHeight / 2;
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
    public boolean isInIt(float x, float y, float mPaintSize) {
        if ((mX - x) * (mX - x) + (mY - y) * (mY - y) <= mPaintSize * mPaintSize) {
            return true;
        }
        return false;
    }

    public CopyLocation copy() {
        CopyLocation copyLocation = new CopyLocation(mIGraffiti, mCopyStartX, mCopyStartY, mTouchStartX, mTouchStartY);
        return copyLocation;
    }

    private PointF xy = new PointF();

    public void rotatePosition(int oldRotate, int nowRotate) {
        // 旋转仿制图标的位置
        PointF coords = rotatePointInGraffiti(xy, nowRotate, oldRotate, this.mX,
                this.mY, mOriginalPivotX, mOriginalPivotY);
        this.mX = coords.x;
        this.mY = coords.y;

        coords = rotatePointInGraffiti(xy, nowRotate, oldRotate, this.mCopyStartX,
                this.mCopyStartY, mOriginalPivotX, mOriginalPivotY);
        this.mCopyStartX = coords.x;
        this.mCopyStartY = coords.y;

        coords = rotatePointInGraffiti(xy, nowRotate, oldRotate, this.mTouchStartX,
                this.mTouchStartY, mOriginalPivotX, mOriginalPivotY);
        this.mTouchStartX = coords.x;
        this.mTouchStartY = coords.y;
    }

}

