package cn.hzw.graffiti;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;

import cn.hzw.graffiti.core.IGraffiti;

import static cn.hzw.graffiti.util.DrawUtil.rotatePoint;

/**
 * 可旋转的item
 */
public abstract class GraffitiRotatableItemBase extends GraffitiSelectableItemBase {

    private PointF mTemp = new PointF();
    private Rect mRectTemp = new Rect();
    private boolean mIsRotating = false;
    private Paint mPaint = new Paint();

    public GraffitiRotatableItemBase(IGraffiti graffiti, int itemRotate, float x, float y) {
        super(graffiti, itemRotate, x, y);
    }

    public GraffitiRotatableItemBase(IGraffiti graffiti, GraffitiPaintAttrs attrs, int itemRotate, float x, float y) {
        super(graffiti, attrs, itemRotate, x, y);
    }

    @Override
    public void doDrawSelectedBackground(Canvas canvas) {
        mRectTemp.set(getBounds());
        float unit = getGraffiti().getSizeUnit();
        mRectTemp.left -= 10 * unit;
        mRectTemp.top -= 10 * unit;
        mRectTemp.right += 10 * unit;
        mRectTemp.bottom += 10 * unit;
        mPaint.setShader(null);
        mPaint.setColor(0x33888888);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(1);
        canvas.drawRect(mRectTemp, mPaint);

        // border
        if (isRotating()) {
            mPaint.setColor(0x88ffd700);
        } else {
            mPaint.setColor(0x88ffffff);
        }
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2 * unit);
        canvas.drawRect(mRectTemp, mPaint);
        // border line
        mPaint.setColor(0x44888888);
        mPaint.setStrokeWidth(0.8f * unit);
        canvas.drawRect(mRectTemp, mPaint);

        // rotation
        if (isRotating()) {
            mPaint.setColor(0x88ffd700);
        } else {
            mPaint.setColor(0x88ffffff);
        }
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2 * unit);
        canvas.drawLine(mRectTemp.right, mRectTemp.top + mRectTemp.height() / 2,
                mRectTemp.right + (GraffitiSelectableItemBase.ITEM_CAN_ROTATE_BOUND - 16) * unit, mRectTemp.top + mRectTemp.height() / 2, mPaint);
        canvas.drawCircle(mRectTemp.right + (GraffitiSelectableItemBase.ITEM_CAN_ROTATE_BOUND - 8) * unit, mRectTemp.top + mRectTemp.height() / 2, 8 * unit, mPaint);
        // rotation line
        mPaint.setColor(0x44888888);
        mPaint.setStrokeWidth(0.8f * unit);
        canvas.drawLine(mRectTemp.right, mRectTemp.top + mRectTemp.height() / 2,
                mRectTemp.right + (GraffitiSelectableItemBase.ITEM_CAN_ROTATE_BOUND - 16) * unit, mRectTemp.top + mRectTemp.height() / 2, mPaint);
        canvas.drawCircle(mRectTemp.right + (GraffitiSelectableItemBase.ITEM_CAN_ROTATE_BOUND - 8) * unit, mRectTemp.top + mRectTemp.height() / 2, 8 * unit, mPaint);

    }

    /**
     * 是否可以旋转
     */
    public boolean canRotate(float x, float y) {
        IGraffiti graffiti = getGraffiti();
        PointF location = getLocation();
        // 把触摸点转换成在item坐标系（即以item起始点作为坐标原点）内的点
        x = x - location.x;
        y = y - location.y;
        // 把变换后矩形中的触摸点，还原回未变换前矩形中的点，然后判断是否矩形中
        PointF xy = rotatePoint(mTemp, (int) -getItemRotate(), x, y, 0, 0);

        mRectTemp.set(getBounds());
        float unit = graffiti.getSizeUnit();
        mRectTemp.left -= 13 * unit;
        mRectTemp.top -= 13 * unit;
        mRectTemp.right += 13 * unit;
        mRectTemp.bottom += 13 * unit;
        return xy.x >= mRectTemp.right && xy.x <= mRectTemp.right + ITEM_CAN_ROTATE_BOUND * graffiti.getSizeUnit()
                && xy.y >= mRectTemp.top && xy.y <= mRectTemp.bottom;
    }

    public boolean isRotating() {
        return mIsRotating;
    }

    public void setIsRotating(boolean isRotating) {
        mIsRotating = isRotating;
    }
}
