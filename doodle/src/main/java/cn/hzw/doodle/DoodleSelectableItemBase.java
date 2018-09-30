package cn.hzw.doodle;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;

import cn.hzw.doodle.core.IDoodle;
import cn.hzw.doodle.core.IDoodleSelectableItem;

import static cn.hzw.doodle.util.DrawUtil.rotatePoint;

/**
 * 可选择的涂鸦item，例如文字、图片
 * Created by huangziwei on 2017/7/16.
 */

public abstract class DoodleSelectableItemBase extends DoodleItemBase implements IDoodleSelectableItem {

    public final static int ITEM_CAN_ROTATE_BOUND = 35;
    public final static int ITEM_PADDING = 3; // 绘制item矩形区域时增加的padding

    private Rect mRect = new Rect();
    private Rect mRectTemp = new Rect();
    private Paint mPaint = new Paint();

    private PointF mTemp = new PointF();
    private boolean mIsSelected = false;

    public DoodleSelectableItemBase(IDoodle doodle, int itemRotate, float x, float y) {
        this(doodle, null, itemRotate, x, y);
    }

    public DoodleSelectableItemBase(IDoodle doodle, DoodlePaintAttrs attrs, int itemRotate, float x, float y) {
        super(doodle, attrs);
        setLocation(x, y);
        setItemRotate(itemRotate);

        resetBounds(mRect);
    }

    @Override
    public Rect getBounds() {
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
    public boolean contains(float x, float y) {
        resetBounds(mRect);
        PointF location = getLocation();
        // 把触摸点转换成在文字坐标系（即以文字起始点作为坐标原点）内的点
        x = x - location.x;
        y = y - location.y;
        // 把变换后相对于矩形的触摸点，还原回未变换前的点，然后判断是否矩形中
        mTemp = rotatePoint(mTemp, (int) -getItemRotate(), x, y, getPivotX() - getLocation().x, getPivotY() - getLocation().y);
        mRectTemp.set(mRect);
        float unit = getDoodle().getUnitSize();
        mRectTemp.left -= ITEM_PADDING * unit;
        mRectTemp.top -= ITEM_PADDING * unit;
        mRectTemp.right += ITEM_PADDING * unit;
        mRectTemp.bottom += ITEM_PADDING * unit;
        return mRectTemp.contains((int) mTemp.x, (int) mTemp.y);
    }

    @Override
    public void drawBefore(Canvas canvas) {

    }

    @Override
    public void drawAfter(Canvas canvas) {

    }

    @Override
    public void drawAtTheTop(Canvas canvas) {
        canvas.save();
        PointF location = getLocation(); // 获取旋转后的起始坐标
        canvas.translate(location.x, location.y); // 把坐标系平移到item矩形范围
        canvas.rotate(getItemRotate(), getPivotX() - getLocation().x, getPivotY() - getLocation().y); // 旋转坐标系

        doDrawAtTheTop(canvas);

        canvas.restore();
    }

    public void doDrawAtTheTop(Canvas canvas) {
        if (isSelected()) { // 选中时的效果，在最上面，避免被其他内容遮住
            mRectTemp.set(getBounds());
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
            mPaint.setColor(0x88ffffff);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(2 * unit);
            canvas.drawRect(mRectTemp, mPaint);
            // border line
            mPaint.setColor(0x44888888);
            mPaint.setStrokeWidth(0.8f * unit);
            canvas.drawRect(mRectTemp, mPaint);
        }
    }

    @Override
    public boolean isSelected() {
        return mIsSelected;
    }

    @Override
    public void setSelected(boolean isSelected) {
        mIsSelected = isSelected;
        setNeedClipOutside(!isSelected);
        refresh();
    }

    protected abstract void resetBounds(Rect rect);

    @Override
    public boolean isDoodleEditable() {
        return true;
    }
}
