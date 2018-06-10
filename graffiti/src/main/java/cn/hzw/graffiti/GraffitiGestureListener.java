package cn.hzw.graffiti;

import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Created on 06/06/2018.
 */

public class GraffitiGestureListener extends TouchGestureDetector.OnTouchGestureListener {

    private GraffitiView mGraffitiView;

    private Float mLastFocusX;
    private Float mLastFocusY;
    // 手势操作相关
    private float mToucheCentreXOnGraffiti, mToucheCentreYOnGraffiti, mTouchCentreX, mTouchCentreY;

    private boolean mIsScaling = false; // 是否正在缩放

    public GraffitiGestureListener(GraffitiView graffitiView) {
        mGraffitiView = graffitiView;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//        mGraffitiView.setTrans(mGraffitiView.getTransX() - distanceX, mGraffitiView.getTransY() - distanceY);
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        mLastFocusX = null;
        mLastFocusY = null;
        Log.i("test", "onScaleBegin");
        mIsScaling = true;
        return true;
    }

    // 手势缩放
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        Log.i("test", "onScale:"+mLastFocusX);
        // 屏幕上的焦点
        mTouchCentreX = detector.getFocusX();
        mTouchCentreY = detector.getFocusY();
        // 对应的图片上的焦点
        mToucheCentreXOnGraffiti = mGraffitiView.toX(mTouchCentreX);
        mToucheCentreYOnGraffiti = mGraffitiView.toY(mTouchCentreY);

        if (mLastFocusX != null && mLastFocusY != null) { // 焦点改变
            final float dx = mTouchCentreX - mLastFocusX;
            final float dy = mTouchCentreY - mLastFocusY;
            // 移动图片
            mGraffitiView.setTrans(mGraffitiView.getTransX() + dx, mGraffitiView.getTransY() + dy);
        }

        // 缩放图片
        float scale = mGraffitiView.getScale() * detector.getScaleFactor();
        mGraffitiView.setScale(scale, mToucheCentreXOnGraffiti, mToucheCentreYOnGraffiti);

        mLastFocusX = mTouchCentreX;
        mLastFocusY = mTouchCentreY;

        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        super.onScaleEnd(detector);
        mIsScaling = false;
    }

    public boolean isScaling() {
        return mIsScaling;
    }
}
