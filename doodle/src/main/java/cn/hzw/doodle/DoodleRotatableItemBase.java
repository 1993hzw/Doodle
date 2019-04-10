package cn.hzw.doodle;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;

import cn.hzw.doodle.core.IDoodle;
import cn.hzw.doodle.util.DrawUtil;

import static cn.hzw.doodle.util.DrawUtil.rotatePoint;

/**
 * 可旋转的item
 */
public abstract class DoodleRotatableItemBase extends DoodleSelectableItemBase {

    private PointF mTemp = new PointF();
    private Rect mRectTemp = new Rect();
    private boolean mIsRotating = false;
    private Paint mPaint = new Paint();

    public DoodleRotatableItemBase(IDoodle doodle, int itemRotate, float x, float y) {
        super(doodle, itemRotate, x, y);
    }

    public DoodleRotatableItemBase(IDoodle doodle, DoodlePaintAttrs attrs, int itemRotate, float x, float y) {
        super(doodle, attrs, itemRotate, x, y);
    }

    @Override
    public void doDrawAtTheTop(Canvas canvas) {
        if (isSelected()) {

            // 反向缩放画布，使视觉上选中边框不随图片缩放而变化
            int count = canvas.save();
            canvas.scale(1 / getDoodle().getDoodleScale(), 1 / getDoodle().getDoodleScale(), getPivotX() - getLocation().x, getPivotY() - getLocation().y);
            mRectTemp.set(getBounds());
            DrawUtil.scaleRect(mRectTemp, getDoodle().getDoodleScale(), getPivotX() - getLocation().x, getPivotY() - getLocation().y);

            float unit = getDoodle().getUnitSize();
            mRectTemp.left -= ITEM_PADDING * unit;
            mRectTemp.top -= ITEM_PADDING * unit;
            mRectTemp.right += ITEM_PADDING * unit;
            mRectTemp.bottom += ITEM_PADDING * unit;
            mPaint.setShader(null);
            mPaint.setColor(0x00888888);
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
                    mRectTemp.right + (DoodleSelectableItemBase.ITEM_CAN_ROTATE_BOUND - 16) * unit, mRectTemp.top + mRectTemp.height() / 2, mPaint);
            canvas.drawCircle(mRectTemp.right + (DoodleSelectableItemBase.ITEM_CAN_ROTATE_BOUND - 8) * unit, mRectTemp.top + mRectTemp.height() / 2, 8 * unit, mPaint);
            // rotation line
            mPaint.setColor(0x44888888);
            mPaint.setStrokeWidth(0.8f * unit);
            canvas.drawLine(mRectTemp.right, mRectTemp.top + mRectTemp.height() / 2,
                    mRectTemp.right + (DoodleSelectableItemBase.ITEM_CAN_ROTATE_BOUND - 16) * unit, mRectTemp.top + mRectTemp.height() / 2, mPaint);
            canvas.drawCircle(mRectTemp.right + (DoodleSelectableItemBase.ITEM_CAN_ROTATE_BOUND - 8) * unit, mRectTemp.top + mRectTemp.height() / 2, 8 * unit, mPaint);


            // pivot
            mPaint.setColor(0xffffffff);
            mPaint.setStrokeWidth(1f * unit);
            mPaint.setStyle(Paint.Style.STROKE);
            // +
            int length = 3;
            canvas.drawLine(getPivotX() - getLocation().x - length * unit, getPivotY() - getLocation().y, getPivotX() - getLocation().x + length * unit, getPivotY() - getLocation().y, mPaint);
            canvas.drawLine(getPivotX() - getLocation().x, getPivotY() - getLocation().y - length * unit, getPivotX() - getLocation().x, getPivotY() - getLocation().y + length * unit, mPaint);
            mPaint.setStrokeWidth(0.5f * unit);
            mPaint.setColor(0xff888888);
            canvas.drawLine(getPivotX() - getLocation().x - length * unit, getPivotY() - getLocation().y, getPivotX() - getLocation().x + length * unit, getPivotY() - getLocation().y, mPaint);
            canvas.drawLine(getPivotX() - getLocation().x, getPivotY() - getLocation().y - length * unit, getPivotX() - getLocation().x, getPivotY() - getLocation().y + length * unit, mPaint);
            mPaint.setStrokeWidth(1f * unit);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(0xffffffff);
            canvas.drawCircle(getPivotX() - getLocation().x, getPivotY() - getLocation().y, unit, mPaint);

            canvas.restoreToCount(count);
        }
    }


    /**
     * 是否可以旋转
     */
    public boolean canRotate(float x, float y) {
        IDoodle doodle = getDoodle();
        PointF location = getLocation();
        // 把触摸点转换成在item坐标系（即以item起始点作为坐标原点）内的点
        x = x - location.x;
        y = y - location.y;
        // 把变换后矩形中的触摸点，还原回未变换前矩形中的点，然后判断是否矩形中
        PointF xy = rotatePoint(mTemp, (int) -getItemRotate(), x, y, getPivotX() - getLocation().x, getPivotY() - getLocation().y);

        // 计算旋转把柄的位置，由于绘制时反向缩放了画布，所以这里也应算上相应的getDoodle().getDoodleScale()
        mRectTemp.set(getBounds());
        float padding = 13 * getDoodle().getUnitSize() / getDoodle().getDoodleScale();
        mRectTemp.top -= padding;
        mRectTemp.right += padding;
        mRectTemp.bottom += padding;
        return xy.x >= mRectTemp.right
                && xy.x <= mRectTemp.right + ITEM_CAN_ROTATE_BOUND * doodle.getUnitSize() / getDoodle().getDoodleScale()
                && xy.y >= mRectTemp.top
                && xy.y <= mRectTemp.bottom;
    }

    public boolean isRotating() {
        return mIsRotating;
    }

    public void setIsRotating(boolean isRotating) {
        mIsRotating = isRotating;
    }
}
