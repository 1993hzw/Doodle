package cn.hzw.graffiti;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import static cn.hzw.graffiti.DrawUtil.restoreRotatePointInGraffiti;
import static cn.hzw.graffiti.DrawUtil.rotatePoint;
import static cn.hzw.graffiti.DrawUtil.rotatePointInGraffiti;

/**
 * 可选择的涂鸦条目，例如文字、图片
 * Created by huangziwei on 2017/7/16.
 */

public abstract class GraffitiSelectableItemBase implements IGraffitiSelectableItem {

    public final static int ITEM_CAN_ROTATE_BOUND = 80;

    private float mSize;
    private GraffitiColor mColor;
    private float mItemRotate; // item的旋转角度
    private int mGraffitiDegree; // 涂鸦图片的旋转角度
    float mPivotX, mPivotY;
    private float mX, mY;

    private Rect mRect = new Rect();
    private Rect mRectTemp = new Rect();
    private Paint paint = new Paint();

    private IGraffiti mGraffiti;

    public GraffitiSelectableItemBase(float size, GraffitiColor color, int itemRotate, int graffitiDegree, float x, float y) {
        this.mSize = size;
        this.mColor = color;
        this.mItemRotate = itemRotate;
        this.mGraffitiDegree = graffitiDegree;
        this.mX = x;
        this.mY = y;

        resetBounds(mRect);
    }

    @Override
    public void setGraffiti(IGraffiti graffiti) {
        mGraffiti = graffiti;
        if (graffiti == null) {
            return;
        }
        mPivotX = graffiti.getBitmapWidth() / 2;
        mPivotY = graffiti.getBitmapHeight() / 2;
    }

    protected Rect getBounds() {
        return mRect;
    }

    public float getSize() {
        return mSize;
    }

    public void setSize(float size) {
        mSize = size;
        resetBounds(mRect);
    }

    public void setXy(float x, float y) {
        float[] xy = restoreRotatePointInGraffiti(mGraffiti.getRotate(), mGraffitiDegree, x, y, mPivotX, mPivotY);
        mX = xy[0];
        mY = xy[1];
    }

    public float[] getXy() {
        return rotatePointInGraffiti(mGraffiti.getRotate(), mGraffitiDegree, mX, mY, mPivotX, mPivotY);
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
    @Override
    public boolean isInIt(IGraffiti graffiti, float x, float y) {
        int currentRotate = graffiti.getRotate();
        float[] xy = getXy();
        // 把触摸点转换成在文字坐标系（即以文字起始点作为坐标原点）内的点
        x = x - xy[0];
        y = y - xy[1];
        // 把变换后相对于矩形的触摸点，还原回未变换前的点，然后判断是否矩形中
        float[] rectXy = rotatePoint((int) -(currentRotate - mGraffitiDegree + mItemRotate), x, y, 0, 0);
        mRectTemp.set(mRect);
        float unit = graffiti.getSizeUnit();
        mRectTemp.left -= 10 * unit;
        mRectTemp.top -= 10 * unit;
        mRectTemp.right += 10 * unit;
        mRectTemp.bottom += 10 * unit;
        return mRectTemp.contains((int) rectXy[0], (int) rectXy[1]);
    }

    /**
     * 是否可以旋转
     */
    @Override
    public boolean isCanRotate(IGraffiti graffiti, float x, float y) {
        float[] xy = getXy();
        // 把触摸点转换成在item坐标系（即以item起始点作为坐标原点）内的点
        x = x - xy[0];
        y = y - xy[1];
        // 把变换后矩形中的触摸点，还原回未变换前矩形中的点，然后判断是否矩形中
        float[] rectXy = rotatePoint((int) -(graffiti.getRotate() - mGraffitiDegree + mItemRotate), x, y, 0, 0);

        mRectTemp.set(mRect);
        float unit = graffiti.getSizeUnit();
        mRectTemp.left -= 10 * unit;
        mRectTemp.top -= 10 * unit;
        mRectTemp.right += 10 * unit;
        mRectTemp.bottom += 10 * unit;
        return rectXy[0] >= mRectTemp.right && rectXy[0] <= mRectTemp.right + ITEM_CAN_ROTATE_BOUND * graffiti.getSizeUnit()
                && rectXy[1] >= mRectTemp.top && rectXy[1] <= mRectTemp.bottom;
    }

    /**
     * 绘制选别时的背景
     *
     * @param canvas
     * @param graffiti
     */
    @Override
    public void drawSelectedBackground(IGraffiti graffiti, Canvas canvas) {
        mRectTemp.set(mRect);
        float unit = graffiti.getSizeUnit();
        mRectTemp.left -= 10 * unit;
        mRectTemp.top -= 10 * unit;
        mRectTemp.right += 10 * unit;
        mRectTemp.bottom += 10 * unit;
        paint.setShader(null);
        // Rect
            /*if (selectableItem.getColor().getType() == GraffitiColor.Type.COLOR) {
                mPaint.setColor(Color.argb(126,
                        255 - Color.red(selectableItem.getColor().getColor()),
                        255 - Color.green(selectableItem.getColor().getColor()),
                        255 - Color.blue(selectableItem.getColor().getColor())));
            } else {*/
        paint.setColor(0x88888888);
//            }
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
        canvas.drawRect(mRectTemp, paint);
        // border
        if (graffiti.isRotatingItem()) {
            paint.setColor(0x88ffd700);
        } else {
            paint.setColor(0x88888888);
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2 * unit);
        canvas.drawRect(mRectTemp, paint);
        // setRotate
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4 * unit);
        canvas.drawLine(mRectTemp.right, mRectTemp.top + mRectTemp.height() / 2,
                mRectTemp.right + (GraffitiSelectableItemBase.ITEM_CAN_ROTATE_BOUND - 16) * unit, mRectTemp.top + mRectTemp.height() / 2, paint);
        canvas.drawCircle(mRectTemp.right + (GraffitiSelectableItemBase.ITEM_CAN_ROTATE_BOUND - 8) * unit, mRectTemp.top + mRectTemp.height() / 2, 8 * unit, paint);

    }


    protected abstract void resetBounds(Rect rect);

    protected abstract void doDraw(IGraffiti graffiti, Canvas canvas);

    @Override
    public void draw(IGraffiti graffiti, Canvas canvas) {
        canvas.save();

        float[] xy = getXy(); // 获取旋转图片后文字的起始坐标
        canvas.translate(xy[0], xy[1]); // 把坐标系平移到文字矩形范围
        canvas.rotate(graffiti.getRotate() - getGraffitiRotate() + getItemRotate(), 0, 0); // 旋转坐标系

        // 在变换后的坐标系中画出selectableItem
        if (graffiti.getSelectedItem() == this) {
            drawSelectedBackground(graffiti, canvas);
        }
        doDraw(graffiti, canvas);

        canvas.restore();
    }
}
