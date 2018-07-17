package cn.hzw.graffiti;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;

import cn.hzw.graffiti.core.IGraffiti;
import cn.hzw.graffiti.core.IGraffitiSelectableItem;

import static cn.hzw.graffiti.util.DrawUtil.rotatePoint;

/**
 * 可选择的涂鸦item，例如文字、图片
 * Created by huangziwei on 2017/7/16.
 */

public abstract class GraffitiSelectableItemBase extends GraffitiItemBase implements IGraffitiSelectableItem {

    public final static int ITEM_CAN_ROTATE_BOUND = 70;

    private Rect mRect = new Rect();
    private Rect mRectTemp = new Rect();
    private Paint paint = new Paint();

    private PointF mTemp = new PointF();
    private boolean mIsRotating = false;
    private boolean mIsSelected = false;

    public GraffitiSelectableItemBase(IGraffiti graffiti, int itemRotate, float x, float y) {
        this(graffiti, null, itemRotate, x, y);
    }

    public GraffitiSelectableItemBase(IGraffiti graffiti, GraffitiPaintAttrs attrs, int itemRotate, float x, float y) {
        super(graffiti, attrs);
        setLocation(x, y);
        setItemRotate(itemRotate);

        resetBounds(mRect);
    }


    protected Rect getBounds() {
        return mRect;
    }


    @Override
    public void setSize(float size) {
        super.setSize(size);
        resetBounds(mRect);
    }

    /**
     * 是否击中
     */
    @Override
    public boolean isInIt(float x, float y) {
        PointF location = getLocation();
        // 把触摸点转换成在文字坐标系（即以文字起始点作为坐标原点）内的点
        x = x - location.x;
        y = y - location.y;
        // 把变换后相对于矩形的触摸点，还原回未变换前的点，然后判断是否矩形中
        mTemp = rotatePoint(mTemp, (int) -getItemRotate(), x, y, 0, 0);
        mRectTemp.set(mRect);
        float unit = getGraffiti().getSizeUnit();
        mRectTemp.left -= 10 * unit;
        mRectTemp.top -= 10 * unit;
        mRectTemp.right += 10 * unit;
        mRectTemp.bottom += 10 * unit;
        return mRectTemp.contains((int) mTemp.x, (int) mTemp.y);
    }

    /**
     * 是否可以旋转
     */
    @Override
    public boolean isCanRotate(float x, float y) {
        IGraffiti graffiti = getGraffiti();
        PointF location = getLocation();
        // 把触摸点转换成在item坐标系（即以item起始点作为坐标原点）内的点
        x = x - location.x;
        y = y - location.y;
        // 把变换后矩形中的触摸点，还原回未变换前矩形中的点，然后判断是否矩形中
        PointF xy = rotatePoint(mTemp, (int) -getItemRotate(), x, y, 0, 0);

        mRectTemp.set(mRect);
        float unit = graffiti.getSizeUnit();
        mRectTemp.left -= 13 * unit;
        mRectTemp.top -= 13 * unit;
        mRectTemp.right += 13 * unit;
        mRectTemp.bottom += 13 * unit;
        return xy.x >= mRectTemp.right && xy.x <= mRectTemp.right + ITEM_CAN_ROTATE_BOUND * graffiti.getSizeUnit()
                && xy.y >= mRectTemp.top && xy.y <= mRectTemp.bottom;
    }

    @Override
    public boolean isRotating() {
        return mIsRotating;
    }

    @Override
    public void setIsRotating(boolean isRotating) {
        mIsRotating = isRotating;
    }

    @Override
    protected void drawBefore(Canvas canvas) {
        if (isSelected()) {
            drawSelectedBackground(canvas);
        }
    }

    /**
     * 绘制选别时的背景
     *
     * @param canvas
     */
    @Override
    public void drawSelectedBackground(Canvas canvas) {
        canvas.save();
        PointF location = getLocation(); // 获取旋转后的起始坐标
        canvas.translate(location.x, location.y); // 把坐标系平移到文字矩形范围
        canvas.rotate(getItemRotate(), 0, 0); // 旋转坐标系

        doDrawSelectedBackground(canvas);

        canvas.restore();
    }

    public void doDrawSelectedBackground(Canvas canvas) {
        mRectTemp.set(mRect);
        float unit = getGraffiti().getSizeUnit();
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
        if (isRotating()) {
            paint.setColor(0x88ffd700);
        } else {
            paint.setColor(0x88888888);
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2 * unit);
        canvas.drawRect(mRectTemp, paint);
        // setGraffitiRotation
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2 * unit);
        canvas.drawLine(mRectTemp.right, mRectTemp.top + mRectTemp.height() / 2,
                mRectTemp.right + (GraffitiSelectableItemBase.ITEM_CAN_ROTATE_BOUND - 16) * unit, mRectTemp.top + mRectTemp.height() / 2, paint);
        canvas.drawCircle(mRectTemp.right + (GraffitiSelectableItemBase.ITEM_CAN_ROTATE_BOUND - 8) * unit, mRectTemp.top + mRectTemp.height() / 2, 8 * unit, paint);
    }

    @Override
    public boolean isSelected() {
        return mIsSelected;
    }

    @Override
    public void setSelected(boolean isSelected) {
        mIsSelected = isSelected;
        setNeedClipOutside(!isSelected);
        if (getGraffiti() != null) {
            getGraffiti().invalidate();
        }
    }

    protected abstract void resetBounds(Rect rect);

}
