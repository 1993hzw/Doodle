package cn.hzw.graffiti;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Build;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.forward.androids.TouchGestureDetector;
import cn.forward.androids.utils.ImageUtils;
import cn.forward.androids.utils.Util;

import static cn.hzw.graffiti.util.DrawUtil.computeAngle;
import static cn.hzw.graffiti.util.DrawUtil.drawCircle;

/**
 * 涂鸦框架
 * Created by huangziwei on 2016/9/3.
 */
public class GraffitiView extends View implements IGraffiti {

    public final static float MAX_SCALE = 4f; // 最大缩放倍数
    public final static float MIN_SCALE = 0.25f; // 最小缩放倍数


    public static final int ERROR_INIT = -1;
    public static final int ERROR_SAVE = -2;

    private static final float VALUE = 1f;

    private GraffitiListener mGraffitiListener;

    private Bitmap mBitmap; // 当前涂鸦的原图（旋转后）
    private Bitmap mGraffitiBitmap; // 绘制涂鸦的图片
    private Canvas mBitmapCanvas;

    private int mOriginalWidth, mOriginalHeight; // 初始图片的尺寸
    private float mOriginalPivotX, mOriginalPivotY; // 图片中心

    private float mPrivateScale; // 图片适应屏幕时的缩放倍数
    private int mPrivateHeight, mPrivateWidth;// 图片适应屏幕时的大小（View窗口坐标系上的大小）
    private float mCentreTranX, mCentreTranY;// 图片在适应屏幕时，位于居中位置的偏移（View窗口坐标系上的偏移）

    private float mScale = 1; // 在适应屏幕时的缩放基础上的缩放倍数 （ 图片真实的缩放倍数为 mPrivateScale*mScale ）
    private float mTransX = 0, mTransY = 0; // 图片在适应屏幕且处于居中位置的基础上的偏移量（ 图片真实偏移量为mCentreTranX + mTransX，View窗口坐标系上的偏移）
    private float mMinScale = MIN_SCALE; // 最小缩放倍数
    private float mMaxScale = MAX_SCALE; // 最大缩放倍数

    private Path mCurrPath; // 当前手写的路径
    private Path mTempPath;
    private CopyLocation mCopyLocation; // 仿制的定位器

    private Paint mPaint;
    private float mPaintSize;
    private GraffitiColor mColor; // 画笔底色

    private boolean mIsPainting = false; // 是否正在绘制
    private boolean isJustDrawOriginal; // 是否只绘制原图

    private boolean mIsDrawableOutside = false; // 触摸时，图片区域外是否绘制涂鸦轨迹
    private boolean mReady = false;


    // 保存涂鸦操作，便于撤销
    private CopyOnWriteArrayList<IGraffitiItem> mItemStack = new CopyOnWriteArrayList<IGraffitiItem>();
    private CopyOnWriteArrayList<GraffitiPath> mPathStack = new CopyOnWriteArrayList<GraffitiPath>();
    private CopyOnWriteArrayList<IGraffitiSelectableItem> mSelectableStack = new CopyOnWriteArrayList<>();

    private Pen mPen;
    private Shape mShape;


    private float mAmplifierRadius;
    private Path mAmplifierPath;
    private float mAmplifierScale = 0; // 放大镜的倍数
    private Paint mAmplifierPaint;
    private int mAmplifierHorizonX; // 放大器的位置的x坐标，使其水平居中

    // 当前选择的文字信息
    private IGraffitiSelectableItem mSelectedItem;

    private float mSelectedItemX, mSelectedItemY;
    private boolean mIsRotatingSelectedItem;
    private float mRotateTextDiff; // 开始旋转图片时的差值（当前图片与触摸点的角度）

    private float mGraffitiSizeUnit = 1; // 长度单位，不同大小的图片的长度单位不一样。该单位的意义同dp的作用类似，独立于图片之外的单位长度
    private int mGraffitiRotateDegree = 0; // 相对于初始图片旋转的角度

    /**
     * @param context
     * @param bitmap
     * @param listener
     * @
     */
    public GraffitiView(Context context, Bitmap bitmap, GraffitiListener listener) {
        super(context);

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

        mOriginalWidth = mBitmap.getWidth();
        mOriginalHeight = mBitmap.getHeight();
        mOriginalPivotX = mOriginalWidth / 2f;
        mOriginalPivotY = mOriginalHeight / 2f;

        init();

    }

    public void init() {

        mScale = 1f;
        mColor = new GraffitiColor(Color.RED);
        mPaint = new Paint();
        mPaint.setStrokeWidth(mPaintSize);
        mPaint.setColor(mColor.getColor());
        mPaint.setAntiAlias(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);// 圆滑

        mPen = Pen.HAND;
        mShape = Shape.HAND_WRITE;

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
        if (!mReady) {
            mCopyLocation.updateLocation(toX(w / 2), toY(h / 2));
            mGraffitiListener.onReady();
            mReady = true;
        }
    }

    private TouchGestureDetector mTouchGestureDetector;
    private float mTouchX, mTouchY;
    private float mLastTouchX, mLastTouchY;
    private float mTouchDownX, mTouchDownY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mTouchGestureDetector == null) {
            mTouchGestureDetector = new TouchGestureDetector(getContext(), new TouchGestureDetector.OnTouchGestureListener() {

                boolean mIsBeginScroll = false;

                // 缩放相关
                private Float mLastFocusX;
                private Float mLastFocusY;
                private float mTouchCentreX, mTouchCentreY;

                @Override
                public boolean onDown(MotionEvent e) {
                    mIsBeginScroll = true;
                    mTouchX = mTouchDownX = e.getX();
                    mTouchY = mTouchDownY = e.getY();
                    return true;
                }

                /**
                 * 开始滚动
                 * @param event
                 */
                @Override
                public void onScrollBegin(MotionEvent event) {
                    mTouchX = event.getX();
                    mTouchY = event.getY();

                    if (isPenSelectable()) {
                        // 判断是否点中选择区域
                        mIsRotatingSelectedItem = false;
                        if (mSelectedItem != null) {
                            PointF xy = mSelectedItem.getLocation();
                            mSelectedItemX = xy.x;
                            mSelectedItemY = xy.y;
                            // 旋转
                            if (mSelectedItem.isCanRotate(GraffitiView.this, toX(mTouchX), toY(mTouchY))) {
                                mIsRotatingSelectedItem = true;
                                mRotateTextDiff = mSelectedItem.getItemRotate() -
                                        computeAngle(xy.x, xy.y, toX(mTouchX), toY(mTouchY));
                            }
                        }
                    } else {
                        // 点击copy
                        if (mPen == Pen.COPY && mCopyLocation.isInIt(toX(mTouchX), toY(mTouchY), mPaintSize)) {
                            mCopyLocation.setRelocating(true);
                            mCopyLocation.setCopying(false);
                        } else {
                            if (mPen == Pen.COPY) {
                                if (!mCopyLocation.isCopying()) {
                                    mCopyLocation.setStartPosition(toX(mTouchX), toY(mTouchY));
                                }
                                mCopyLocation.setCopying(true);
                            }
                            mCopyLocation.setRelocating(false);
                            mCurrPath = new Path();
                            mCurrPath.moveTo(toX(mTouchX), toY(mTouchY));
                            if (mShape == Shape.HAND_WRITE) { // 手写

                            } else {  // 画图形

                            }
                            mIsPainting = true;
                        }
                    }
                }

                @Override
                public void onScrollEnd(MotionEvent e) {
                    // 为了仅点击时也能出现绘图，必须移动path
                    if (mTouchDownX == mTouchX && mTouchDownY == mTouchY & mTouchDownX == mLastTouchX && mTouchDownY == mLastTouchY) {
                        mTouchX += VALUE;
                        mTouchY += VALUE;
                    }

                    if (isPenSelectable()) {
                        mIsRotatingSelectedItem = false;
                    } else {
                        if (mIsPainting) {
                            if (mPen == Pen.COPY) {
                                if (mCopyLocation.isRelocating()) { // 正在定位location
                                    mCopyLocation.updateLocation(toX(mTouchX), toY(mTouchY));
                                    mCopyLocation.setRelocating(false);
                                } else {
                                    mCopyLocation.updateLocation(mCopyLocation.getCopyStartX() + toX(mTouchX) - mCopyLocation.getTouchStartX(),
                                            mCopyLocation.getCopyStartY() + toY(mTouchY) - mCopyLocation.getTouchStartY());
                                }
                            }

                            GraffitiPath path = null;

                            // 把操作记录到加入的堆栈中
                            if (mShape == Shape.HAND_WRITE) { // 手写
                                mCurrPath.quadTo(
                                        toX(mLastTouchX),
                                        toY(mLastTouchY),
                                        toX((mTouchX + mLastTouchX) / 2),
                                        toY((mTouchY + mLastTouchY) / 2));
                                path = GraffitiPath.toPath(GraffitiView.this, mCurrPath);
                            } else {  // 画图形
                                path = GraffitiPath.toShape(GraffitiView.this,
                                        toX(mTouchDownX), toY(mTouchDownY), toX(mTouchX), toY(mTouchY));
                            }
                            addItem(path);
                            mIsPainting = false;
                        }
                    }

                    invalidate();
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    if (mIsBeginScroll) { // 标志开始滚动
                        mIsBeginScroll = false;
                        onScrollBegin(e1);
                    }

                    mLastTouchX = mTouchX;
                    mLastTouchY = mTouchY;
                    mTouchX = e2.getX();
                    mTouchY = e2.getY();

                    if (isPenSelectable()) { //画笔是否是可选择的
                        if (mIsRotatingSelectedItem) {
                            PointF xy = mSelectedItem.getLocation();
                            mSelectedItem.setItemRotate(mRotateTextDiff + computeAngle(
                                    xy.x, xy.y, toX(mTouchX), toY(mTouchY)
                            ));
                        } else {
                            if (mSelectedItem != null) {
                                mSelectedItem.setLocation(
                                        mSelectedItemX + toX(mTouchX) - toX(mTouchDownX),
                                        mSelectedItemY + toY(mTouchY) - toY(mTouchDownY));
                            }
                        }
                    } else {
                        if (mPen == Pen.COPY && mCopyLocation.isRelocating()) {
                            // 正在定位location
                            mCopyLocation.updateLocation(toX(mTouchX), toY(mTouchY));
                        } else {
                            if (mPen == Pen.COPY) {
                                mCopyLocation.updateLocation(mCopyLocation.getCopyStartX() + toX(mTouchX) - mCopyLocation.getTouchStartX(),
                                        mCopyLocation.getCopyStartY() + toY(mTouchY) - mCopyLocation.getTouchStartY());
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
                    }
                    invalidate();
                    return true;
                }


                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    if (isPenSelectable()) {
                        boolean found = false;
                        IGraffitiSelectableItem item;
                        for (int i = mSelectableStack.size() - 1; i >= 0; i--) {
                            item = mSelectableStack.get(i);
                            if (item.isInIt(GraffitiView.this, toX(mTouchX), toY(mTouchY))) {
                                found = true;
                                mSelectedItem = item;
                                PointF xy = item.getLocation();
                                mSelectedItemX = xy.x;
                                mSelectedItemY = xy.y;
                                mGraffitiListener.onSelectedItem(mSelectedItem, true);
                                break;
                            }
                        }
                        if (!found) {
                            if (mSelectedItem != null) { // 取消选定
                                IGraffitiSelectableItem old = mSelectedItem;
                                mSelectedItem = null;
                                mGraffitiListener.onSelectedItem(old, false);
                            } else {
                                mGraffitiListener.onCreateSelectableItem(mPen, toX(mTouchX), toY(mTouchY));
                            }
                        }
                    } else {

                    }
                    invalidate();
                    return true;
                }

                @Override
                public boolean onScaleBegin(ScaleGestureDetector detector) {
                    mLastFocusX = null;
                    mLastFocusY = null;
                    return true;
                }

                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    // 屏幕上的焦点
                    mTouchCentreX = detector.getFocusX();
                    mTouchCentreY = detector.getFocusY();

                    if (mLastFocusX != null && mLastFocusY != null) { // 焦点改变
                        final float dx = mTouchCentreX - mLastFocusX;
                        final float dy = mTouchCentreY - mLastFocusY;
                        // 移动图片
                        if (Math.abs(dx) > 1 || Math.abs(dy) > 1) {
                            GraffitiView.this.setTransX(GraffitiView.this.getTransX() + dx);
                            GraffitiView.this.setTransY(GraffitiView.this.getTransY() + dy);
                        }
                    }

                    if (detector.getScaleFactor() > 0.1f) {
                        // 缩放图片
                        float scale = GraffitiView.this.getScale() * detector.getScaleFactor();
                        GraffitiView.this.setScale(scale, toX(mTouchCentreX), toY(mTouchCentreY));
                    }

                    mLastFocusX = mTouchCentreX;
                    mLastFocusY = mTouchCentreY;

                    return true;
                }

            });

            mTouchGestureDetector.setScaleSpanSlop(1);
            mTouchGestureDetector.setScaleMinSpan(1);
            mTouchGestureDetector.setIsLongpressEnabled(false);
            mTouchGestureDetector.setIsScrollAfterScaled(false);
        }

        return mTouchGestureDetector.onTouchEvent(event);
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

        if (mPathStack.size() > 0) {
            for (IGraffitiItem item : mPathStack) {
                item.draw(mBitmapCanvas);
            }
        }

        mAmplifierRadius = Math.min(getWidth(), getHeight()) / 4;
        mAmplifierPath = new Path();
        mAmplifierPath.addCircle(mAmplifierRadius, mAmplifierRadius, mAmplifierRadius, Path.Direction.CCW);
        mAmplifierHorizonX = (int) (Math.min(getWidth(), getHeight()) / 2 - mAmplifierRadius);

        mGraffitiSizeUnit = Util.dp2px(getContext(), 1) / mPrivateScale;

        if (!mReady) { // 只有初始化时才需要设置画笔大小
            mPaintSize = 30 * mGraffitiSizeUnit;
        }

        mTransX = mTransY = 0;
        mScale = 1;

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
            drawCircle(canvas, mAmplifierRadius, mAmplifierRadius, mAmplifierRadius, mAmplifierPaint);
            canvas.restore();
        }

    }

    GraffitiPath graffitiPath = new GraffitiPath(this);

    private void doDraw(Canvas canvas) {
        float left = mCentreTranX + mTransX;
        float top = mCentreTranY + mTransY;

        // 画布和图片共用一个坐标系，只需要处理屏幕坐标系到图片（画布）坐标系的映射关系
        canvas.translate(left, top); // 偏移画布
        canvas.scale(mPrivateScale * mScale, mPrivateScale * mScale); // 缩放画布

        canvas.save();
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

            if (mShape == Shape.HAND_WRITE) { // 手写
                graffitiPath.reset(this, path);
            } else {  // 画图形
                graffitiPath.reset(this, toX(mTouchDownX), toY(mTouchDownY), toX(mTouchX + span), toY(mTouchY + span));
            }
            graffitiPath.setGraffiti(this);
            if (mPen == IGraffiti.Pen.ERASER || mPen == IGraffiti.Pen.COPY) {
                graffitiPath.getColor().setColor(mBitmap); // 图片底色为原图
            }
            graffitiPath.draw(canvas);


        }
        canvas.restore();


        if (mPen == Pen.COPY) {
            mCopyLocation.drawItSelf(canvas, mPaintSize);
        }

        for (IGraffitiSelectableItem item : mSelectableStack) {
            item.draw(canvas);
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
     * 将图片坐标x转换成屏幕触摸坐标
     */
    public final float toTouchX(float x) {
        return x * ((mPrivateScale * mScale)) + mCentreTranX + mTransX;
    }

    /**
     * 将图片坐标y转换成屏幕触摸坐标
     */
    public final float toTouchY(float y) {
        return y * ((mPrivateScale * mScale)) + mCentreTranY + mTransY;
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


    private void initCanvas() {
        if (mGraffitiBitmap != null) {
            mGraffitiBitmap.recycle();
        }
        mGraffitiBitmap = mBitmap.copy(Bitmap.Config.RGB_565, true);
        mBitmapCanvas = new Canvas(mGraffitiBitmap);
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
    }

    private CopyLocation getCopyLocation() {
        if (mPen == Pen.COPY) {
            return mCopyLocation.copy();
        }
        return null;
    }

    private boolean isPenSelectable() {
        return mPen == Pen.TEXT || mPen == Pen.BITMAP;
    }

    // ========================= api ================================

    @Override
    public int getRotate() {
        return mGraffitiRotateDegree;
    }

    /**
     * 相对于初始图片旋转的角度
     *
     * @param degree
     */
    @Override
    public void setRotate(int degree) {
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

        if (degree == mGraffitiRotateDegree) {
            return;
        }
        int r = degree - mGraffitiRotateDegree;
        int originalDegree = mGraffitiRotateDegree;
        mGraffitiRotateDegree = degree;

        mBitmap = ImageUtils.rotate(mBitmap, r, true);
        setBG();

        mCopyLocation.rotatePosition(originalDegree, mGraffitiRotateDegree, mOriginalPivotX, mOriginalPivotY);

        invalidate();

    }


    /**
     * 保存
     */
    @Override
    public void save() {

        mSelectedItem = null;

        // 保存的时候，把文字画上去
        for (IGraffitiSelectableItem item : mSelectableStack) {
            item.draw(mBitmapCanvas);
        }
        mGraffitiListener.onSaved(mGraffitiBitmap);
    }

    /**
     * 清屏
     */
    @Override
    public void clear() {
        mPathStack.clear();
        mSelectableStack.clear();
        mItemStack.clear();
        initCanvas();
        invalidate();
    }

    @Override
    public boolean undo(int step) {

        if (mItemStack.size() > 0) {
            step = Math.min(mItemStack.size(), step);
            removeItem(mItemStack.get(mItemStack.size() - step));
            return true;
        }
        return false;
    }

    /**
     * 撤销
     */
    @Override
    public boolean undo() {
        return undo(1);
    }

    /**
     * 只绘制原图
     *
     * @param justDrawOriginal
     */
    @Override
    public void setShowOriginal(boolean justDrawOriginal) {
        isJustDrawOriginal = justDrawOriginal;
        invalidate();
    }

    @Override
    public boolean isShowOriginal() {
        return isJustDrawOriginal;
    }

    @Override
    public float getOriginalBitmapWidth() {
        return mOriginalWidth;
    }

    @Override
    public float getOriginalBitmapHeight() {
        return mOriginalHeight;
    }

    /**
     * 设置画笔底色
     *
     * @param color
     */
    @Override
    public void setColor(GraffitiColor color) {
        mColor = color;
        if (mSelectedItem != null) {
            mSelectedItem.setColor(color);
        }
        invalidate();
    }

    @Override
    public GraffitiColor getColor() {
        return mColor;
    }

    /**
     * 围绕某个点缩放
     * 图片真实的缩放倍数为 mPrivateScale*mScale
     *
     * @param scale
     * @param pivotX 缩放的中心点
     * @param pivotY
     */
    @Override
    public void setScale(float scale, float pivotX, float pivotY) {
        if (scale < mMinScale) {
            scale = mMinScale;
        } else if (scale > mMaxScale) {
            scale = mMaxScale;
        }

        float touchX = toTouchX(pivotX);
        float touchY = toTouchY(pivotY);
        this.mScale = scale;

        // 缩放后，偏移图片，以产生围绕某个点缩放的效果
        mTransX = toTransX(touchX, pivotX);
        mTransY = toTransY(touchY, pivotY);

        judgePosition();
        invalidate();
    }

    @Override
    public float getScale() {
        return mScale;
    }

    /**
     * 设置画笔
     *
     * @param pen
     */
    @Override
    public void setPen(Pen pen) {
        if (pen == null) {
            throw new RuntimeException("Pen can't be null");
        }
        Pen old = mPen;
        mPen = pen;

        if (!isPenSelectable() || old != mPen) {
            if (mSelectedItem != null) {
                IGraffitiSelectableItem oldItem = mSelectedItem;
                mSelectedItem = null;
                mGraffitiListener.onSelectedItem(oldItem, false);
            }
        }

        invalidate();
    }

    @Override
    public IGraffiti.Pen getPen() {
        return mPen;
    }

    /**
     * 设置画笔形状
     *
     * @param shape
     */
    @Override
    public void setShape(Shape shape) {
        if (shape == null) {
            throw new RuntimeException("Shape can't be null");
        }
        mShape = shape;
        invalidate();
    }

    @Override
    public Shape getShape() {
        return mShape;
    }

    @Override
    public void setTrans(float transX, float transY) {
        mTransX = transX;
        mTransY = transY;
        judgePosition();
        invalidate();
    }

    /**
     * 设置图片偏移
     *
     * @param transX
     */
    @Override
    public void setTransX(float transX) {
        this.mTransX = transX;
        judgePosition();
        invalidate();
    }

    @Override
    public float getTransX() {
        return mTransX;
    }

    @Override
    public void setTransY(float transY) {
        this.mTransY = transY;
        judgePosition();
        invalidate();
    }

    @Override
    public float getTransY() {
        return mTransY;
    }


    @Override
    public void setSize(float paintSize) {
        mPaintSize = paintSize;
        if (mSelectedItem != null) {
            mSelectedItem.setSize(paintSize);
        }
        invalidate();
    }

    @Override
    public float getSize() {
        return mPaintSize;
    }

    /**
     * 触摸时，图片区域外是否绘制涂鸦轨迹
     *
     * @param isDrawableOutside
     */
    @Override
    public void setIsDrawableOutside(boolean isDrawableOutside) {
        mIsDrawableOutside = isDrawableOutside;
    }

    /**
     * 触摸时，图片区域外是否绘制涂鸦轨迹
     */
    @Override
    public boolean isDrawableOutside() {
        return mIsDrawableOutside;
    }

    /**
     * 设置放大镜的倍数，当小于等于0时表示不使用放大器功能
     *
     * @param amplifierScale
     */
    @Override
    public void setAmplifierScale(float amplifierScale) {
        mAmplifierScale = amplifierScale;
        invalidate();
    }

    @Override
    public float getAmplifierScale() {
        return mAmplifierScale;
    }

    @Override
    public boolean isSelectedItem() {
        return mSelectedItem != null;
    }

    @Override
    public IGraffitiSelectableItem getSelectedItem() {
        return mSelectedItem;
    }

    /**
     * 是否正在旋转item
     *
     * @return
     */
    @Override
    public boolean isRotatingItem() {
        return mIsRotatingSelectedItem;
    }

    @Override
    public void topItem() {
        if (mSelectedItem == null) {
            throw new NullPointerException("Selected item is null!");
        }
        mSelectableStack.remove(mSelectedItem);
        mSelectableStack.add(mSelectedItem);
        invalidate();
    }

    @Override
    public void setMinScale(float minScale) {
        mMinScale = minScale;
        setScale(mScale, 0, 0);
    }

    @Override
    public float getMinScale() {
        return mMinScale;
    }

    @Override
    public void setMaxScale(float maxScale) {
        mMaxScale = maxScale;
        setScale(mScale, 0, 0);
    }

    @Override
    public float getMaxScale() {
        return mMaxScale;
    }

    @Override
    public float getSizeUnit() {
        return mGraffitiSizeUnit;
    }

    @Override
    public void addItem(IGraffitiItem graffitiItem) {
        if (this != graffitiItem.getGraffiti()) {
            throw new RuntimeException("Graffiti is different");
        }
        if (graffitiItem instanceof GraffitiPath) {
            mPathStack.add((GraffitiPath) graffitiItem);
        }
        if (graffitiItem instanceof IGraffitiSelectableItem) {
            mSelectableStack.add((IGraffitiSelectableItem) graffitiItem);
        }
        mItemStack.add(graffitiItem);
        if (!(graffitiItem instanceof IGraffitiSelectableItem)) {
            graffitiItem.draw(mBitmapCanvas); // 保存到图片中
        }
        invalidate();
    }

    @Override
    public void removeItem(IGraffitiItem graffitiItem) {
        graffitiItem.setGraffiti(null);
        mPathStack.remove(graffitiItem);
        mSelectableStack.remove(graffitiItem);
        mItemStack.remove(graffitiItem);
        if (!(graffitiItem instanceof IGraffitiSelectableItem)) {
            initCanvas();
            for (IGraffitiItem item : mPathStack) {
                item.draw(mBitmapCanvas);
            }
        } else if (graffitiItem == mSelectedItem) {
            mSelectedItem = null;
            mGraffitiListener.onSelectedItem((IGraffitiSelectableItem) graffitiItem, false);
        }
        invalidate();
    }

    @Override
    public List<IGraffitiItem> getAllItem() {
        return null;
    }

    @Override
    public Bitmap getBitmap() {
        return mBitmap;
    }
}
