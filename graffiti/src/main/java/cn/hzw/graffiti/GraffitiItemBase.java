package cn.hzw.graffiti;

import android.graphics.Canvas;
import android.graphics.PointF;

import static cn.hzw.graffiti.util.DrawUtil.restoreRotatePointInGraffiti;
import static cn.hzw.graffiti.util.DrawUtil.rotatePointInGraffiti;

/**
 * Created on 29/06/2018.
 */

public abstract class GraffitiItemBase implements IGraffitiItem {

    private float mItemRotate; // item的旋转角度
    private int mGraffitiRotate; // 涂鸦图片的旋转角度
    private float mOriginalX, mOriginalY; // 在原图中的起始位置

    private IGraffiti mGraffiti;
    private float mOriginalPivotX, mOriginalPivotY; // // 原图的中心位置

    private PointF mLocation = new PointF();
    private PointF mTemp = new PointF();

    private GraffitiView.Pen mPen; // 画笔类型
    private GraffitiView.Shape mShape; // 画笔形状
    private float mSize; // 大小
    private GraffitiColor mColor; // 颜色
    private boolean mIsDrawOptimize = false; //优化绘制

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
        mGraffitiRotate = graffiti.getRotate();
        int bitmapWidth = graffiti.getBitmap().getWidth();
        int bitmapHeight = graffiti.getBitmap().getHeight();
        int degree = graffiti.getRotate();
        if (Math.abs(degree) == 90 || Math.abs(degree) == 270) { // 获取原始图片的宽高
            int t = bitmapWidth;
            bitmapWidth = bitmapHeight;
            bitmapHeight = t;
        }
        mOriginalPivotX = bitmapWidth / 2;
        mOriginalPivotY = bitmapHeight / 2;
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
        // 转换成未旋转前的坐标
        mTemp = restoreRotatePointInGraffiti(mTemp, mGraffiti.getRotate(), mGraffitiRotate, x, y, mOriginalPivotX, mOriginalPivotY);
        mOriginalX = mTemp.x;
        mOriginalY = mTemp.y;

        // 使用下面的代码 旋转后移动异常
//        mOriginalX = x;
//        mOriginalY = y;
    }

    @Override
    public PointF getLocation() {
        return rotatePointInGraffiti(mLocation, mGraffiti.getRotate(), mGraffitiRotate, mOriginalX, mOriginalY, mOriginalPivotX, mOriginalPivotY);
    }

    public float getOriginalPivotX() {
        return mOriginalPivotX;
    }

    public float getOriginalPivotY() {
        return mOriginalPivotY;
    }

    public int getGraffitiRotate() {
        return mGraffitiRotate;
    }

    public GraffitiView.Pen getPen() {
        return mPen;
    }

    public void setPen(GraffitiView.Pen pen) {
        mPen = pen;
    }

    public GraffitiView.Shape getShape() {
        return mShape;
    }

    public void setShape(GraffitiView.Shape shape) {
        mShape = shape;
    }

    public float getSize() {
        return mSize;
    }

    public void setSize(float size) {
        mSize = size;
    }

    public GraffitiColor getColor() {
        return mColor;
    }

    public void setColor(GraffitiColor color) {
        mColor = color;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();
        mLocation = getLocation(); // 获取旋转后的起始坐标
        canvas.translate(mLocation.x, mLocation.y); // 把坐标系平移到文字矩形范围
        canvas.rotate(mGraffiti.getRotate() - mGraffitiRotate + mItemRotate, 0, 0); // 旋转坐标系

        doDraw(canvas);

        canvas.restore();

    }

    protected abstract void doDraw(Canvas canvas);

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
}
