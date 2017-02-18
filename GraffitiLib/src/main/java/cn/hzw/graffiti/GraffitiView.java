package cn.hzw.graffiti;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;

import java.util.concurrent.CopyOnWriteArrayList;

import cn.forward.androids.utils.ImageUtils;
import cn.forward.androids.utils.Util;

/**
 * Created by huangziwei on 2016/9/3.
 */
public class GraffitiView extends View {

    public static final int ERROR_INIT = -1;
    public static final int ERROR_SAVE = -2;

    private static final float VALUE = 1f;
    private final int TIME_SPAN = 80;

    private GraffitiListener mGraffitiListener;

    private Bitmap mBitmap; // 当前涂鸦的原图（旋转后）
    private Bitmap mBitmapEraser; // 橡皮擦底图
    private Bitmap mGraffitiBitmap; // 用绘制涂鸦的图片
    private Canvas mBitmapCanvas;

    private int mOriginalWidth, mOriginalHeight; // 初始图片的尺寸
    private float mOriginalPivotX, mOriginalPivotY; // 图片选择中心

    private float mPrivateScale; // 图片适应屏幕（mScale=1）时的缩放倍数
    private int mPrivateHeight, mPrivateWidth;// 图片在缩放mPrivateScale倍数的情况下，适应屏幕（mScale=1）时的大小（肉眼看到的在屏幕上的大小）
    private float mCentreTranX, mCentreTranY;// 图片在缩放mPrivateScale倍数的情况下，居中（mScale=1）时的偏移（肉眼看到的在屏幕上的偏移）

    private BitmapShader mBitmapShader; // 主要用于盖章和橡皮擦（未设置底图）
    private BitmapShader mBitmapShaderEraser; // 橡皮擦底图，当未设置橡皮擦底图时，mBitmapShaderEraser = mBitmapShader
    private Path mCurrPath; // 当前手写的路径
    private Path mTempPath;
    private CopyLocation mCopyLocation; // 仿制的定位器

    private Paint mPaint;
    private int mTouchMode; // 触摸模式，用于判断单点或多点触摸
    private float mPaintSize;
    private GraffitiColor mColor; // 画笔底色
    private float mScale; // 图片在相对于居中时的缩放倍数 （ 图片真实的缩放倍数为 mPrivateScale*mScale ）

    private float mTransX = 0, mTransY = 0; // 图片在相对于居中时且在缩放mScale倍数的情况下的偏移量 （ 图片真实偏移量为　(mCentreTranX + mTransX)/mPrivateScale*mScale ）

/*
      明白下面一点，对于理解涂鸦坐标系很重要：
      假设不考虑任何缩放，图片就是肉眼看到的那么大，此时图片的大小width =  mPrivateWidth * mScale ,
      偏移量x = mCentreTranX + mTransX，而view的大小为width = getWidth()。height和偏移量y以此类推。
*/

    private boolean mIsPainting = false; // 是否正在绘制
    private boolean isJustDrawOriginal; // 是否只绘制原图

    private boolean mIsDrawableOutside = false; // 触摸时，图片区域外是否绘制涂鸦轨迹
    private boolean mEraserImageIsResizeable;
    private boolean mReady = false;


    // 保存涂鸦操作，便于撤销
    private CopyOnWriteArrayList<GraffitiPath> mPathStack = new CopyOnWriteArrayList<GraffitiPath>();

    /**
     * 画笔
     */
    public enum Pen {
        HAND, // 手绘
        COPY, // 仿制
        ERASER // 橡皮擦
    }

    /**
     * 图形
     */
    public enum Shape {
        HAND_WRITE, //
        ARROW, // 箭头
        LINE, // 直线
        FILL_CIRCLE, // 实心圆
        HOLLOW_CIRCLE, // 空心圆
        FILL_RECT, // 实心矩形
        HOLLOW_RECT, // 空心矩形
    }

    private Pen mPen;
    private Shape mShape;

    private float mTouchDownX, mTouchDownY, mLastTouchX, mLastTouchY, mTouchX, mTouchY;
    private Matrix mShaderMatrix; // 仿制图片的变化矩阵
    private Matrix mShaderMatrixEraser; // 橡皮擦底图的变化矩阵
    private Matrix mShaderMatrixColor; // 画笔图片底图的变化矩阵

    private float mAmplifierRadius;
    private Path mAmplifierPath;
    private float mAmplifierScale = 0; // 放大镜的倍数
    private Paint mAmplifierPaint;
    private int mAmplifierHorizonX; // 放大器的位置的x坐标，使其水平居中

    public GraffitiView(Context context, Bitmap bitmap, GraffitiListener listener) {
        this(context, bitmap, null, true, listener);
    }

    /**
     * @param context
     * @param bitmap
     * @param eraser                  橡皮擦的底图，如果涂鸦保存后再次涂鸦，传入涂鸦前的底图，则可以实现擦除涂鸦的效果．
     * @param eraserImageIsResizeable 橡皮擦底图是否调整大小，如果可以则调整到跟当前涂鸦图片一样的大小．
     * @param listener
     * @
     */
    public GraffitiView(Context context, Bitmap bitmap, String eraser, boolean eraserImageIsResizeable, GraffitiListener listener) {
        super(context);

       /* //[11,18)对硬件加速支持不完整，clipPath时会crash
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }*/

        // 关闭硬件加速，因为bitmap的Canvas不支持硬件加速
        if (Build.VERSION.SDK_INT >= 11) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }

        mBitmap = bitmap;
        mGraffitiListener = listener;
        if (mGraffitiListener == null) {
            throw new RuntimeException("GraffitiListener is null!!!");
        }
        if (mBitmap == null) {
            throw new RuntimeException("Bitmap is null!!!");
        }

        if (eraser != null) {
            mBitmapEraser = ImageUtils.createBitmapFromPath(eraser, getContext());
        }
        mEraserImageIsResizeable = eraserImageIsResizeable;

        mOriginalWidth = mBitmap.getWidth();
        mOriginalHeight = mBitmap.getHeight();
        mOriginalPivotX = mOriginalWidth / 2f;
        mOriginalPivotY = mOriginalHeight / 2f;


        init();

    }

    public void init() {

        mScale = 1f;
        mPaintSize = 30;
        mColor = new GraffitiColor(Color.RED);
        mPaint = new Paint();
        mPaint.setStrokeWidth(mPaintSize);
        mPaint.setColor(mColor.mColor);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);// 圆滑

        mPen = Pen.HAND;
        mShape = Shape.HAND_WRITE;


        mShaderMatrix = new Matrix();
        mShaderMatrixEraser = new Matrix();
        mTempPath = new Path();
        mCopyLocation = new CopyLocation(150, 150);

        mShaderMatrixColor = new Matrix();

        mAmplifierPaint = new Paint();
        mAmplifierPaint.setColor(0xaaffffff);
        mAmplifierPaint.setStyle(Paint.Style.STROKE);
        mAmplifierPaint.setAntiAlias(true);
        mAmplifierPaint.setStrokeJoin(Paint.Join.ROUND);
        mAmplifierPaint.setStrokeCap(Paint.Cap.ROUND);// 圆滑
        mAmplifierPaint.setStrokeWidth(Util.dp2px(getContext(), 10));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setBG();
        mCopyLocation.updateLocation(toX(w / 2), toY(h / 2));
        if (!mReady) {
            mGraffitiListener.onReady();
            mReady = true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mTouchMode = 1;
                mTouchDownX = mTouchX = mLastTouchX = event.getX();
                mTouchDownY = mTouchY = mLastTouchY = event.getY();

                if (mPen == Pen.COPY && mCopyLocation.isInIt(toX(mTouchX), toY(mTouchY))) { // 点击copy
                    mCopyLocation.isRelocating = true;
                    mCopyLocation.isCopying = false;
                } else {
                    if (mPen == Pen.COPY) {
                        if (!mCopyLocation.isCopying) {
                            mCopyLocation.setStartPosition(toX(mTouchX), toY(mTouchY));
                            resetMatrix();
                        }
                        mCopyLocation.isCopying = true;
                    }
                    mCopyLocation.isRelocating = false;
                    mCurrPath = new Path();
                    mCurrPath.moveTo(toX(mTouchDownX), toY(mTouchDownY));
                    if (mShape == Shape.HAND_WRITE) { // 手写

                    } else {  // 画图形

                    }
                    mIsPainting = true;
                }
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mTouchMode = 0;
                mLastTouchX = mTouchX;
                mLastTouchY = mTouchY;
                mTouchX = event.getX();
                mTouchY = event.getY();

                // 为了仅点击时也能出现绘图，必须移动path
                if (mTouchDownX == mTouchX && mTouchDownY == mTouchY & mTouchDownX == mLastTouchX && mTouchDownY == mLastTouchY) {
                    mTouchX += VALUE;
                    mTouchY += VALUE;
                }

                if (mCopyLocation.isRelocating) { // 正在定位location
                    mCopyLocation.updateLocation(toX(mTouchX), toY(mTouchY));
                    mCopyLocation.isRelocating = false;
                } else {
                    if (mIsPainting) {

                        if (mPen == Pen.COPY) {
                            mCopyLocation.updateLocation(mCopyLocation.mCopyStartX + toX(mTouchX) - mCopyLocation.mTouchStartX,
                                    mCopyLocation.mCopyStartY + toY(mTouchY) - mCopyLocation.mTouchStartY);
                        }

                        GraffitiPath path = null;

                        // 把操作记录到加入的堆栈中
                        if (mShape == Shape.HAND_WRITE) { // 手写
                            mCurrPath.quadTo(
                                    toX(mLastTouchX),
                                    toY(mLastTouchY),
                                    toX((mTouchX + mLastTouchX) / 2),
                                    toY((mTouchY + mLastTouchY) / 2));
                            path = GraffitiPath.toPath(mPen, mShape, mPaintSize, mColor.copy(), mCurrPath, mRotateDegree, mOriginalPivotX, mOriginalPivotY,
                                    getCopyLocation());
                        } else {  // 画图形
                            path = GraffitiPath.toShape(mPen, mShape, mPaintSize, mColor.copy(),
                                    toX(mTouchDownX), toY(mTouchDownY), toX(mTouchX), toY(mTouchY), mRotateDegree, mOriginalPivotX, mOriginalPivotY,
                                    getCopyLocation());
                        }
                        mPathStack.add(path);
                        draw(mBitmapCanvas, path); // 保存到图片中
                        mIsPainting = false;
                    }
                }

                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (mTouchMode < 2) { // 单点滑动
                    mLastTouchX = mTouchX;
                    mLastTouchY = mTouchY;
                    mTouchX = event.getX();
                    mTouchY = event.getY();

                    if (mCopyLocation.isRelocating) { // 正在定位location
                        mCopyLocation.updateLocation(toX(mTouchX), toY(mTouchY));
                    } else {
                        if (mPen == Pen.COPY) {
                            mCopyLocation.updateLocation(mCopyLocation.mCopyStartX + toX(mTouchX) - mCopyLocation.mTouchStartX,
                                    mCopyLocation.mCopyStartY + toY(mTouchY) - mCopyLocation.mTouchStartY);
                        }
                        if (mShape == Shape.HAND_WRITE) { // 手写
                            mCurrPath.quadTo(
                                    toX(mLastTouchX),
                                    toY(mLastTouchY),
                                    toX((mTouchX + mLastTouchX) / 2),
                                    toY((mTouchY + mLastTouchY) / 2));
                        } else { // 画图形

                        }
                    }
                } else { // 多点

                }

                invalidate();
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                mTouchMode -= 1;

                invalidate();
                return true;
            case MotionEvent.ACTION_POINTER_DOWN:
                mTouchMode += 1;

                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    private int mRotateDegree = 0; // 相对于初始图片旋转的角度

    public int getRotateDegree() {
        return mRotateDegree;
    }

    /**
     * 相对于初始图片旋转的角度
     *
     * @param degree
     */
    public void rotate(int degree) {
        degree = degree % 360;
        int absDegree = Math.abs(degree);
        if (absDegree > 0 && absDegree < 90) {
            degree = degree / absDegree * 90;
        } else if (absDegree > 90 && absDegree < 180) {
            degree = degree / absDegree * 180;
        } else if (absDegree > 180 && absDegree < 270) {
            degree = degree / absDegree * 2700;
        } else if (absDegree > 270 && absDegree < 360) {
            degree = 0;
        }

        if (degree == mRotateDegree) {
            return;
        }
        int r = degree - mRotateDegree;
        int originalDegree = mRotateDegree;
        mRotateDegree = degree;

        mCopyLocation.rotatePosition(originalDegree);

        mBitmap = ImageUtils.rotate(getContext(), mBitmap, r, true);
        if (mBitmapEraser != null) {
            mBitmapEraser = ImageUtils.rotate(getContext(), mBitmapEraser, r, true);
        }
        setBG();

        if (mPathStack.size() > 0) {
            draw(mBitmapCanvas, mPathStack);
        }
        invalidate();

    }

    private void setBG() {// 不用resize preview
        this.mBitmapShader = new BitmapShader(this.mBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

        if (mBitmapEraser != null) {
            this.mBitmapShaderEraser = new BitmapShader(this.mBitmapEraser, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        } else {
            this.mBitmapShaderEraser = mBitmapShader;
        }

        int w = mBitmap.getWidth();
        int h = mBitmap.getHeight();
        float nw = w * 1f / getWidth();
        float nh = h * 1f / getHeight();
        if (nw > nh) {
            mPrivateScale = 1 / nw;
            mPrivateWidth = getWidth();
            mPrivateHeight = (int) (h * mPrivateScale);
        } else {
            mPrivateScale = 1 / nh;
            mPrivateWidth = (int) (w * mPrivateScale);
            mPrivateHeight = getHeight();
        }
        // 使图片居中
        mCentreTranX = (getWidth() - mPrivateWidth) / 2f;
        mCentreTranY = (getHeight() - mPrivateHeight) / 2f;

        initCanvas();
        resetMatrix();

        mAmplifierRadius = Math.min(getWidth(), getHeight()) / 4;
        mAmplifierPath = new Path();
        mAmplifierPath.addCircle(mAmplifierRadius, mAmplifierRadius, mAmplifierRadius, Path.Direction.CCW);
        mAmplifierHorizonX = (int) (Math.min(getWidth(), getHeight()) / 2 - mAmplifierRadius);

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap.isRecycled() || mGraffitiBitmap.isRecycled()) {
            return;
        }

        canvas.save();
        doDraw(canvas);
        canvas.restore();

        if (mAmplifierScale > 0) { //启用放大镜
            canvas.save();

            if (mTouchY <= mAmplifierRadius * 2) { // 在放大镜的范围内， 把放大镜仿制底部
                canvas.translate(mAmplifierHorizonX, getHeight() - mAmplifierRadius * 2);
            } else {
                canvas.translate(mAmplifierHorizonX, 0);
            }
            canvas.clipPath(mAmplifierPath);
            canvas.drawColor(0xff000000);

            canvas.save();
            float scale = mAmplifierScale / mScale; // 除以mScale，无论当前图片缩放多少，都产生图片在居中状态下缩放mAmplifierScale倍的效果
            canvas.scale(scale, scale);
            canvas.translate(-mTouchX + mAmplifierRadius / scale, -mTouchY + mAmplifierRadius / scale);
            doDraw(canvas);
            canvas.restore();

            // 画放大器的边框
            DrawUtil.drawCircle(canvas, mAmplifierRadius, mAmplifierRadius, mAmplifierRadius, mAmplifierPaint);
            canvas.restore();
        }

    }

    private void doDraw(Canvas canvas) {
        float left = (mCentreTranX + mTransX) / (mPrivateScale * mScale);
        float top = (mCentreTranY + mTransY) / (mPrivateScale * mScale);
        // 画布和图片共用一个坐标系，只需要处理屏幕坐标系到图片（画布）坐标系的映射关系
        canvas.scale(mPrivateScale * mScale, mPrivateScale * mScale); // 缩放画布
        canvas.translate(left, top); // 偏移画布

        if (!mIsDrawableOutside) { // 裁剪绘制区域为图片区域
            canvas.clipRect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        }

        if (isJustDrawOriginal) { // 只绘制原图
            canvas.drawBitmap(mBitmap, 0, 0, null);
            return;
        }

        // 绘制涂鸦
        canvas.drawBitmap(mGraffitiBitmap, 0, 0, null);

        if (mIsPainting) {  //画在view的画布上
            Path path;
            float span = 0;
            // 为了仅点击时也能出现绘图，必须移动path
            if (mTouchDownX == mTouchX && mTouchDownY == mTouchY && mTouchDownX == mLastTouchX && mTouchDownY == mLastTouchY) {
                mTempPath.reset();
                mTempPath.addPath(mCurrPath);
                mTempPath.quadTo(
                        toX(mLastTouchX),
                        toY(mLastTouchY),
                        toX((mTouchX + mLastTouchX + VALUE) / 2),
                        toY((mTouchY + mLastTouchY + VALUE) / 2));
                path = mTempPath;
                span = VALUE;
            } else {
                path = mCurrPath;
                span = 0;
            }
            // 画触摸的路径
            mPaint.setStrokeWidth(mPaintSize);
            if (mShape == Shape.HAND_WRITE) { // 手写
                draw(canvas, mPen, mPaint, path, mPen == Pen.ERASER ? mShaderMatrixEraser : mShaderMatrix,
                        mColor, mRotateDegree);
            } else {  // 画图形
                draw(canvas, mPen, mShape, mPaint,
                        toX(mTouchDownX), toY(mTouchDownY), toX(mTouchX + span), toY(mTouchY + span),
                        mPen == Pen.ERASER ? mShaderMatrixEraser : mShaderMatrix, mColor, mRotateDegree);
            }
        }

        if (mPen == Pen.COPY) {
            mCopyLocation.drawItSelf(canvas);
        }
    }

    private void draw(Canvas canvas, Pen pen, Paint paint, Path path, Matrix matrix, GraffitiColor color, int degree) {
        resetPaint(pen, paint, matrix, color, degree);

        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint);

    }

    private void draw(Canvas canvas, Pen pen, Shape shape, Paint paint, float sx, float sy, float dx, float dy, Matrix matrix, GraffitiColor color, int degree) {
        resetPaint(pen, paint, matrix, color, degree);

        paint.setStyle(Paint.Style.STROKE);

        switch (shape) { // 绘制图形
            case ARROW:
                paint.setStyle(Paint.Style.FILL);
                DrawUtil.drawArrow(canvas, sx, sy, dx, dy, paint);
                break;
            case LINE:
                DrawUtil.drawLine(canvas, sx, sy, dx, dy, paint);
                break;
            case FILL_CIRCLE:
                paint.setStyle(Paint.Style.FILL);
            case HOLLOW_CIRCLE:
                DrawUtil.drawCircle(canvas, sx, sy,
                        (float) Math.sqrt((sx - dx) * (sx - dx) + (sy - dy) * (sy - dy)), paint);
                break;
            case FILL_RECT:
                paint.setStyle(Paint.Style.FILL);
            case HOLLOW_RECT:
                DrawUtil.drawRect(canvas, sx, sy, dx, dy, paint);
                break;
            default:
                throw new RuntimeException("unknown shape:" + shape);
        }
    }


    private void draw(Canvas canvas, CopyOnWriteArrayList<GraffitiPath> pathStack) {
        // 还原堆栈中的记录的操作
        for (GraffitiPath path : pathStack) {
            draw(canvas, path);
        }
    }

    private void draw(Canvas canvas, GraffitiPath path) {
        mPaint.setStrokeWidth(path.mStrokeWidth);
        if (path.mShape == Shape.HAND_WRITE) { // 手写
            draw(canvas, path.mPen, mPaint, path.getPath(mRotateDegree), path.getMatrix(mRotateDegree), path.mColor, path.mRotateDegree);
        } else { // 画图形
            float[] sxy = path.getSxSy(mRotateDegree);
            float[] dxy = path.getDxDy(mRotateDegree);
            draw(canvas, path.mPen, path.mShape, mPaint,
                    sxy[0], sxy[1], dxy[0], dxy[1], path.getMatrix(mRotateDegree), path.mColor, path.mRotateDegree);
        }
    }

    private void resetPaint(Pen pen, Paint paint, Matrix matrix, GraffitiColor color, int rotateDegree) {
        switch (pen) { // 设置画笔
            case HAND:
                paint.setShader(null);
                mShaderMatrixColor.reset();

                if (color.getType() == GraffitiColor.Type.BITMAP) { // 旋转底图
                    if (mRotateDegree != 0) {
                        float px = mOriginalPivotX, py = mOriginalPivotY;
                        if (mRotateDegree == 90 || mRotateDegree == 270) { //　交换中心点的xy坐标
                            float t = px;
                            px = py;
                            py = t;
                        }
                        mShaderMatrixColor.postRotate(mRotateDegree, px, py);
                        if (Math.abs(mRotateDegree) == 90 || Math.abs(mRotateDegree) == 270) {
                            mShaderMatrixColor.postTranslate((py - px), -(py - px));
                        }
                    }
                }

                color.initColor(paint, mShaderMatrixColor);
                break;
            case COPY:
                // 调整copy图片位置
                mBitmapShader.setLocalMatrix(matrix);
                paint.setShader(this.mBitmapShader);
                break;
            case ERASER:
                mBitmapShaderEraser.setLocalMatrix(matrix);
                if (mBitmapShader != mBitmapShaderEraser) {
                    mBitmapShaderEraser.setLocalMatrix(mShaderMatrixEraser);
                }
                paint.setShader(this.mBitmapShaderEraser);
                break;
        }
    }


    /**
     * 将屏幕触摸坐标x转换成在图片中的坐标
     */
    public final float toX(float touchX) {
        return (touchX - mCentreTranX - mTransX) / (mPrivateScale * mScale);
    }

    /**
     * 将屏幕触摸坐标y转换成在图片中的坐标
     */
    public final float toY(float touchY) {
        return (touchY - mCentreTranY - mTransY) / (mPrivateScale * mScale);
    }

    /**
     * 坐标换算
     * （公式由toX()中的公式推算出）
     *
     * @param touchX    触摸坐标
     * @param graffitiX 在涂鸦图片中的坐标
     * @return 偏移量
     */
    public final float toTransX(float touchX, float graffitiX) {
        return -graffitiX * (mPrivateScale * mScale) + touchX - mCentreTranX;
    }

    public final float toTransY(float touchY, float graffitiY) {
        return -graffitiY * (mPrivateScale * mScale) + touchY - mCentreTranY;
    }

    private static class GraffitiPath {
        Pen mPen; // 画笔类型
        Shape mShape; // 画笔形状
        float mStrokeWidth; // 大小
        GraffitiColor mColor; // 颜色
        Path mPath; // 画笔的路径
        float mSx, mSy; // 映射后的起始坐标，（手指点击）
        float mDx, mDy; // 映射后的终止坐标，（手指抬起）
        Matrix mMatrix = new Matrix(); //　图片的偏移矩阵
        int mRotateDegree = 0; // 选择的角度（围绕图片中心旋转）
        float mPivotX, mPivotY;
        CopyLocation mCopy;

        public Path getPath(int currentDegree) {
            int degree = currentDegree - mRotateDegree;
            if (degree == 0) {
                return mPath;
            }
            Path path = new Path(mPath);
            Matrix matrix = new Matrix();

            float px = mPivotX, py = mPivotY;
            if (mRotateDegree == 90 || mRotateDegree == 270) { //　交换中心点的xy坐标
                float t = px;
                px = py;
                py = t;
            }

            matrix.setRotate(degree, px, py);
            if (Math.abs(degree) == 90 || Math.abs(degree) == 270) {
                matrix.postTranslate((py - px), -(py - px));
            }
            path.transform(matrix);
            return path;
        }

        public float[] getDxDy(int currentDegree) {

            return rotatePointInGrffiti(currentDegree, mRotateDegree, mDx, mDy, mPivotX, mPivotY);
        }

        public float[] getSxSy(int currentDegree) {

            return rotatePointInGrffiti(currentDegree, mRotateDegree, mSx, mSy, mPivotX, mPivotY);
        }

        public Matrix getMatrix(int currentDegree) {
            if (mMatrix == null) {
                return null;
            }
            if (mPen == Pen.COPY) { // 仿制，加上mCopyLocation记录的偏移
                mMatrix.reset();

                int degree = currentDegree - mRotateDegree;
                if (degree == 0) {
                    mMatrix.postTranslate(mCopy.mTouchStartX - mCopy.mCopyStartX, mCopy.mTouchStartY - mCopy.mCopyStartY);
                    return mMatrix;
                }
                float px = mPivotX, py = mPivotY;
                if (mRotateDegree == 90 || mRotateDegree == 270) { //　交换中心点的xy坐标
                    float t = px;
                    px = py;
                    py = t;
                }
                float[] coords = rotatePoint(degree, mCopy.mTouchStartX, mCopy.mTouchStartY, px, py);
                float[] coordsCopy = rotatePoint(degree, mCopy.mCopyStartX, mCopy.mCopyStartY, px, py);
                if (Math.abs(degree) == 90 || Math.abs(degree) == 270) {
                    coords[0] += (py - px);
                    coords[1] += -(py - px);
                    coordsCopy[0] += (py - px);
                    coordsCopy[1] += -(py - px);
                }
                mMatrix.postTranslate(coords[0] - coordsCopy[0], coords[1] - coordsCopy[1]);
                return mMatrix;
            } else {
                return mMatrix;
            }

        }

        static GraffitiPath toShape(Pen pen, Shape shape, float width, GraffitiColor color,
                                    float sx, float sy, float dx, float dy, int degree, float px, float py, CopyLocation copyLocation) {
            GraffitiPath path = new GraffitiPath();
            path.mPen = pen;
            path.mShape = shape;
            path.mStrokeWidth = width;
            path.mColor = color;
            path.mSx = sx;
            path.mSy = sy;
            path.mDx = dx;
            path.mDy = dy;
            path.mRotateDegree = degree;
            path.mPivotX = px;
            path.mPivotY = py;
            path.mCopy = copyLocation;
            return path;
        }

        static GraffitiPath toPath(Pen pen, Shape shape, float width, GraffitiColor color, Path p, int degree, float px, float py, CopyLocation copyLocation) {
            GraffitiPath path = new GraffitiPath();
            path.mPen = pen;
            path.mShape = shape;
            path.mStrokeWidth = width;
            path.mColor = color;
            path.mPath = p;
            path.mRotateDegree = degree;
            path.mPivotX = px;
            path.mPivotY = py;
            path.mCopy = copyLocation;
            return path;
        }
    }

    private void initCanvas() {
        if (mGraffitiBitmap != null) {
            mGraffitiBitmap.recycle();
        }
        mGraffitiBitmap = mBitmap.copy(Bitmap.Config.RGB_565, true);
        mBitmapCanvas = new Canvas(mGraffitiBitmap);
    }

    private void resetMatrix() {
        if (mPen == Pen.COPY) { // 仿制，加上mCopyLocation记录的偏移
            this.mShaderMatrix.reset();
            this.mShaderMatrix.postTranslate(mCopyLocation.mTouchStartX - mCopyLocation.mCopyStartX, mCopyLocation.mTouchStartY - mCopyLocation.mCopyStartY);
        } else {
            this.mShaderMatrix.reset();
        }
        mShaderMatrixEraser.reset();
        mShaderMatrixEraser.set(mShaderMatrix);

        // 如果使用了自定义的橡皮擦底图，则需要调整矩阵
        if (mPen == Pen.ERASER && mBitmapShader != mBitmapShaderEraser) {
            // 缩放橡皮擦底图，使之与涂鸦图片大小一样
            if (mEraserImageIsResizeable) {
                mShaderMatrixEraser.preScale(mBitmap.getWidth() * 1f / mBitmapEraser.getWidth(), mBitmap.getHeight() * 1f / mBitmapEraser.getHeight());
            } else {
                if (mRotateDegree == 90) {
                    mShaderMatrixEraser.preTranslate(mBitmap.getWidth() - mBitmapEraser.getWidth(), 0);
                } else if (mRotateDegree == 180) {
                    mShaderMatrixEraser.preTranslate(mBitmap.getWidth() - mBitmapEraser.getWidth(), mBitmap.getHeight() - mBitmapEraser.getHeight());
                } else if (mRotateDegree == 270) {
                    mShaderMatrixEraser.preTranslate(0, mBitmap.getHeight() - mBitmapEraser.getHeight());
                }
            }
        }
    }

    /**
     * 调整图片位置
     * <p>
     * 明白下面一点很重要：
     * 假设不考虑任何缩放，图片就是肉眼看到的那么大，此时图片的大小width =  mPrivateWidth * mScale ,
     * 偏移量x = mCentreTranX + mTransX，而view的大小为width = getWidth()。height和偏移量y以此类推。
     */
    private void judgePosition() {
        boolean changed = false;
        if (mPrivateWidth * mScale < getWidth()) { // 限制在view范围内
            if (mTransX + mCentreTranX < 0) {
                mTransX = -mCentreTranX;
                changed = true;
            } else if (mTransX + mCentreTranX + mPrivateWidth * mScale > getWidth()) {
                mTransX = getWidth() - mCentreTranX - mPrivateWidth * mScale;
                changed = true;
            }
        } else { // 限制在view范围外
            if (mTransX + mCentreTranX > 0) {
                mTransX = -mCentreTranX;
                changed = true;
            } else if (mTransX + mCentreTranX + mPrivateWidth * mScale < getWidth()) {
                mTransX = getWidth() - mCentreTranX - mPrivateWidth * mScale;
                changed = true;
            }
        }
        if (mPrivateHeight * mScale < getHeight()) { // 限制在view范围内
            if (mTransY + mCentreTranY < 0) {
                mTransY = -mCentreTranY;
                changed = true;
            } else if (mTransY + mCentreTranY + mPrivateHeight * mScale > getHeight()) {
                mTransY = getHeight() - mCentreTranY - mPrivateHeight * mScale;
                changed = true;
            }
        } else { // 限制在view范围外
            if (mTransY + mCentreTranY > 0) {
                mTransY = -mCentreTranY;
                changed = true;
            } else if (mTransY + mCentreTranY + mPrivateHeight * mScale < getHeight()) {
                mTransY = getHeight() - mCentreTranY - mPrivateHeight * mScale;
                changed = true;
            }
        }
        if (changed) {
            resetMatrix();
        }
    }

    private CopyLocation getCopyLocation() {
        if (mPen == Pen.COPY) {
            return mCopyLocation.copy();
        }
        return null;
    }

    /**
     * 仿制的定位器
     */
    private class CopyLocation {

        private float mCopyStartX, mCopyStartY; // 仿制的坐标
        private float mTouchStartX, mTouchStartY; // 开始触摸的坐标
        private float mX, mY; // 当前位置

        private Paint mPaint;

        private boolean isRelocating = true; // 正在定位中
        private boolean isCopying = false; // 正在仿制绘图中

        public CopyLocation(float copyStartX, float copyStartY, float touchStartX, float touchStartY) {
            this.mCopyStartX = copyStartX;
            this.mCopyStartY = copyStartY;
            this.mTouchStartX = touchStartX;
            this.mTouchStartY = touchStartY;
        }

        public CopyLocation(float x, float y) {
            mX = x;
            mY = y;
            mTouchStartX = x;
            mTouchStartY = y;
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setStrokeWidth(mPaintSize);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
        }


        public void updateLocation(float x, float y) {
            mX = x;
            mY = y;
        }

        public void setStartPosition(float x, float y) {
            mCopyStartX = mX;
            mCopyStartY = mY;
            mTouchStartX = x;
            mTouchStartY = y;
        }

        public void drawItSelf(Canvas canvas) {
            mPaint.setStrokeWidth(mPaintSize / 4);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(0xaa666666); // 灰色
            DrawUtil.drawCircle(canvas, mX, mY, mPaintSize / 2 + mPaintSize / 8, mPaint);

            mPaint.setStrokeWidth(mPaintSize / 16);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(0xaaffffff); // 白色
            DrawUtil.drawCircle(canvas, mX, mY, mPaintSize / 2 + mPaintSize / 32, mPaint);

            mPaint.setStyle(Paint.Style.FILL);
            if (!isCopying) {
                mPaint.setColor(0x44ff0000); // 红色
                DrawUtil.drawCircle(canvas, mX, mY, mPaintSize / 2, mPaint);
            } else {
                mPaint.setColor(0x44000088); // 蓝色
                DrawUtil.drawCircle(canvas, mX, mY, mPaintSize / 2, mPaint);
            }
        }

        /**
         * 判断是否点中
         */
        public boolean isInIt(float x, float y) {
            if ((mX - x) * (mX - x) + (mY - y) * (mY - y) <= mPaintSize * mPaintSize) {
                return true;
            }
            return false;
        }

        public CopyLocation copy() {
            CopyLocation copyLocation = new CopyLocation(mCopyStartX, mCopyStartY, mTouchStartX, mTouchStartY);
            return copyLocation;
        }

        public void rotatePosition(int originalDegree) {
            // 旋转仿制图标的位置
            float[] coords = rotatePointInGrffiti(mRotateDegree, originalDegree, this.mX,
                    this.mY, mOriginalPivotX, mOriginalPivotY);
            this.mX = coords[0];
            this.mY = coords[1];

            coords = rotatePointInGrffiti(mRotateDegree, originalDegree, this.mCopyStartX,
                    this.mCopyStartY, mOriginalPivotX, mOriginalPivotY);
            this.mCopyStartX = coords[0];
            this.mCopyStartY = coords[1];

            coords = rotatePointInGrffiti(mRotateDegree, originalDegree, this.mTouchStartX,
                    this.mTouchStartY, mOriginalPivotX, mOriginalPivotY);
            this.mTouchStartX = coords[0];
            this.mTouchStartY = coords[1];
        }

    }

    /**
     * 涂鸦底色
     */
    public static class GraffitiColor {
        public enum Type {
            COLOR, // 颜色值
            BITMAP // 图片
        }

        private int mColor;
        private Bitmap mBitmap;
        private Type mType;
        private Shader.TileMode mTileX = Shader.TileMode.MIRROR;
        private Shader.TileMode mTileY = Shader.TileMode.MIRROR;  // 镜像

        public GraffitiColor(int color) {
            mType = Type.COLOR;
            mColor = color;
        }

        public GraffitiColor(Bitmap bitmap) {
            mType = Type.BITMAP;
            mBitmap = bitmap;
        }

        public GraffitiColor(Bitmap bitmap, Shader.TileMode tileX, Shader.TileMode tileY) {
            mType = Type.BITMAP;
            mBitmap = bitmap;
            mTileX = tileX;
            mTileY = tileY;
        }

        void initColor(Paint paint, Matrix matrix) {
            if (mType == Type.COLOR) {
                paint.setColor(mColor);
            } else if (mType == Type.BITMAP) {
                BitmapShader shader = new BitmapShader(mBitmap, mTileX, mTileY);
                shader.setLocalMatrix(matrix);
                paint.setShader(shader);
            }
        }

        private void setColor(int color) {
            mType = Type.COLOR;
            mColor = color;
        }

        private void setColor(Bitmap bitmap) {
            mType = Type.BITMAP;
            mBitmap = bitmap;
        }

        private void setColor(Bitmap bitmap, Shader.TileMode tileX, Shader.TileMode tileY) {
            mType = Type.BITMAP;
            mBitmap = bitmap;
            mTileX = tileX;
            mTileY = tileY;
        }

        public int getColor() {
            return mColor;
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }

        public Type getType() {
            return mType;
        }

        public GraffitiColor copy() {
            GraffitiColor color = null;
            if (mType == Type.COLOR) {
                color = new GraffitiColor(mColor);
            } else {
                color = new GraffitiColor(mBitmap);
            }
            color.mTileX = mTileX;
            color.mTileY = mTileY;
            return color;
        }
    }


    // ===================== api ==============

    /**
     * 保存
     */
    public void save() {
//            initCanvas();
//            draw(mBitmapCanvas, mPathStackBackup, false);
//            draw(mBitmapCanvas, mPathStack, false);
        mGraffitiListener.onSaved(mGraffitiBitmap, mBitmapEraser);
    }

    /**
     * 清屏
     */
    public void clear() {
        mPathStack.clear();
//        mPathStackBackup.clear();
        initCanvas();
        invalidate();
    }

    /**
     * 撤销
     */
    public void undo() {
        if (mPathStack.size() > 0) {
            mPathStack.remove(mPathStack.size() - 1);
            initCanvas();
            draw(mBitmapCanvas, mPathStack);
            invalidate();
        }
    }

    /**
     * 是否有修改
     */
    public boolean isModified() {
        return mPathStack.size() != 0 || mRotateDegree != 0;
    }

    /**
     * 居中图片
     */
    public void centrePic() {
        mScale = 1;
        // 居中图片
        mTransX = 0;
        mTransY = 0;
        judgePosition();
        invalidate();
    }

    /**
     * 只绘制原图
     *
     * @param justDrawOriginal
     */
    public void setJustDrawOriginal(boolean justDrawOriginal) {
        isJustDrawOriginal = justDrawOriginal;
        invalidate();
    }

    public boolean isJustDrawOriginal() {
        return isJustDrawOriginal;
    }

    /**
     * 设置画笔底色
     *
     * @param color
     */
    public void setColor(int color) {
        mColor.setColor(color);
        invalidate();
    }

    public void setColor(Bitmap bitmap) {
        if (mBitmap == null) {
            return;
        }
        mColor.setColor(bitmap);
        invalidate();
    }

    public void setColor(Bitmap bitmap, Shader.TileMode tileX, Shader.TileMode tileY) {
        if (mBitmap == null) {
            return;
        }
        mColor.setColor(bitmap, tileX, tileY);
        invalidate();
    }

    public GraffitiColor getGraffitiColor() {
        return mColor;
    }

    /**
     * 缩放倍数，图片真实的缩放倍数为 mPrivateScale*mScale
     *
     * @param scale
     */
    public void setScale(float scale) {
        this.mScale = scale;
        judgePosition();
        resetMatrix();
        invalidate();
    }

    public float getScale() {
        return mScale;
    }

    /**
     * 设置画笔
     *
     * @param pen
     */
    public void setPen(Pen pen) {
        if (pen == null) {
            throw new RuntimeException("Pen can't be null");
        }
        mPen = pen;
        resetMatrix();
        invalidate();
    }

    public Pen getPen() {
        return mPen;
    }

    /**
     * 设置画笔形状
     *
     * @param shape
     */
    public void setShape(Shape shape) {
        if (shape == null) {
            throw new RuntimeException("Shape can't be null");
        }
        mShape = shape;
        invalidate();
    }

    public Shape getShape() {
        return mShape;
    }

    public void setTrans(float transX, float transY) {
        mTransX = transX;
        mTransY = transY;
        judgePosition();
        resetMatrix();
        invalidate();
    }

    /**
     * 设置图片偏移
     *
     * @param transX
     */
    public void setTransX(float transX) {
        this.mTransX = transX;
        judgePosition();
        invalidate();
    }

    public float getTransX() {
        return mTransX;
    }

    public void setTransY(float transY) {
        this.mTransY = transY;
        judgePosition();
        invalidate();
    }

    public float getTransY() {
        return mTransY;
    }


    public void setPaintSize(float paintSize) {
        mPaintSize = paintSize;
        invalidate();
    }

    public float getPaintSize() {
        return mPaintSize;
    }

    /**
     * 触摸时，图片区域外是否绘制涂鸦轨迹
     *
     * @param isDrawableOutside
     */
    public void setIsDrawableOutside(boolean isDrawableOutside) {
        mIsDrawableOutside = isDrawableOutside;
    }

    /**
     * 触摸时，图片区域外是否绘制涂鸦轨迹
     */
    public boolean getIsDrawableOutside() {
        return mIsDrawableOutside;
    }

    /**
     * 设置放大镜的倍数，当小于等于0时表示不使用放大器功能
     *
     * @param amplifierScale
     */
    public void setAmplifierScale(float amplifierScale) {
        mAmplifierScale = amplifierScale;
        invalidate();
    }

    public float getAmplifierScale() {
        return mAmplifierScale;
    }

    private static float[] rotatePointInGrffiti(int nowDegree, int oldDegree, float x, float y, float mOriginalPivotX, float mOriginalPivotY) {
        int degree = nowDegree - oldDegree;
        if (degree != 0) {
            float px = mOriginalPivotX, py = mOriginalPivotY;
            if (oldDegree == 90 || oldDegree == 270) { //　交换中心点的xy坐标
                float t = px;
                px = py;
                py = t;
            }

            float[] coords = rotatePoint(degree, x,
                    y, px, py);
            if (Math.abs(degree) == 90 || Math.abs(degree) == 270) {
                coords[0] += (py - px);
                coords[1] += -(py - px);
            }
            return coords;
        }
        return new float[]{x, y};
    }

    // 顺时针旋转
    public static float[] rotatePoint(int degree, float x, float y, float px, float py) {
        float[] coords = new float[2];
        /*角度变成弧度*/
        float radian = (float) (degree * Math.PI / 180);
        coords[0] = (float) ((x - px) * Math.cos(radian) - (y - py) * Math.sin(radian) + px);
        coords[1] = (float) ((x - px) * Math.sin(radian) + (y - py) * Math.cos(radian) + py);

        return coords;
    }

    public interface GraffitiListener {

        /**
         * 保存图片
         *
         * @param bitmap       涂鸦后的图片
         * @param bitmapEraser 橡皮擦底图
         */
        void onSaved(Bitmap bitmap, Bitmap bitmapEraser);

        /**
         * 出错
         *
         * @param i
         * @param msg
         */
        void onError(int i, String msg);

        /**
         * 准备工作已经完成
         */
        void onReady();
    }
}
