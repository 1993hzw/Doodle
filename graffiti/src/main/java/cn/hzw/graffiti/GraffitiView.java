package cn.hzw.graffiti;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.forward.androids.utils.ImageUtils;
import cn.forward.androids.utils.Util;
import cn.hzw.graffiti.core.IGraffiti;
import cn.hzw.graffiti.core.IGraffitiColor;
import cn.hzw.graffiti.core.IGraffitiItem;
import cn.hzw.graffiti.core.IGraffitiPen;
import cn.hzw.graffiti.core.IGraffitiShape;
import cn.hzw.graffiti.core.IGraffitiTouchDetector;

import static cn.hzw.graffiti.util.DrawUtil.drawCircle;
import static cn.hzw.graffiti.util.DrawUtil.rotatePoint;

/**
 * 涂鸦框架
 * Created by huangziwei on 2016/9/3.
 */
public class GraffitiView extends FrameLayout implements IGraffiti {

    // ACTION
    public final static int ACTION_ROTATION = 1; // 旋转
    public final static int ACTION_SAVE = 2; // 保存
    public final static int ACTION_UNDO = 3; // 撤销
    public final static int ACTION_CLEAR = 4; // 清空

    public final static float MAX_SCALE = 4f; // 最大缩放倍数
    public final static float MIN_SCALE = 0.25f; // 最小缩放倍数
    public final static int DEFAULT_SIZE = 6; // 默认画笔大小

    public static final int ERROR_INIT = -1;
    public static final int ERROR_SAVE = -2;

    private IGraffitiListener mGraffitiListener;

    private Bitmap mBitmap; // 当前涂鸦的原图（旋转后）
    private Bitmap mGraffitiBitmap; // 绘制涂鸦的图片
    private Canvas mBitmapCanvas;

    private float mPrivateScale; // 图片适应屏幕时的缩放倍数
    private int mPrivateHeight, mPrivateWidth;// 图片适应屏幕时的大小（View窗口坐标系上的大小）
    private float mCentreTranX, mCentreTranY;// 图片在适应屏幕时，位于居中位置的偏移（View窗口坐标系上的偏移）

    private float mRotateScale = 1;
    private float mRotateTranX, mRotateTranY;

    private float mScale = 1; // 在适应屏幕时的缩放基础上的缩放倍数 （ 图片真实的缩放倍数为 mPrivateScale*mScale ）
    private float mTransX = 0, mTransY = 0; // 图片在适应屏幕且处于居中位置的基础上的偏移量（ 图片真实偏移量为mCentreTranX + mTransX，View窗口坐标系上的偏移）
    private float mMinScale = MIN_SCALE; // 最小缩放倍数
    private float mMaxScale = MAX_SCALE; // 最大缩放倍数

    private Paint mPaint;
    private float mSize;
    private IGraffitiColor mColor; // 画笔底色

    private boolean isJustDrawOriginal; // 是否只绘制原图

    private boolean mIsDrawableOutside = false; // 触摸时，图片区域外是否绘制涂鸦轨迹
    private boolean mReady = false;

    private float mTouchX, mTouchY;
    private boolean mEnableAmplifier = false; // 放大镜功能

    // 保存涂鸦操作，便于撤销
    private CopyOnWriteArrayList<IGraffitiItem> mItemStack = new CopyOnWriteArrayList<IGraffitiItem>();

    private IGraffitiPen mPen;
    private IGraffitiShape mShape;

    private float mAmplifierRadius;
    private Path mAmplifierPath;
    private float mAmplifierScale = 0; // 放大镜的倍数
    private Paint mAmplifierPaint;
    private int mAmplifierHorizonX; // 放大器的位置的x坐标，使其水平居中

    private float mGraffitiSizeUnit = 1; // 长度单位，不同大小的图片的长度单位不一样。该单位的意义同dp的作用类似，独立于图片之外的单位长度
    private int mGraffitiRotateDegree = 0; // 相对于初始图片旋转的角度

    private List<WeakReference<IGraffitiViewListener>> mListenerList = new CopyOnWriteArrayList<>();

    // 手势相关
    private IGraffitiTouchDetector mDefaultTouchDetector;
    private Map<IGraffitiPen, IGraffitiTouchDetector> mTouchDetectorMap = new HashMap<>();

    private GraffitiViewInner mInner;
    private RectF mGraffitiBound = new RectF();
    private PointF mTempPoint = new PointF();

    public GraffitiView(Context context, Bitmap bitmap, IGraffitiListener listener) {
        this(context, bitmap, listener, null);
    }

    /**
     * @param context
     * @param bitmap
     * @param listener
     * @param defaultDetector 默认手势识别器
     */
    public GraffitiView(Context context, Bitmap bitmap, IGraffitiListener listener, IGraffitiTouchDetector defaultDetector) {
        super(context);

        // 关闭硬件加速，因为bitmap的Canvas不支持硬件加速
        if (Build.VERSION.SDK_INT >= 11) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }

        mBitmap = bitmap;
        mGraffitiListener = listener;
        if (mGraffitiListener == null) {
            throw new RuntimeException("IGraffitiListener is null!!!");
        }
        if (mBitmap == null) {
            throw new RuntimeException("Bitmap is null!!!");
        }

        mScale = 1f;
        mColor = new GraffitiColor(Color.RED);
        mPaint = new Paint();
        mPaint.setStrokeWidth(mSize);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);// 圆滑

        mPen = GraffitiPen.HAND;
        mShape = GraffitiShape.HAND_WRITE;

        mAmplifierPaint = new Paint();
        mAmplifierPaint.setColor(0xaaffffff);
        mAmplifierPaint.setStyle(Paint.Style.STROKE);
        mAmplifierPaint.setAntiAlias(true);
        mAmplifierPaint.setStrokeJoin(Paint.Join.ROUND);
        mAmplifierPaint.setStrokeCap(Paint.Cap.ROUND);// 圆滑
        mAmplifierPaint.setStrokeWidth(Util.dp2px(getContext(), 10));

        mDefaultTouchDetector = defaultDetector;

        mInner = new GraffitiViewInner(context);
        addView(mInner);
        setClipChildren(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initCanvas();
        initGraffitiBitmap();
        if (!mReady) {
            mGraffitiListener.onReady();
            mReady = true;
        }
    }

    private Matrix mTouchEventMatrix = new Matrix();

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        mTouchX = event.getX();
        mTouchY = event.getY();

        if (mGraffitiRotateDegree == 0 || mGraffitiRotateDegree == 180) {
            return super.dispatchTouchEvent(event);
        } else {
            // 把事件转发给innerView，避免在区域外不可点击
            MotionEvent transformedEvent = MotionEvent.obtain(event);
//        final float offsetX = mInner.getScrollX() - mInner.getLeft();
//        final float offsetY = mInner.getScrollY() - mInner.getTop();
//        transformedEvent.offsetLocation(offsetX, offsetY);
            mTouchEventMatrix.reset();
            mTouchEventMatrix.setRotate(-mGraffitiRotateDegree, mInner.getWidth() / 2, mInner.getHeight() / 2);
            transformedEvent.transform(mTouchEventMatrix);
            boolean handled = mInner.dispatchTouchEvent(transformedEvent);
            transformedEvent.recycle();

            return handled;
        }
    }

    private void initGraffitiBitmap() {// 不用resize preview
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

        mAmplifierRadius = Math.min(getWidth(), getHeight()) / 4;
        mAmplifierPath = new Path();
        mAmplifierPath.addCircle(mAmplifierRadius, mAmplifierRadius, mAmplifierRadius, Path.Direction.CCW);
        mAmplifierHorizonX = (int) (Math.min(getWidth(), getHeight()) / 2 - mAmplifierRadius);

        mGraffitiSizeUnit = Util.dp2px(getContext(), 1) / mPrivateScale;

        if (!mReady) { // 只有初始化时才需要设置画笔大小
            mSize = DEFAULT_SIZE * mGraffitiSizeUnit;
        }
        // 居中适应屏幕
        mTransX = mTransY = 0;
        mScale = 1;

        invalidate();
    }

    public RectF getGraffitiBound() {
        rotatePoint(mTempPoint, mGraffitiRotateDegree, mTransX, mTransY, 0, 0);
        mTempPoint.x = mCentreTranX + mRotateTranX + mTempPoint.x;
        mTempPoint.y = mCentreTranY + mRotateTranY + mTempPoint.y;

        mGraffitiBound.set(mTempPoint.x, mTempPoint.y, mTempPoint.x + mPrivateWidth * mRotateScale * mScale, mTempPoint.y + mPrivateHeight * mRotateScale * mScale);

        float px = mTempPoint.x + mPrivateWidth * mRotateScale / 2;
        float py = mTempPoint.y + mPrivateHeight * mRotateScale / 2;
        // 左上
        rotatePoint(mTempPoint, mGraffitiRotateDegree, mGraffitiBound.left, mGraffitiBound.top, px, py);
        float ltX = mTempPoint.x;
        float ltY = mTempPoint.y;
        //右下
        rotatePoint(mTempPoint, mGraffitiRotateDegree, mGraffitiBound.right, mGraffitiBound.bottom, px, py);
        float rbX = mTempPoint.x;
        float rbY = mTempPoint.y;
        // 左下
        rotatePoint(mTempPoint, mGraffitiRotateDegree, mGraffitiBound.left, mGraffitiBound.bottom, px, py);
        float lbX = mTempPoint.x;
        float lbY = mTempPoint.y;
        //右上
        rotatePoint(mTempPoint, mGraffitiRotateDegree, mGraffitiBound.right, mGraffitiBound.top, px, py);
        float rtX = mTempPoint.x;
        float rtY = mTempPoint.y;

        mGraffitiBound.left = Math.min(Math.min(ltX, rbX), Math.min(lbX, rtX));
        mGraffitiBound.top = Math.min(Math.min(ltY, rbY), Math.min(lbY, rtY));
        mGraffitiBound.right = Math.max(Math.max(ltX, rbX), Math.max(lbX, rtX));
        mGraffitiBound.bottom = Math.max(Math.max(ltY, rbY), Math.max(lbY, rtY));

        return mGraffitiBound;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (mBitmap.isRecycled() || mGraffitiBitmap.isRecycled()) {
            return;
        }

        super.dispatchDraw(canvas);

        /*mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeWidth(20);
        canvas.drawRect( getGraffitiBound(), mPaint);*/

        if (mEnableAmplifier && mAmplifierScale > 0) { //启用放大镜
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
            super.dispatchDraw(canvas);
            canvas.restore();

            // 画放大器的边框
            drawCircle(canvas, mAmplifierRadius, mAmplifierRadius, mAmplifierRadius, mAmplifierPaint);
            canvas.restore();
        }

    }

    private void doDraw(Canvas canvas) {
        float left = getInnerTranX();
        float top = getInnerTranY();

        // 画布和图片共用一个坐标系，只需要处理屏幕坐标系到图片（画布）坐标系的映射关系
        canvas.translate(left, top); // 偏移画布
        float scale = getInnerScale();
        canvas.scale(scale, scale); // 缩放画布

        if (isJustDrawOriginal) { // 只绘制原图
            canvas.drawBitmap(mBitmap, 0, 0, null);
            return;
        }
        // 绘制涂鸦后的图片
        canvas.drawBitmap(mGraffitiBitmap, 0, 0, null);

        boolean canvasClipped = false;
        canvas.save(); // 1
        if (!mIsDrawableOutside) { // 裁剪绘制区域为图片区域
            canvasClipped = true;
            canvas.clipRect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        }
        for (IGraffitiItem item : mItemStack) {
            if (item instanceof GraffitiItemBase) {
                if (((GraffitiItemBase) item).isDrawOptimize()) { // 优化绘制

                } else { //画在view的画布上
                    if (!item.isNeedClipOutside()) { // 1.不需要裁剪
                        if (canvasClipped) {
                            canvas.restore();
                        }

                        ((GraffitiItemBase) item).drawBefore(canvas);
                        item.draw(canvas);
                        ((GraffitiItemBase) item).drawAfter(canvas);

                        if (canvasClipped) { // 2.恢复裁剪
                            canvas.save();
                            canvas.clipRect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
                        }
                    } else {
                        ((GraffitiItemBase) item).drawBefore(canvas);
                        item.draw(canvas);
                        ((GraffitiItemBase) item).drawAfter(canvas);
                    }
                }
            } else {
                item.draw(canvas);
            }
        }
        canvas.restore();

        mPen.draw(canvas, this);
    }

    private float getInnerScale() {
        return mPrivateScale * mRotateScale * mScale;
    }

    private float getInnerTranX() {
        return mCentreTranX + mRotateTranX + mTransX;
    }

    private float getInnerTranY() {
        return mCentreTranY + mRotateTranY + mTransY;
    }

    /**
     * 将屏幕触摸坐标x转换成在图片中的坐标
     */
    public final float toX(float touchX) {
        return (touchX - getInnerTranX()) / getInnerScale();
    }

    /**
     * 将屏幕触摸坐标y转换成在图片中的坐标
     */
    public final float toY(float touchY) {
        return (touchY - getInnerTranY()) / getInnerScale();
    }

    /**
     * 将图片坐标x转换成屏幕触摸坐标
     */
    public final float toTouchX(float x) {
        return x * getInnerScale() + getInnerTranX();
    }

    /**
     * 将图片坐标y转换成屏幕触摸坐标
     */
    public final float toTouchY(float y) {
        return y * getInnerScale() + getInnerTranY();
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
        return -graffitiX * getInnerScale() + touchX - mCentreTranX - mRotateTranX;
    }

    public final float toTransY(float touchY, float graffitiY) {
        return -graffitiY * getInnerScale() + touchY - mCentreTranY - mRotateTranY;
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
       /* if (mPrivateWidth * mScale < getWidth()) { // 限制在view范围内
            if (mTransX + mCentreTranX < 0) {
                mTransX = -mCentreTranX;
            } else if (mTransX + mCentreTranX + mPrivateWidth * mScale > getWidth()) {
                mTransX = getWidth() - mCentreTranX - mPrivateWidth * mScale;
            }
        } else { // 限制在view范围外
            if (mTransX + mCentreTranX > 0) {
                mTransX = -mCentreTranX;
            } else if (mTransX + mCentreTranX + mPrivateWidth * mScale < getWidth()) {
                mTransX = getWidth() - mCentreTranX - mPrivateWidth * mScale;
            }
        }
        if (mPrivateHeight * mScale < getHeight()) { // 限制在view范围内
            if (mTransY + mCentreTranY < 0) {
                mTransY = -mCentreTranY;
            } else if (mTransY + mCentreTranY + mPrivateHeight * mScale > getHeight()) {
                mTransY = getHeight() - mCentreTranY - mPrivateHeight * mScale;
            }
        } else { // 限制在view范围外
            if (mTransY + mCentreTranY > 0) {
                mTransY = -mCentreTranY;
            } else if (mTransY + mCentreTranY + mPrivateHeight * mScale < getHeight()) {
                mTransY = getHeight() - mCentreTranY - mPrivateHeight * mScale;
            }
        }*/
    }

    /**
     * 根据画笔绑定手势识别器
     *
     * @param pen
     * @param detector
     */
    public void bindTouchDetector(IGraffitiPen pen, IGraffitiTouchDetector detector) {
        if (pen == null) {
            return;
        }
        mTouchDetectorMap.put(pen, detector);
    }

    /**
     * 获取画笔绑定的手势识别器
     *
     * @param pen
     */
    public IGraffitiTouchDetector getDefaultTouchDetector(IGraffitiPen pen) {
        return mTouchDetectorMap.get(pen);
    }

    /**
     * 移除指定画笔的手势识别器
     *
     * @param pen
     */
    public void removeTouchDetector(IGraffitiPen pen) {
        if (pen == null) {
            return;
        }
        mTouchDetectorMap.remove(pen);
    }

    /**
     * 设置默认手势识别器
     *
     * @param touchGestureDetector
     */
    public void setDefaultTouchDetector(IGraffitiTouchDetector touchGestureDetector) {
        mDefaultTouchDetector = touchGestureDetector;
    }

    /**
     * 默认手势识别器
     *
     * @return
     */
    public IGraffitiTouchDetector getDefaultTouchDetector() {
        return mDefaultTouchDetector;
    }

    // ========================= api ================================

    /**
     * 强制刷新，包括重新刷新涂鸦图片
     */
    public void invalidateForce() {
        initCanvas();
        // 重新绘制到图片上
        for (IGraffitiItem item : mItemStack) {
            if (item instanceof GraffitiItemBase) {
                if (((GraffitiItemBase) item).isDrawOptimize()) { // 优化绘制
                    item.draw(mBitmapCanvas);
                } else { //画在view的画布上

                }
            } else {
                item.draw(mBitmapCanvas);
            }
        }
        invalidate();
    }

    public void invalidate(IGraffitiItem item) {
        if (!mItemStack.contains(item)) {
            throw new RuntimeException("graffiti doesn't include the item");
        }
        if (((GraffitiItemBase) item).isDrawOptimize()) { // 优化绘制,保存到图片上
            item.draw(mBitmapCanvas);
        } else {

        }
        invalidate();
    }

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
        mGraffitiRotateDegree = degree;
        mGraffitiRotateDegree = mGraffitiRotateDegree % 360;
        mInner.setPivotX(mInner.getWidth() / 2);
        mInner.setPivotY(mInner.getHeight() / 2);
        mInner.setRotation(mGraffitiRotateDegree);

        RectF rectF = getGraffitiBound();

        int w = (int) (rectF.width()/getInnerScale());
        int h = (int) (rectF.height()/getInnerScale());
        float nw = w * 1f / getWidth();
        float nh = h * 1f / getHeight();
        float scale;
        float tx, ty;
        if (nw > nh) {
            scale = 1 / nw;
        } else {
            scale = 1 / nh;
        }

        int pivotX = mBitmap.getWidth() / 2;
        int pivotY = mBitmap.getHeight() / 2;

        mTransX = mTransY = 0;
        mRotateTranX = mRotateTranY = 0;
        this.mScale = 1;
        mRotateScale = 1;
        float touchX = toTouchX(pivotX);
        float touchY = toTouchY(pivotY);
        mRotateScale = scale / mPrivateScale;

        // 缩放后，偏移图片，以产生围绕某个点缩放的效果
        tx = toTransX(touchX, pivotX);
        ty = toTransY(touchY, pivotY);

        mRotateTranX = tx;
        mRotateTranY = ty;

        /*if (nw > nh) {
            mPrivateWidth = getWidth();
            mPrivateHeight = (int) (h * mPrivateScale);
        } else {
            mPrivateWidth = (int) (w * mPrivateScale);
            mPrivateHeight = getHeight();
        }
*/
        invalidate();
    }

    /**
     * 保存, 回调GraffitiListener.onSaved()的线程和调用save()的线程相同
     */
    @Override
    public void save() {
        for (IGraffitiItem item : mItemStack) {
            if (item instanceof GraffitiItemBase) {
                if (((GraffitiItemBase) item).isDrawOptimize()) { // 优化绘制，addItem时已经保存在图片上

                } else { // 最终保存到图片上
                    item.draw(mBitmapCanvas);
                }
            }
        }
        mGraffitiBitmap = ImageUtils.rotate(mGraffitiBitmap, mGraffitiRotateDegree, true);

        mGraffitiListener.onSaved(mGraffitiBitmap, new Runnable() {
            @Override
            public void run() {
                // 还原涂鸦图片，确保在ui线程刷新
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    invalidateForce();
                } else {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            invalidateForce();
                        }
                    });
                }
            }
        });
        notifyActionOccur(ACTION_SAVE, null);
    }

    /**
     * 清屏
     */
    @Override
    public void clear() {
        int size = mItemStack.size();
        mItemStack.clear();
        notifyActionOccur(ACTION_CLEAR, size);
        invalidateForce();
    }

    @Override
    public boolean undo(int step) {

        if (mItemStack.size() > 0) {
            step = Math.min(mItemStack.size(), step);
            IGraffitiItem item = mItemStack.get(mItemStack.size() - step);
            removeItem(item);
            notifyActionOccur(ACTION_UNDO, item);
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

    /**
     * 设置画笔底色
     *
     * @param color
     */
    @Override
    public void setColor(IGraffitiColor color) {
        mColor = color;
        invalidate();
    }

    @Override
    public IGraffitiColor getColor() {
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
    public void setPen(IGraffitiPen pen) {
        if (pen == null) {
            throw new RuntimeException("Pen can't be null");
        }
        IGraffitiPen old = mPen;
        mPen = pen;
        invalidate();
    }

    @Override
    public IGraffitiPen getPen() {
        return mPen;
    }

    /**
     * 设置画笔形状
     *
     * @param shape
     */
    @Override
    public void setShape(IGraffitiShape shape) {
        if (shape == null) {
            throw new RuntimeException("Shape can't be null");
        }
        mShape = shape;
        invalidate();
    }

    @Override
    public IGraffitiShape getShape() {
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
     * 设置图片G偏移
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
        mSize = paintSize;
        invalidate();
    }

    @Override
    public float getSize() {
        return mSize;
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

    /**
     * 设置是否开启放大镜
     *
     * @param enable
     */
    public void enableAmplifier(boolean enable) {
        mEnableAmplifier = enable;
    }

    /**
     * 是否开启放大镜
     */
    public boolean isEnableAmplifier() {
        return mEnableAmplifier;
    }

    @Override
    public void topItem(IGraffitiItem item) {
        mItemStack.remove(item);
        mItemStack.add(item);
        invalidate();
    }

    @Override
    public void bottomItem(IGraffitiItem item) {
        mItemStack.remove(item);
        mItemStack.add(0, item);
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
            throw new RuntimeException("the object Graffiti is illegal");
        }
        if (mItemStack.contains(graffitiItem)) {
            throw new RuntimeException("the item has been added");
        }
        mItemStack.add(graffitiItem);
        graffitiItem.onAdd();

        if (((GraffitiItemBase) graffitiItem).isDrawOptimize()) { // // 优化绘制
            graffitiItem.draw(mBitmapCanvas); // 提前保存到图片中
        }
        invalidate();
    }

    @Override
    public void removeItem(IGraffitiItem graffitiItem) {
        if (!mItemStack.remove(graffitiItem)) {
            return;
        }
        graffitiItem.onRemove();

        if (graffitiItem instanceof GraffitiItemBase &&
                ((GraffitiItemBase) graffitiItem).isDrawOptimize()) { // 由于优化绘制，需要重新绘制抹掉图片上的痕迹
            invalidateForce();
        }
        invalidate();
    }

    @Override
    public List<IGraffitiItem> getAllItem() {
        return mItemStack;
    }

    @Override
    public Bitmap getBitmap() {
        return mBitmap;
    }

    @Override
    public Bitmap getGraffitiBitmap() {
        return mGraffitiBitmap;
    }


    /**
     * 添加回调
     *
     * @param listener
     */
    public void addGraffitiViewListener(IGraffitiViewListener listener) {
        if (listener == null || mListenerList.contains(listener)) {
            return;
        }
        mListenerList.add(new WeakReference<>(listener));
    }

    /**
     * 移除回调
     *
     * @param listener
     */
    public void removeGraffitiViewListener(IGraffitiViewListener listener) {
        IGraffitiViewListener callBack;
        for (WeakReference<IGraffitiViewListener> ref : mListenerList) {
            callBack = ref.get();
            if (callBack == null || callBack == listener) {
                mListenerList.remove(ref);
            }
        }
    }

    private void notifyActionOccur(int action, Object obj) {
        IGraffitiViewListener callBack;
        for (WeakReference<IGraffitiViewListener> ref : mListenerList) {
            callBack = ref.get();
            if (callBack != null) {
                callBack.onActionOccur(action, obj);
            } else { // 刪除无用的引用
                mListenerList.remove(ref);
            }
        }
    }

    /**
     * 监听涂鸦中的事件
     */
    public interface IGraffitiViewListener {

        /**
         * 操作发生后回调
         *
         * @param action
         * @param obj    动作相关的对象信息
         */
        public void onActionOccur(int action, Object obj);
    }

    private class GraffitiViewInner extends View {

        public GraffitiViewInner(Context context) {
            super(context);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            return super.dispatchTouchEvent(event);
        }


        @Override
        public boolean onTouchEvent(MotionEvent event) {
            // 綁定的识别器
            IGraffitiTouchDetector detector = mTouchDetectorMap.get(mPen);
            if (detector != null) {
                return detector.onTouchEvent(event);
            }
            // 默认识别器
            if (mDefaultTouchDetector != null) {
                return mDefaultTouchDetector.onTouchEvent(event);
            }
            return super.onTouchEvent(event);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.save();
            doDraw(canvas);
            canvas.restore();
        }
    }
}
