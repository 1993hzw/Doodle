package cn.hzw.graffiti;

import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;
import cn.forward.androids.utils.LogUtil;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Administrator on 2016/9/3.
 */
public class GraffitiView extends View {

    Context context;
    Bitmap galleryBitmap;
    HandWrite.GraffitiListener mGraffitiListener;

    private float scale;
    private Bitmap originalBitmap;
    private Canvas myCanvas;

    private float n;
    private int height, width;// 包揽图片的框大小 并非为preview的大�?
    private float centreX, centreY;// 是图片居�?

    private Paint mBitmapPaint;
    private BitmapShader mBitmapShader;
    private float transX = 0, transY = 0;

    private int mode;
    private float radius;
    private int color;

    private Path mCurrPath; // 当前手写的路径

    private boolean mIsPainting = false;

    // 保存涂鸦操作，便于撤销
    private CopyOnWriteArrayList<GraffitiPath> mPathStack = new CopyOnWriteArrayList<GraffitiPath>();
    private CopyOnWriteArrayList<GraffitiPath> pathStackBackup = new CopyOnWriteArrayList<GraffitiPath>();

    /**
     * 画笔
     */
    public enum Pen {
        Freehand, // 手绘
        Copy, // 仿制
        Eraser // 橡皮擦
    }

    /**
     * 图形
     */
    public enum Shape {
        HandWrite, //
        Arrow, // 箭头
        Line, // 直线
        FillCircle, // 实心圆
        HollCircle, // 空心圆
        FillRect, // 实心矩形
        HollRecct, // 空心矩形

    }

    private Pen mPen;
    private Shape mShape;

    private float mTouchDownX, mTouchDownY, mLastTouchX, mLastTouchY, mTouchX, mTouchY;
    private Matrix mShaderMatrix;

    public GraffitiView(Context context, Bitmap bitmap, HandWrite.GraffitiListener listener) {
        super(context);
        this.context = context;
        galleryBitmap = bitmap;
        mGraffitiListener = listener;
        if (mGraffitiListener == null) {
            throw new RuntimeException("GraffitiListener is null!!!");
        }
        if (galleryBitmap == null) {
            throw new RuntimeException("Bitmap is null!!!");
        }
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setBG();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mode = 1;
                mTouchDownX = mTouchX = mLastTouchX = event.getX();
                mTouchDownY = mTouchY = mLastTouchY = event.getY();

                if (mPathStack.size() > 3) {// 当前栈大�?3，则拷贝到备份栈
                    pathStackBackup.addAll(mPathStack);
                    mPathStack.clear();
                }

                if (mShape == Shape.HandWrite) { // 手写
                    mCurrPath = new Path();
                    mCurrPath.moveTo(toX(mTouchDownX), toY(mTouchDownY));
                } else {  // 画图形

                }

                mIsPainting = true;
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mode = 0;
                mLastTouchX = mTouchX;
                mLastTouchY = mTouchY;
                mTouchX = event.getX();
                mTouchY = event.getY();
                // 把操作记录到加入的堆栈中
                if (mShape == Shape.HandWrite) { // 手写
                    mPathStack.add(GraffitiPath.toPath(mPen, radius, color, mCurrPath));
                } else {  // 画图形
                    mPathStack.add(GraffitiPath.toShape(mPen, mShape, radius, color,
                            toX(mTouchDownX), toY(mTouchDownY), toX(mTouchX), toY(mTouchY)));
                }

                mIsPainting = false;
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (mode < 2) {// 单点滑动
                    mLastTouchX = mTouchX;
                    mLastTouchY = mTouchY;
                    mTouchX = event.getX();
                    mTouchY = event.getY();
                    if (mShape == Shape.HandWrite) { // 手写
                        mCurrPath.quadTo(
                                toX((mTouchX + mLastTouchX) / 2),
                                toY((mTouchY + mLastTouchY) / 2),
                                toX(mTouchX),
                                toY(mTouchY));
                    } else {  // 画图形

                    }
                } else {// 多点

                }

                invalidate();
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                mode -= 1;

                invalidate();
                return true;
            case MotionEvent.ACTION_POINTER_DOWN:
                mode += 1;

                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    public void init() {
        try {
//            originalBitmap = Bitmap.createBitmap(galleryBitmap.getWidth(),
//                    galleryBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            originalBitmap = galleryBitmap.copy(Bitmap.Config.ARGB_8888, true);
        } catch (Error e) {
            LogUtil.d(e.getMessage());
            return;
        }
        scale = 0.7f;
        myCanvas = new Canvas(originalBitmap);

        radius = 60;
        color = Color.RED;
        mBitmapPaint = new Paint();
        mBitmapPaint.setStrokeWidth(radius);
        mBitmapPaint.setColor(color);
        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setStrokeJoin(Paint.Join.ROUND);
        mBitmapPaint.setStrokeCap(Paint.Cap.ROUND);// 圆滑

        mPen = Pen.Freehand;
        mShape = Shape.HandWrite;

//        this.mBitmapShader = new BitmapShader(this.galleryBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
//        mShaderMatrix = new Matrix();
    }

    private void setBG() {// 不用resize preview
        int w = galleryBitmap.getWidth();
        int h = galleryBitmap.getHeight();
        float nw = w * 1f / getWidth();
        float nh = h * 1f / getHeight();
        if (nw > nh) {
            n = 1 / nw;
            width = getWidth();
            height = (int) (h * n);
        } else {
            n = 1 / nh;
            width = (int) (w * n);
            height = getHeight();
        }
        // 使图片居中
        centreX = (getWidth() - width) / 2f;
        centreY = (getHeight() - height) / 2f;

       /* this.mShaderMatrix.set(null);
        this.mShaderMatrix.postTranslate((centreX + transX) / (n * scale), (centreY + transY) / (n * scale));
        this.mBitmapShader.setLocalMatrix(this.mShaderMatrix);*/

        myCanvas.translate(-(centreX + transX) / (n * scale), -(centreY + transY) / (n * scale));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (galleryBitmap.isRecycled() || originalBitmap.isRecycled()) {
            return;
        }

        // 还原堆栈中的记录的操作
        for (GraffitiPath path : mPathStack) {
            if (mShape == Shape.HandWrite) { // 手写
                mBitmapPaint.setStrokeWidth(path.mStrokeWidth);
                mBitmapPaint.setColor(path.mColor);
                draw(myCanvas, path.mPen, mBitmapPaint, path.mPath);
            } else { // 画图形
                mBitmapPaint.setStrokeWidth(path.mStrokeWidth);
                mBitmapPaint.setColor(path.mColor);
                draw(myCanvas, path.mPen, path.mShape, mBitmapPaint,
                        path.mSx, path.mSy, path.mDx, path.mDy);
            }
        }

        canvas.scale(n * scale, n * scale);
        canvas.drawBitmap(originalBitmap, (centreX + transX) / (n * scale), (centreY + transY) / (n * scale), null);

        if (mIsPainting) {
            // 画触摸的路径
            mBitmapPaint.setStrokeWidth(radius);
            mBitmapPaint.setColor(color);
            if (mShape == Shape.HandWrite) { // 手写
                draw(canvas, mPen, mBitmapPaint, mCurrPath);
            } else {  // 画图形
                draw(canvas, mPen, mShape, mBitmapPaint,
                        toX(mTouchDownX), toY(mTouchDownY), toX(mTouchX), toY(mTouchY));
            }
        }

    }

    private void resetPaint(Pen pen, Paint paint) {
        switch (pen) { // 设置画笔
            case Freehand:
                paint.setShader(null);
                break;
            case Copy:
                paint.setShader(this.mBitmapShader);
                break;
            case Eraser:
                paint.setShader(this.mBitmapShader);
                break;
        }
    }

    private void draw(Canvas canvas, Pen pen, Paint paint, Path path) {
        resetPaint(pen, paint);

        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint);

    }

    private void draw(Canvas canvas, Pen pen, Shape shape, Paint paint, float sx, float sy, float dx, float dy) {
        resetPaint(pen, paint);

        paint.setStyle(Paint.Style.STROKE);
        switch (shape) { // 绘制图形
            case Arrow:
                DrawUtil.drawArrow(
                        canvas, sx, sy, dx, dy, paint);
                break;
            case Line:
                DrawUtil.drawLine(
                        canvas, sx, sy, dx, dy, paint);
                break;
            case FillCircle:
                paint.setStyle(Paint.Style.FILL);
            case HollCircle:
                DrawUtil.drawCircle(canvas, sx, sy,
                        (float) Math.sqrt((sx - dx) * (sx - dx) + (sy - dy) * (sy - dy)), paint);
                break;
            case FillRect:
                paint.setStyle(Paint.Style.FILL);
            case HollRecct:
                DrawUtil.drawRect(
                        canvas, sx, sy, dx, dy, paint);
                break;
        }
    }


    /**
     * 将屏幕触摸坐标x转换成在图片中的坐标
     */
    private float toX(float x) {
        return (x - transX - centreX) / (n * scale);
    }

    /**
     * 将屏幕触摸坐标y转换成在图片中的坐标
     */
    private float toY(float y) {
        return (y - transY - centreY) / (n * scale);
    }

    private static class GraffitiPath {
        Pen mPen;
        Shape mShape;
        float mStrokeWidth;
        int mColor;
        Path mPath;
        float mSx, mSy, mDx, mDy;

        static GraffitiPath toShape(Pen pen, Shape shape, float width, int color,
                                    float sx, float sy, float dx, float dy) {
            GraffitiPath path = new GraffitiPath();
            path.mPen = pen;
            path.mShape = shape;
            path.mStrokeWidth = width;
            path.mColor = color;
            path.mSx = sx;
            path.mSy = sy;
            path.mDx = dx;
            path.mDy = dy;
            return path;
        }

        static GraffitiPath toPath(Pen pen, float width, int color, Path p) {
            GraffitiPath path = new GraffitiPath();
            path.mPen = pen;
            path.mStrokeWidth = width;
            path.mColor = color;
            path.mPath = p;
            return path;
        }
    }
}
