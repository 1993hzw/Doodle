package cn.hzw.graffiti;

import android.graphics.Canvas;
import android.graphics.PointF;

import cn.hzw.graffiti.core.IGraffiti;
import cn.hzw.graffiti.core.IGraffitiColor;
import cn.hzw.graffiti.core.IGraffitiItem;
import cn.hzw.graffiti.core.IGraffitiPen;
import cn.hzw.graffiti.core.IGraffitiShape;

/**
 * Created on 29/06/2018.
 */

public abstract class GraffitiItemBase implements IGraffitiItem {

    private float mItemRotate; // item的旋转角度

    private IGraffiti mGraffiti;

    private PointF mLocation = new PointF();
    private PointF mTemp = new PointF();

    private IGraffitiPen mPen; // 画笔类型
    private IGraffitiShape mShape; // 画笔形状
    private float mSize; // 大小
    private IGraffitiColor mColor; // 颜色
    private boolean mIsDrawOptimize = false; //优化绘制
    private boolean mIsNeedClipOutside = true; // 是否需要裁剪图片区域外的部分

    public GraffitiItemBase(IGraffiti graffiti) {
        this(graffiti, null);
    }

    public GraffitiItemBase(IGraffiti graffiti, GraffitiPaintAttrs attrs) {
        setGraffiti(graffiti);
        if (attrs != null) {
            mPen = attrs.pen();
            mShape = attrs.shape();
            mSize = attrs.size();
            mColor = attrs.color();
        }
    }

    @Override
    public void setGraffiti(IGraffiti graffiti) {
        if (graffiti != null && mGraffiti != null) { // 不能重复赋予非空值
            throw new RuntimeException("item's graffiti object is not null");
        }
        mGraffiti = graffiti;
        if (graffiti == null) {
            return;
        }
    }

    @Override
    public IGraffiti getGraffiti() {
        return mGraffiti;
    }

    @Override
    public void setItemRotate(float textRotate) {
        mItemRotate = textRotate;
    }

    @Override
    public float getItemRotate() {
        return mItemRotate;
    }

    @Override
    public void setLocation(float x, float y) {
        mLocation.x = x;
        mLocation.y = y;
    }

    @Override
    public PointF getLocation() {
        return mLocation;
    }

    @Override
    public IGraffitiPen getPen() {
        return mPen;
    }
    @Override
    public void setPen(IGraffitiPen pen) {
        mPen = pen;
    }
    @Override
    public IGraffitiShape getShape() {
        return mShape;
    }
    @Override
    public void setShape(IGraffitiShape shape) {
        mShape = shape;
    }
    @Override
    public float getSize() {
        return mSize;
    }
    @Override
    public void setSize(float size) {
        mSize = size;
    }
    @Override
    public IGraffitiColor getColor() {
        return mColor;
    }
    @Override
    public void setColor(IGraffitiColor color) {
        mColor = color;
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

    }

    @Override
    public void onRemove() {

    }

    /**
     * 仅画在View上，在绘制涂鸦图片之前调用(相当于背景图，但是保存图片时不包含该部分)
     * @param canvas 为View的Canvas
     */
    protected void drawBefore(Canvas canvas){

    }
    /**
     * 绘制item，不限制Canvas
     * @param canvas
     */
    protected abstract void doDraw(Canvas canvas);
    /**
     * 仅画在View上，在绘制涂鸦图片之后调用(相当于前景图，但是保存图片时不包含该部分)
     * @param canvas 为View的Canvas
     */
    protected  void drawAfter(Canvas canvas){

    }

}
