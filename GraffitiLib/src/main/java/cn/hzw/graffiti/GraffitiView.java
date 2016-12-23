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
import cn.forward.androids.utils.ThreadUtil;
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

    private Bitmap mBitmap; // 原图
    private Bitmap mBitmapEraser; // 橡皮擦底图
    private Bitmap mGraffitiBitmap; // 用绘制涂鸦的图片
    private Canvas mBitmapCanvas;

    private float mPrivateScale; // 图片适应屏幕时的缩放倍数
    private int mPrivateHeight, mPrivateWidth;// 图片适应屏幕时的大小（肉眼看到的在屏幕上的大小）
    private float mCentreTranX, mCentreTranY;// 图片居中时的偏移（肉眼看到的在屏幕上的偏移）

    private BitmapShader mBitmapShader; // 用于涂鸦的图片上
    private BitmapShader mBitmapShader4C;
    private BitmapShader mBitmapShaderEraser; // 橡皮擦底图
    private BitmapShader mBitmapShaderEraser4C;
    private Path mCurrPath; // 当前手写的路径
    private Path mCanvasPath; //
    private Path mTempPath;
    private CopyLocation mCopyLocation; // 仿制的定位器

    private Paint mPaint;
    private int mTouchMode; // 触摸模式，用于判断单点或多点触摸
    private float mPaintSize;
    private GraffitiColor mColor; // 画笔底色
    private float mScale; // 缩放倍数, 图片真实的缩放倍数为 mPrivateScale*mScale
    private float mTransX = 0, mTransY = 0; // 偏移量，图片真实偏移量为　mCentreTranX + mTransX

    private boolean mIsPainting = false; // 是否正在绘制
    private boolean isJustDrawOriginal; // 是否只绘制原图

    private boolean mIsDrawableOutside = false; // 触摸时，图片区域外是否绘制涂鸦轨迹
    private boolean mEraserImageIsResizeable;
    private boolean mReady = false;


    // 保存涂鸦操作，便于撤销
    private CopyOnWriteArrayList<GraffitiPath> mPathStack = new CopyOnWriteArrayList<GraffitiPath>();
//    private CopyOnWriteArrayList<GraffitiPath> mPathStackBackup = new CopyOnWriteArrayList<GraffitiPath>();

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
    private Matrix mShaderMatrix, mShaderMatrix4C, mMatrixTemp;

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

        this.mBitmapShader = new BitmapShader(this.mBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        this.mBitmapShader4C = new BitmapShader(this.mBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

        if (mBitmapEraser != null) {
            this.mBitmapShaderEraser = new BitmapShader(this.mBitmapEraser, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            this.mBitmapShaderEraser4C = new BitmapShader(this.mBitmapEraser, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        } else {
            this.mBitmapShaderEraser = mBitmapShader;
            this.mBitmapShaderEraser4C = mBitmapShader4C;
        }

        mShaderMatrix = new Matrix();
        mShaderMatrix4C = new Matrix();
        mMatrixTemp = new Matrix();
        mCanvasPath = new Path();
        mTempPath = new Path();
        mCopyLocation = new CopyLocation(150, 150);

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
        mCopyLocation.updateLocation(toX4C(w / 2), toY4C(h / 2));
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

                if (mPen == Pen.COPY && mCopyLocation.isInIt(toX4C(mTouchX), toY4C(mTouchY))) { // 点击copy
                    mCopyLocation.isRelocating = true;
                    mCopyLocation.isCopying = false;
                } else {
                    if (mPen == Pen.COPY) {
                        if (!mCopyLocation.isCopying) {
                            mCopyLocation.setStartPosition(toX4C(mTouchX), toY4C(mTouchY));
                            resetMatrix();
                        }
                        mCopyLocation.isCopying = true;
                    }
                    mCopyLocation.isRelocating = false;
                    if (mShape == Shape.HAND_WRITE) { // 手写
                        mCurrPath = new Path();
                        mCurrPath.moveTo(toX(mTouchDownX), toY(mTouchDownY));
                        mCanvasPath.reset();
                        mCanvasPath.moveTo(toX4C(mTouchDownX), toY4C(mTouchDownY));

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
                    mCopyLocation.updateLocation(toX4C(mTouchX), toY4C(mTouchY));
                    mCopyLocation.isRelocating = false;
                } else {
                    if (mIsPainting) {

                        if (mPen == Pen.COPY) {
                            mCopyLocation.updateLocation(mCopyLocation.mCopyStartX + toX4C(mTouchX) - mCopyLocation.mTouchStartX,
                                    mCopyLocation.mCopyStartY + toY4C(mTouchY) - mCopyLocation.mTouchStartY);
                        }

                        GraffitiPath path = null;

                        // 把操作记录到加入的堆栈中
                        if (mShape == Shape.HAND_WRITE) { // 手写
                            mCurrPath.quadTo(
                                    toX(mLastTouchX),
                                    toY(mLastTouchY),
                                    toX((mTouchX + mLastTouchX) / 2),
                                    toY((mTouchY + mLastTouchY) / 2));
                            path = GraffitiPath.toPath(mPen, mShape, mPaintSize, mColor.copy(), mCurrPath, mPen == Pen.COPY ? new Matrix(mShaderMatrix) : null);
                        } else {  // 画图形
                            path = GraffitiPath.toShape(mPen, mShape, mPaintSize, mColor.copy(),
                                    toX(mTouchDownX), toY(mTouchDownY), toX(mTouchX), toY(mTouchY),
                                    mPen == Pen.COPY ? new Matrix(mShaderMatrix) : null);
                        }
                        mPathStack.add(path);
                        draw(mBitmapCanvas, path, false); // 保存到图片中
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
                        mCopyLocation.updateLocation(toX4C(mTouchX), toY4C(mTouchY));
                    } else {
                        if (mPen == Pen.COPY) {
                            mCopyLocation.updateLocation(mCopyLocation.mCopyStartX + toX4C(mTouchX) - mCopyLocation.mTouchStartX,
                                    mCopyLocation.mCopyStartY + toY4C(mTouchY) - mCopyLocation.mTouchStartY);
                        }
                        if (mShape == Shape.HAND_WRITE) { // 手写
                            mCurrPath.quadTo(
                                    toX(mLastTouchX),
                                    toY(mLastTouchY),
                                    toX((mTouchX + mLastTouchX) / 2),
                                    toY((mTouchY + mLastTouchY) / 2));
                            mCanvasPath.quadTo(
                                    toX4C(mLastTouchX),
                                    toY4C(mLastTouchY),
                                    toX4C((mTouchX + mLastTouchX) / 2),
                                    toY4C((mTouchY + mLastTouchY) / 2));
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


    private void setBG() {// 不用resize preview
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
        canvas.scale(mPrivateScale * mScale, mPrivateScale * mScale); // 缩放画布，接下来的操作要进行坐标换算
        float left = (mCentreTranX + mTransX) / (mPrivateScale * mScale);
        float top = (mCentreTranY + mTransY) / (mPrivateScale * mScale);

        if (!mIsDrawableOutside) { // 裁剪绘制区域为图片区域
            canvas.clipRect(left, top, left + mBitmap.getWidth(), top + mBitmap.getHeight());
        }

        if (isJustDrawOriginal) { // 只绘制原图
            canvas.drawBitmap(mBitmap, left, top, null);
            return;
        }

        // 绘制涂鸦
        canvas.drawBitmap(mGraffitiBitmap, (mCentreTranX + mTransX) / (mPrivateScale * mScale), (mCentreTranY + mTransY) / (mPrivateScale * mScale), null);

        if (mIsPainting) {  //画在view的画布上
            Path path;
            float span = 0;
            // 为了仅点击时也能出现绘图，必须移动path
            if (mTouchDownX == mTouchX && mTouchDownY == mTouchY && mTouchDownX == mLastTouchX && mTouchDownY == mLastTouchY) {
                mTempPath.reset();
                mTempPath.addPath(mCanvasPath);
                mTempPath.quadTo(
                        toX4C(mLastTouchX),
                        toY4C(mLastTouchY),
                        toX4C((mTouchX + mLastTouchX + VALUE) / 2),
                        toY4C((mTouchY + mLastTouchY + VALUE) / 2));
                path = mTempPath;
                span = VALUE;
            } else {
                path = mCanvasPath;
                span = 0;
            }
            // 画触摸的路径
            mPaint.setStrokeWidth(mPaintSize);
            if (mShape == Shape.HAND_WRITE) { // 手写
                draw(canvas, mPen, mPaint, path, null, true, mColor);
            } else {  // 画图形
                draw(canvas, mPen, mShape, mPaint,
                        toX4C(mTouchDownX), toY4C(mTouchDownY), toX4C(mTouchX + span), toY4C(mTouchY + span), null, true, mColor);
            }
        }

        if (mPen == Pen.COPY) {
            mCopyLocation.drawItSelf(canvas);
        }
    }

    private void draw(Canvas canvas, Pen pen, Paint paint, Path path, Matrix matrix, boolean is4Canvas, GraffitiColor color) {
        resetPaint(pen, paint, is4Canvas, matrix, color);

        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint);

    }

    private void draw(Canvas canvas, Pen pen, Shape shape, Paint paint, float sx, float sy, float dx, float dy, Matrix matrix, boolean is4Canvas, GraffitiColor color) {
        resetPaint(pen, paint, is4Canvas, matrix, color);

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


    private void draw(Canvas canvas, CopyOnWriteArrayList<GraffitiPath> pathStack, boolean is4Canvas) {
        // 还原堆栈中的记录的操作
        for (GraffitiPath path : pathStack) {
            draw(canvas, path, is4Canvas);
        }
    }

    private void draw(Canvas canvas, GraffitiPath path, boolean is4Canvas) {
        mPaint.setStrokeWidth(path.mStrokeWidth);
        if (path.mShape == Shape.HAND_WRITE) { // 手写
            draw(canvas, path.mPen, mPaint, path.mPath, path.mMatrix, is4Canvas, path.mColor);
        } else { // 画图形
            draw(canvas, path.mPen, path.mShape, mPaint,
                    path.mSx, path.mSy, path.mDx, path.mDy, path.mMatrix, is4Canvas, path.mColor);
        }
    }

    private void resetPaint(Pen pen, Paint paint, boolean is4Canvas, Matrix matrix, GraffitiColor color) {
        switch (pen) { // 设置画笔
            case HAND:
                paint.setShader(null);
                if (is4Canvas) {
                    color.initColor(paint, mShaderMatrix4C);
                } else {
                    color.initColor(paint, null);
                }
                break;
            case COPY:
                if (is4Canvas) { // 画在view的画布上
                    paint.setShader(this.mBitmapShader4C);
                } else { // 调整copy图片位置
                    mBitmapShader.setLocalMatrix(matrix);
                    paint.setShader(this.mBitmapShader);
                }
                break;
            case ERASER:
                if (is4Canvas) {
                    paint.setShader(this.mBitmapShaderEraser4C);
                } else {
                    if (mBitmapShader == mBitmapShaderEraser) { // 图片的矩阵不需要任何偏移
                        mBitmapShaderEraser.setLocalMatrix(null);
                    }
                    paint.setShader(this.mBitmapShaderEraser);
                }
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

    /**
     * 将屏幕触摸坐标x转换成在canvas中的坐标
     */
    public final float toX4C(float x) {
        return (x) / (mPrivateScale * mScale);
    }

    /**
     * 将屏幕触摸坐标y转换成在canvas中的坐标
     */
    public final float toY4C(float y) {
        return (y) / (mPrivateScale * mScale);
    }


    private static class GraffitiPath {
        Pen mPen; // 画笔类型
        Shape mShape; // 画笔形状
        float mStrokeWidth; // 大小
        GraffitiColor mColor; // 颜色
        Path mPath; // 画笔的路径
        float mSx, mSy; // 映射后的起始坐标，（手指点击）
        float mDx, mDy; // 映射后的终止坐标，（手指抬起）
        Matrix mMatrix; //　仿制图片的偏移矩阵

        static GraffitiPath toShape(Pen pen, Shape shape, float width, GraffitiColor color,
                                    float sx, float sy, float dx, float dy, Matrix matrix) {
            GraffitiPath path = new GraffitiPath();
            path.mPen = pen;
            path.mShape = shape;
            path.mStrokeWidth = width;
            path.mColor = color;
            path.mSx = sx;
            path.mSy = sy;
            path.mDx = dx;
            path.mDy = dy;
            path.mMatrix = matrix;
            return path;
        }

        static GraffitiPath toPath(Pen pen, Shape shape, float width, GraffitiColor color, Path p, Matrix matrix) {
            GraffitiPath path = new GraffitiPath();
            path.mPen = pen;
            path.mShape = shape;
            path.mStrokeWidth = width;
            path.mColor = color;
            path.mPath = p;
            path.mMatrix = matrix;
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
            this.mShaderMatrix.set(null);
            this.mShaderMatrix.postTranslate(mCopyLocation.mTouchStartX - mCopyLocation.mCopyStartX, mCopyLocation.mTouchStartY - mCopyLocation.mCopyStartY);
            this.mBitmapShader.setLocalMatrix(this.mShaderMatrix);


            this.mShaderMatrix4C.set(null);
            this.mShaderMatrix4C.postTranslate((mCentreTranX + mTransX) / (mPrivateScale * mScale) + mCopyLocation.mTouchStartX - mCopyLocation.mCopyStartX,
                    (mCentreTranY + mTransY) / (mPrivateScale * mScale) + mCopyLocation.mTouchStartY - mCopyLocation.mCopyStartY);
            this.mBitmapShader4C.setLocalMatrix(this.mShaderMatrix4C);

        } else {
            this.mShaderMatrix.set(null);
            this.mBitmapShader.setLocalMatrix(this.mShaderMatrix);

            this.mShaderMatrix4C.set(null);
            this.mShaderMatrix4C.postTranslate((mCentreTranX + mTransX) / (mPrivateScale * mScale), (mCentreTranY + mTransY) / (mPrivateScale * mScale));
            this.mBitmapShader4C.setLocalMatrix(this.mShaderMatrix4C);
        }

        // 如果使用了自定义的橡皮擦底图，则需要跳转矩阵
        if (mPen == Pen.ERASER && mBitmapShader != mBitmapShaderEraser) {
            mMatrixTemp.reset();
            mBitmapShaderEraser.getLocalMatrix(mMatrixTemp);
            mBitmapShader.getLocalMatrix(mMatrixTemp);
            // 缩放橡皮擦底图，使之与涂鸦图片大小一样
            if (mEraserImageIsResizeable) {
                mMatrixTemp.preScale(mBitmap.getWidth() * 1f / mBitmapEraser.getWidth(), mBitmap.getHeight() * 1f / mBitmapEraser.getHeight());
            }
            mBitmapShaderEraser.setLocalMatrix(mMatrixTemp);

            mMatrixTemp.reset();
            mBitmapShaderEraser4C.getLocalMatrix(mMatrixTemp);
            mBitmapShader4C.getLocalMatrix(mMatrixTemp);
            // 缩放橡皮擦底图，使之与涂鸦图片大小一样
            if (mEraserImageIsResizeable) {
                mMatrixTemp.preScale(mBitmap.getWidth() * 1f / mBitmapEraser.getWidth(), mBitmap.getHeight() * 1f / mBitmapEraser.getHeight());
            }
            mBitmapShaderEraser4C.setLocalMatrix(mMatrixTemp);
        }
    }

    /**
     * 调整图片位置
     */
    private void judgePosition() {
        boolean changed = false;
        if (mPrivateWidth * mScale > getWidth()) { // 图片偏移的位置不能超过屏幕边缘
            if (mCentreTranX + mTransX > 0) {
                mTransX = -mCentreTranX;
                changed = true;
            } else if (mCentreTranX + mTransX + mPrivateWidth * mScale < getWidth()) {
                mTransX = getWidth() - mPrivateWidth * mScale - mCentreTranX;
                changed = true;
            }
        } else { // 图片只能在屏幕可见范围内移动
            if (mCentreTranX + mTransX + mBitmap.getWidth() * mPrivateScale * mScale > getWidth()) { // mScale<1是preview.width不用乘scale
                mTransX = getWidth() - mBitmap.getWidth() * mPrivateScale * mScale - mCentreTranX;
                changed = true;
            } else if (mCentreTranX + mTransX < 0) {
                mTransX = -mCentreTranX;
                changed = true;
            }
        }

        if (mPrivateHeight * mScale > getHeight()) { // 图片偏移的位置不能超过屏幕边缘
            if (mCentreTranY + mTransY > 0) {
                mTransY = -mCentreTranY;
                changed = true;
            } else if (mCentreTranY + mTransY + mPrivateHeight * mScale < getHeight()) {
                mTransY = getHeight() - mPrivateHeight * mScale - mCentreTranY;
                changed = true;
            }
        } else { // 图片只能在屏幕可见范围内移动
            if (mCentreTranY + mTransY + mBitmap.getHeight() * mPrivateScale * mScale > getHeight()) {
                mTransY = getHeight() - mBitmap.getHeight() * mPrivateScale * mScale - mCentreTranY;
                changed = true;
            } else if (mCentreTranY + mTransY < 0) {
                mTransY = -mCentreTranY;
                changed = true;
            }
        }
        if (changed) {
            resetMatrix();
        }
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
            draw(mBitmapCanvas, mPathStack, false);
            invalidate();
        }
    }

    /**
     * 是否有修改
     */
    public boolean isModified() {
        return mPathStack.size() != 0;
    }

    /**
     * 居中图片
     */
    public void centrePic() {
        if (mScale >= 1) {
            ThreadUtil.getInstance().runOnAsyncThread(new Runnable() {
                boolean isScaling = true;
                int mTimes = Math.max((int) ((mScale - 1) / 0.2f + 0.5f), 1);
                float mSpan = mTransX / mTimes;

                public void run() {
                    do {
                        // 围绕坐标(0,0)缩放图片
                        mScale -= 0.2f;

                        if (mScale <= 1) {
                            mScale = 1;
                            isScaling = false;
                        }
                        mTransX = mTransX - mSpan;
                        if (!isScaling) {
                            mTransX = 0;
                        }
                        judgePosition();
                        postInvalidate();
                        try {
                            Thread.sleep(TIME_SPAN / 2);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } while (isScaling);

                }
            });
        } else if (mScale < 1) {
            ThreadUtil.getInstance().runOnAsyncThread(new Runnable() {
                boolean isScaling = true;
                int mTimes = Math.max((int) ((1 - mScale) / 0.2f), 1);
                float mSpan = mTransX / mTimes;

                public void run() {
                    do {
                        mScale += 0.2f;
                        if (mScale >= 1) {
                            mScale = 1;
                            isScaling = false;
                        }
                        mTransX = mTransX - mSpan;
                        if (!isScaling) {
                            mTransX = 0;
                        }
                        judgePosition();
                        postInvalidate();
                        try {
                            Thread.sleep(TIME_SPAN / 2);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } while (isScaling);
                }
            });
        }
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
