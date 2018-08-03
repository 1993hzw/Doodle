package cn.hzw.doodle;

import android.graphics.Canvas;
import android.graphics.PointF;

import cn.hzw.doodle.core.IDoodle;
import cn.hzw.doodle.core.IDoodleColor;
import cn.hzw.doodle.core.IDoodleItem;
import cn.hzw.doodle.core.IDoodlePen;
import cn.hzw.doodle.core.IDoodleShape;
import cn.hzw.doodle.util.DrawUtil;

/**
 * Created on 29/06/2018.
 */

public abstract class DoodleItemBase implements IDoodleItem {

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
            return;
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

    private PointF mPointF = new PointF();

    @Override
    public void setItemRotate(float textRotate) {
        mItemRotate = textRotate;
        refresh();
    }

    @Override
    public float getItemRotate() {
        return mItemRotate;
    }

    @Override
    public void setLocation(float x, float y) {
        mLocation.x = x;
        mLocation.y = y;
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
        canvas.save();
        mLocation = getLocation(); // 获取旋转后的起始坐标
        canvas.translate(mLocation.x, mLocation.y); // 把坐标系平移到文字矩形范围
        canvas.rotate(mItemRotate, 0, 0); // 旋转坐标系

        doDraw(canvas);

        canvas.restore();
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

    /**
     * 是否优化绘制，若是则在添加item时提前会绘制到图片上，若否则在每次view绘制时绘制在View中，直到保存时才绘制到图片上
     */
    public boolean isDrawOptimize() {
        return mIsDrawOptimize;
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

    /**
     * 仅画在View上，在绘制涂鸦图片之前调用(相当于背景图，但是保存图片时不包含该部分)
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
     * 仅画在View上，在绘制涂鸦图片之后调用(相当于前景图，但是保存图片时不包含该部分)
     *
     * @param canvas 为View的Canvas
     */
    protected void drawAfter(Canvas canvas) {

    }

}
