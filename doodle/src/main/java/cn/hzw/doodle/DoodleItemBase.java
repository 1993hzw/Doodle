package cn.hzw.doodle;

import android.graphics.Canvas;
import android.graphics.PointF;

import cn.hzw.doodle.core.IDoodle;
import cn.hzw.doodle.core.IDoodleColor;
import cn.hzw.doodle.core.IDoodleItem;
import cn.hzw.doodle.core.IDoodlePen;
import cn.hzw.doodle.core.IDoodleShape;

/**
 * Created on 29/06/2018.
 */

public abstract class DoodleItemBase implements IDoodleItem {

    public static final float MIN_SCALE = 0.01f;
    public static final float MAX_SCALE = 100f;

    private float mItemRotate; // item的旋转角度
    private IDoodle mDoodle;
    private PointF mLocation = new PointF();
    private IDoodlePen mPen; // 画笔类型
    private IDoodleShape mShape; // 画笔形状
    private float mSize; // 大小
    private IDoodleColor mColor; // 颜色
    private boolean mIsDrawOptimize = false; //优化绘制
    private boolean mIsNeedClipOutside = true; // 是否需要裁剪图片区域外的部分
    private float mPivotX, mPivotY;
    private float mMinScale = MIN_SCALE;
    private float mMaxScale = MAX_SCALE;
    private float mScale = 1;

    private boolean mHasAdded = false;

    public DoodleItemBase(IDoodle doodle) {
        this(doodle, null);
    }

    public DoodleItemBase(IDoodle doodle, DoodlePaintAttrs attrs) {
        setDoodle(doodle);
        if (attrs != null) {
            mPen = attrs.pen();
            mShape = attrs.shape();
            mSize = attrs.size();
            mColor = attrs.color();
        }
    }

    @Override
    public void setDoodle(IDoodle doodle) {
        if (doodle != null && mDoodle != null) { // 不能重复赋予非空值
            throw new RuntimeException("item's doodle object is not null");
        }
        mDoodle = doodle;
        if (doodle == null) {
        }
    }

    @Override
    public IDoodle getDoodle() {
        return mDoodle;
    }

    @Override
    public void setPivotX(float pivotX) {
        mPivotX = pivotX;
    }

    @Override
    public float getPivotX() {
        return mPivotX;
    }

    @Override
    public void setPivotY(float pivotY) {
        mPivotY = pivotY;
    }

    @Override
    public float getPivotY() {
        return mPivotY;
    }

    @Override
    public void setItemRotate(float textRotate) {
        mItemRotate = textRotate;
        refresh();
    }

    @Override
    public float getItemRotate() {
        return mItemRotate;
    }

    /**
     * 默认改变相应的中心点位置
     *
     * @param x
     * @param y
     */
    @Override
    public void setLocation(float x, float y) {
        setLocation(x, y, true);
    }

    /**
     * @param x
     * @param y
     * @param changePivot 是否随着移动相应改变中心点的位置
     */
    public void setLocation(float x, float y, boolean changePivot) {
        float diffX = x - mLocation.x, diffY = y - mLocation.y;
        mLocation.x = x;
        mLocation.y = y;

        if (changePivot) {
            mPivotX = mPivotX + diffX;
            mPivotY = mPivotY + diffY;
        }

        refresh();
    }

    @Override
    public PointF getLocation() {
        return mLocation;
    }

    @Override
    public IDoodlePen getPen() {
        return mPen;
    }

    @Override
    public void setPen(IDoodlePen pen) {
        mPen = pen;
        refresh();
    }

    @Override
    public IDoodleShape getShape() {
        return mShape;
    }

    @Override
    public void setShape(IDoodleShape shape) {
        mShape = shape;
        refresh();
    }

    @Override
    public float getSize() {
        return mSize;
    }

    @Override
    public void setSize(float size) {
        mSize = size;
        refresh();
    }

    @Override
    public IDoodleColor getColor() {
        return mColor;
    }

    @Override
    public void setColor(IDoodleColor color) {
        mColor = color;
        refresh();
    }

    @Override
    public void draw(Canvas canvas) {
        drawBefore(canvas);

        canvas.save();
        mLocation = getLocation(); // 获取旋转后的起始坐标
        canvas.translate(mLocation.x, mLocation.y); // 偏移，把坐标系平移到item矩形范围
        float px = mPivotX - mLocation.x, py = mPivotY - mLocation.y; // 需要减去偏移
        canvas.rotate(mItemRotate, px, py); // 旋转坐标系
        canvas.scale(mScale, mScale, px, py); // 缩放
        doDraw(canvas);
        canvas.restore();

        drawAfter(canvas);
    }

    /**
     * 是否优化绘制，若是则在添加item时提前会绘制到图片上，若否则在每次view绘制时绘制在View中，直到保存时才绘制到图片上
     *
     * @param drawOptimize
     */
    public void setDrawOptimize(boolean drawOptimize) {
        if (drawOptimize == mIsDrawOptimize) {
            return;
        }
        mIsDrawOptimize = drawOptimize;
    }

    @Override
    public boolean isNeedClipOutside() {
        return mIsNeedClipOutside;
    }

    @Override
    public void setNeedClipOutside(boolean clip) {
        mIsNeedClipOutside = clip;
    }

    @Override
    public void onAdd() {
        mHasAdded = true;
    }

    @Override
    public void onRemove() {
        mHasAdded = false;
    }

    @Override
    public void refresh() {
        if (mHasAdded && mDoodle != null) {
            mDoodle.refresh();
        }
    }

    @Override
    public boolean isDoodleEditable() {
        return false;
    }

    @Override
    public void setScale(float scale) {
        if (scale <= mMinScale) {
            scale = mMinScale;
        } else if (scale > mMaxScale) {
            scale = mMaxScale;
        }
        mScale = scale;
        refresh();
    }

    @Override
    public float getScale() {
        return mScale;
    }

    public void setMinScale(float minScale) {
        if (mMinScale <= 0) {
            minScale = MIN_SCALE;
        } else if (minScale > mMaxScale) {
            minScale = mMaxScale;
        }
        mMinScale = minScale;
        setScale(getScale());
    }

    public float getMinScale() {
        return mMinScale;
    }

    public void setMaxScale(float maxScale) {
        if (maxScale <= 0) {
            maxScale = MIN_SCALE;
        } else if (maxScale < mMinScale) {
            maxScale = mMinScale;
        }
        mMaxScale = maxScale;
        setScale(getScale());
    }

    public float getMaxScale() {
        return mMaxScale;
    }

    /**
     * 仅画在View上，在绘制涂鸦之前调用(相当于背景图，但是保存图片时不包含该部分)
     *
     * @param canvas 为View的Canvas
     */
    protected void drawBefore(Canvas canvas) {

    }

    /**
     * 绘制item，不限制Canvas
     *
     * @param canvas
     */
    protected abstract void doDraw(Canvas canvas);

    /**
     * 仅画在View上，在绘制涂鸦之后调用(相当于前景图，但是保存图片时不包含该部分)
     *
     * @param canvas 为View的Canvas
     */
    protected void drawAfter(Canvas canvas) {

    }

    /**
     * 画在所有item的上面
     *
     * @param canvas
     */
    @Override
    public void drawAtTheTop(Canvas canvas) {

    }

}
