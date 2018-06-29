package cn.hzw.graffiti;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;

import static cn.hzw.graffiti.util.DrawUtil.rotatePoint;

/**
 * 可选择的涂鸦条目，例如文字、图片
 * Created by huangziwei on 2017/7/16.
 */

public abstract class GraffitiSelectableItemBase extends GraffitiItemBase implements IGraffitiSelectableItem {

    public final static int ITEM_CAN_ROTATE_BOUND = 80;

    private Rect mRect = new Rect();
    private Rect mRectTemp = new Rect();
    private Paint paint = new Paint();

    private PointF mLocationTemp = new PointF();

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
    public boolean isInIt(IGraffiti graffiti, float x, float y) {
        int currentRotate = graffiti.getRotate();
        mLocationTemp = getLocation();
        // 把触摸点转换成在文字坐标系（即以文字起始点作为坐标原点）内的点
        x = x - mLocationTemp.x;
        y = y - mLocationTemp.y;
        // 把变换后相对于矩形的触摸点，还原回未变换前的点，然后判断是否矩形中
        mLocationTemp = rotatePoint(mLocationTemp, (int) -(currentRotate - getGraffitiRotate() + getItemRotate()), x, y, 0, 0);
        mRectTemp.set(mRect);
        float unit = graffiti.getSizeUnit();
        mRectTemp.left -= 10 * unit;
        mRectTemp.top -= 10 * unit;
        mRectTemp.right += 10 * unit;
        mRectTemp.bottom += 10 * unit;
        return mRectTemp.contains((int) mLocationTemp.x, (int) mLocationTemp.y);
    }

    /**
     * 是否可以旋转
     */
    @Override
    public boolean isCanRotate(IGraffiti graffiti, float x, float y) {
        mLocationTemp = getLocation();
        // 把触摸点转换成在item坐标系（即以item起始点作为坐标原点）内的点
        x = x - mLocationTemp.x;
        y = y - mLocationTemp.y;
        // 把变换后矩形中的触摸点，还原回未变换前矩形中的点，然后判断是否矩形中
        PointF rectXy = rotatePoint(mLocationTemp, (int) -(graffiti.getRotate() - getGraffitiRotate() + getItemRotate()), x, y, 0, 0);

        mRectTemp.set(mRect);
        float unit = graffiti.getSizeUnit();
        mRectTemp.left -= 10 * unit;
        mRectTemp.top -= 10 * unit;
        mRectTemp.right += 10 * unit;
        mRectTemp.bottom += 10 * unit;
        return rectXy.x >= mRectTemp.right && rectXy.x <= mRectTemp.right + ITEM_CAN_ROTATE_BOUND * graffiti.getSizeUnit()
                && rectXy.y >= mRectTemp.top && rectXy.y <= mRectTemp.bottom;
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

}
