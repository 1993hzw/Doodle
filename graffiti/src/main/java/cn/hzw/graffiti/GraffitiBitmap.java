package cn.hzw.graffiti;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import static cn.hzw.graffiti.DrawUtil.GRAFFITI_PIXEL_UNIT;
import static cn.hzw.graffiti.DrawUtil.restoreRotatePointInGraffiti;
import static cn.hzw.graffiti.DrawUtil.rotatePoint;
import static cn.hzw.graffiti.DrawUtil.rotatePointInGraffiti;

/**
 * Created by huangziwei on 2017/3/16.
 */

public class GraffitiBitmap implements Undoable, GraffitiSelectableItem {


    private final static Paint sPaint = new Paint();
    private Bitmap mBitmap;
    private float mSize;
    private GraffitiColor mColor;
    private float mTextRotate; // 文字的旋转角度
    private int mRotateDegree; // 图片的旋转角度
    float mPivotX, mPivotY;
    private float mX, mY;

    private Rect mRect = new Rect();

    private Rect mSrcRect = new Rect();
    private Rect mDstRect = new Rect();

    public GraffitiBitmap(Bitmap bitmap, float size, GraffitiColor color, int textRotate, int rotateDegree, float x, float y, float px, float py) {
        this.mBitmap = bitmap;
        this.mSize = size;
        this.mColor = color;
        this.mTextRotate = textRotate;
        this.mRotateDegree = rotateDegree;
        this.mX = x;
        this.mY = y;
        this.mPivotX = px;
        this.mPivotY = py;

        resetBounds();
    }

    private void resetBounds() {
        mRect.set(0, 0, (int) mSize, (int) (mSize * mBitmap.getHeight() / mBitmap.getWidth()));
        mRect.left -= 10 * GRAFFITI_PIXEL_UNIT;
        mRect.top -= 10 * GRAFFITI_PIXEL_UNIT;
        mRect.right += 10 * GRAFFITI_PIXEL_UNIT;
        mRect.bottom += 10 * GRAFFITI_PIXEL_UNIT;

        mSrcRect.set(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        mDstRect.set(0, 0, (int) mSize, (int) (mSize * mBitmap.getHeight()) / mBitmap.getWidth());
    }

    public float getSize() {
        return mSize;
    }

    public void setSize(float size) {
        mSize = size;
        resetBounds();
    }

    public void setXy(int currentRotate, float x, float y) {
        float[] xy = restoreRotatePointInGraffiti(currentRotate, mRotateDegree, x, y, mPivotX, mPivotY);
        mX = xy[0];
        mY = xy[1];
    }

    public float[] getXy(int currentDegree) {
        return rotatePointInGraffiti(currentDegree, mRotateDegree, mX, mY, mPivotX, mPivotY);
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        resetBounds();
    }

    public Bitmap getBitmap() {
        return mBitmap;
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
        mTextRotate = textRotate;
    }

    public float getItemRotate() {
        return mTextRotate;
    }

    public int getGraffitiRotate() {
        return mRotateDegree;
    }

    // 判断xy是否在文字范围内
    @Override
    public boolean isInIt(int currentRotate, float x, float y, GraffitiView.Pen pen) {
        if (pen != GraffitiView.Pen.BITMAP) {
            return false;
        }
        float[] xy = getXy(currentRotate);
        // 把触摸点转换成在文字坐标系（即以文字起始点作为坐标原点）内的点
        x = x - xy[0];
        y = y - xy[1];
        // 把变换后相对于矩形的触摸点，还原回未变换前的点，然后判断是否矩形中
        float[] rectXy = rotatePoint((int) -(currentRotate - mRotateDegree + mTextRotate), x, y, 0, 0);
        return mRect.contains((int) rectXy[0], (int) rectXy[1]);
    }

    @Override
    public boolean isCanRotate(int currentRotate, float x, float y) {
        float[] xy = getXy(currentRotate);
        // 把触摸点转换成在文字坐标系（即以文字起始点作为坐标原点）内的点
        x = x - xy[0];
        y = y - xy[1];
        // 把变换后矩形中的触摸点，还原回未变换前矩形中的点，然后判断是否矩形中
        float[] rectXy = rotatePoint((int) -(currentRotate - mRotateDegree + mTextRotate), x, y, 0, 0);

        return rectXy[0] >= mRect.right && rectXy[0] <= mRect.right + ITEM_CAN_ROTATE_BOUND * GRAFFITI_PIXEL_UNIT
                && rectXy[1] >= mRect.top && rectXy[1] <= mRect.bottom;
    }

    @Override
    public void draw(Canvas canvas, GraffitiView graffitiView, Paint paint) {
        canvas.drawBitmap(mBitmap, mSrcRect, mDstRect, null);
    }

}


