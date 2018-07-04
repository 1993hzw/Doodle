package cn.hzw.graffiti;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.util.List;

import cn.forward.androids.TouchGestureDetector;

import static cn.hzw.graffiti.util.DrawUtil.computeAngle;

/**
 * GraffitiView的涂鸦手势监听
 * Created on 30/06/2018.
 */

public class GraffitiOnTouchGestureListener extends TouchGestureDetector.OnTouchGestureListener {
    private static final float VALUE = 1f;

    // 触摸的相关信息
    private float mTouchX, mTouchY;
    private float mLastTouchX, mLastTouchY;
    private float mTouchDownX, mTouchDownY;

    // 缩放相关
    private Float mLastFocusX;
    private Float mLastFocusY;
    private float mTouchCentreX, mTouchCentreY;


    private float mSelectedItemX, mSelectedItemY;
    private float mRotateTextDiff; // 开始旋转图片时的差值（当前图片与触摸点的角度）

    private Path mCurrPath; // 当前手写的路径
    private GraffitiPath mCurrGraffitiPath;
    private CopyLocation mCopyLocation;

    private GraffitiView mGraffiti;

    private ValueAnimator mScaleAnimator ;
    private float mAnimTransX, mAnimTranY;
    private boolean mEnableAmplifier;

    public GraffitiOnTouchGestureListener(GraffitiView graffiti) {
        mGraffiti = graffiti;
        mCopyLocation = mGraffiti.getCopyLocation();
    }

    @Override
    public boolean onDown(MotionEvent e) {
        mTouchX = mTouchDownX = e.getX();
        mTouchY = mTouchDownY = e.getY();
        mGraffiti.enableAmplifier(false); // 关闭放大镜
        return true;
    }

    /**
     * 开始滚动
     *
     * @param event
     */
    @Override
    public void onScrollBegin(MotionEvent event) {
        mLastTouchX = mTouchX = event.getX();
        mLastTouchY = mTouchY = event.getY();

        if (mGraffiti.getPen().isSelectable()) {
            // 判断是否点中选择区域
            if (mGraffiti.getSelectedItem() != null) {
                PointF xy = mGraffiti.getSelectedItem().getLocation();
                mSelectedItemX = xy.x;
                mSelectedItemY = xy.y;
                // 旋转
                if (mGraffiti.getSelectedItem().isCanRotate(mGraffiti.toX(mTouchX), mGraffiti.toY(mTouchY))) {
                    mGraffiti.getSelectedItem().setIsRotating(true);
                    mRotateTextDiff = mGraffiti.getSelectedItem().getItemRotate() -
                            computeAngle(xy.x, xy.y, mGraffiti.toX(mTouchX), mGraffiti.toY(mTouchY));
                }
            }
        } else {
            mGraffiti.enableAmplifier(true); // 涂鸦时开启放大镜
            // 点击copy
            if (mGraffiti.getPen() == IGraffiti.Pen.COPY && mCopyLocation.isInIt(mGraffiti.toX(mTouchX), mGraffiti.toY(mTouchY), mGraffiti.getSize())) {
                mCopyLocation.setRelocating(true);
                mCopyLocation.setCopying(false);
            } else {
                if (mGraffiti.getPen() == IGraffiti.Pen.COPY) {
                    if (!mCopyLocation.isCopying()) {
                        mCopyLocation.setStartPosition(mGraffiti.toX(mTouchX), mGraffiti.toY(mTouchY));
                    }
                    mCopyLocation.setCopying(true);
                }
                mCopyLocation.setRelocating(false);

                // 初始化绘制
                mCurrPath = new Path();
                mCurrPath.moveTo(mGraffiti.toX(mTouchX), mGraffiti.toY(mTouchY));
                if (mGraffiti.getShape() == IGraffiti.Shape.HAND_WRITE) { // 手写
                    mCurrGraffitiPath = GraffitiPath.toPath(mGraffiti, mCurrPath);
                } else {  // 画图形
                    mCurrGraffitiPath = GraffitiPath.toShape(mGraffiti,
                            mGraffiti.toX(mTouchDownX), mGraffiti.toY(mTouchDownY), mGraffiti.toX(mTouchX), mGraffiti.toY(mTouchY));
                }
                mCurrGraffitiPath.setDrawOptimize(false);
                mGraffiti.addItem(mCurrGraffitiPath);
            }
        }
        mGraffiti.invalidate();
    }

    @Override
    public void onScrollEnd(MotionEvent e) {
        mLastTouchX = mTouchX;
        mLastTouchY = mTouchY;
        mTouchX = e.getX();
        mTouchY = e.getY();

        if (mGraffiti.getPen().isSelectable()) {
            mGraffiti.getSelectedItem().setIsRotating(false);
        } else {
            mCurrGraffitiPath.setDrawOptimize(true);
            mGraffiti.invalidate(mCurrGraffitiPath);
        }
        mGraffiti.enableAmplifier(false); // 关闭放大镜
        mGraffiti.invalidate();
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        mLastTouchX = mTouchX;
        mLastTouchY = mTouchY;
        mTouchX = e2.getX();
        mTouchY = e2.getY();

        if (mGraffiti.getPen().isSelectable()) { //画笔是否是可选择的
            if (mGraffiti.getSelectedItem() != null) {
                if (mGraffiti.getSelectedItem().isRotating()) { // 旋转item
                    PointF xy = mGraffiti.getSelectedItem().getLocation();
                    mGraffiti.getSelectedItem().setItemRotate(mRotateTextDiff + computeAngle(
                            xy.x, xy.y, mGraffiti.toX(mTouchX), mGraffiti.toY(mTouchY)
                    ));
                } else { // 移动item
                    mGraffiti.getSelectedItem().setLocation(
                            mSelectedItemX + mGraffiti.toX(mTouchX) - mGraffiti.toX(mTouchDownX),
                            mSelectedItemY + mGraffiti.toY(mTouchY) - mGraffiti.toY(mTouchDownY));
                }
            }
        } else {
            if (mGraffiti.getPen() == IGraffiti.Pen.COPY && mCopyLocation.isRelocating()) {
                // 正在定位location
                mCopyLocation.updateLocation(mGraffiti.toX(mTouchX), mGraffiti.toY(mTouchY));
            } else {
                if (mGraffiti.getPen() == IGraffiti.Pen.COPY) {
                    mCopyLocation.updateLocation(mCopyLocation.getCopyStartX() + mGraffiti.toX(mTouchX) - mCopyLocation.getTouchStartX(),
                            mCopyLocation.getCopyStartY() + mGraffiti.toY(mTouchY) - mCopyLocation.getTouchStartY());
                }
                if (mGraffiti.getShape() == IGraffiti.Shape.HAND_WRITE) { // 手写
                    mCurrPath.quadTo(
                            mGraffiti.toX(mLastTouchX),
                            mGraffiti.toY(mLastTouchY),
                            mGraffiti.toX((mTouchX + mLastTouchX) / 2),
                            mGraffiti.toY((mTouchY + mLastTouchY) / 2));
                    mCurrGraffitiPath.reset(mGraffiti, mCurrPath);
                } else {  // 画图形
                    mCurrGraffitiPath.reset(mGraffiti, mGraffiti.toX(mTouchDownX), mGraffiti.toY(mTouchDownY), mGraffiti.toX(mTouchX), mGraffiti.toY(mTouchY));
                }
            }
        }
        mGraffiti.invalidate();
        return true;
    }


    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        mLastTouchX = mTouchX;
        mLastTouchY = mTouchY;
        mTouchX = e.getX();
        mTouchY = e.getY();
        // 为了仅点击时也能出现绘图，必须移动path
        if (mTouchDownX == mTouchX && mTouchDownY == mTouchY & mTouchDownX == mLastTouchX && mTouchDownY == mLastTouchY) {
            mTouchX += VALUE;
            mTouchY += VALUE;
        }

        if (mGraffiti.getPen().isSelectable()) {
            boolean found = false;
            IGraffitiSelectableItem item;
            List<IGraffitiItem> items = mGraffiti.getAllItem();
            for (int i = items.size() - 1; i >= 0; i--) {
                IGraffitiItem elem = items.get(i);
                if (!(elem instanceof IGraffitiSelectableItem)) {
                    continue;
                }
                item = (IGraffitiSelectableItem) elem;

                if (item.isInIt(mGraffiti.toX(mTouchX), mGraffiti.toY(mTouchY))) {
                    found = true;
                    mGraffiti.setSelectedItem(item);
                    PointF xy = item.getLocation();
                    mSelectedItemX = xy.x;
                    mSelectedItemY = xy.y;
                    mGraffiti.getGraffitiListener().onSelectedItem(mGraffiti.getSelectedItem(), true);
                    break;
                }
            }
            if (!found) {
                if (mGraffiti.getSelectedItem() != null) { // 取消选定
                    IGraffitiSelectableItem old = mGraffiti.getSelectedItem();
                    mGraffiti.setSelectedItem(null);
                    mGraffiti.getGraffitiListener().onSelectedItem(old, false);
                } else {
                    mGraffiti.getGraffitiListener().onCreateSelectableItem(mGraffiti.getPen(), mGraffiti.toX(mTouchX), mGraffiti.toY(mTouchY));
                }
            }
        } else {

        }
        mGraffiti.invalidate();
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        mLastFocusX = null;
        mLastFocusY = null;
        mGraffiti.enableAmplifier(false);
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
                mGraffiti.setTransX(mGraffiti.getTransX() + dx);
                mGraffiti.setTransY(mGraffiti.getTransY() + dy);
            }
        }

        if (detector.getScaleFactor() > 0.1f) {
            // 缩放图片
            float scale = mGraffiti.getScale() * detector.getScaleFactor();
            mGraffiti.setScale(scale, mGraffiti.toX(mTouchCentreX), mGraffiti.toY(mTouchCentreY));
        }

        mLastFocusX = mTouchCentreX;
        mLastFocusY = mTouchCentreY;

        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        if(mGraffiti.getScale()<1){
            if(mScaleAnimator==null) {
                mScaleAnimator = new ValueAnimator();
                mScaleAnimator.setDuration(100);
                mScaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (float) animation.getAnimatedValue();
                        float fraction = animation.getAnimatedFraction();
                        mGraffiti.setScale(value, mGraffiti.toX(mTouchCentreX), mGraffiti.toY(mTouchCentreY));
                        mGraffiti.setTrans(mAnimTransX*(1-fraction),mAnimTranY*(1-fraction));
                    }
                });
            }
            mScaleAnimator.cancel();
            mAnimTransX = mGraffiti.getTransX();
            mAnimTranY = mGraffiti.getTransY();
            mScaleAnimator.setFloatValues(mGraffiti.getScale(),1);
            mScaleAnimator.start();
        }
    }

}
