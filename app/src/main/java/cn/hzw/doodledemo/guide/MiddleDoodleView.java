package cn.hzw.doodledemo.guide;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import cn.forward.androids.TouchGestureDetector;

/**
 * 中级涂鸦
 * 单击时可以选择某个涂鸦，进行移动
 * Created on 24/06/2018.
 */
public class MiddleDoodleView extends View {

    private final static String TAG = "MiddleDoodleView";

    private Paint mPaint = new Paint();
    private List<PathItem> mPathList = new ArrayList<>(); // 保存涂鸦轨迹的集合
    private TouchGestureDetector mTouchGestureDetector; // 触摸手势监听
    private float mLastX, mLastY;
    private PathItem mCurrentPathItem; // 当前的涂鸦轨迹
    private PathItem mSelectedPathItem; // 选中的涂鸦轨迹

    public MiddleDoodleView(Context context) {
        super(context);
        // 设置画笔
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(20);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        // 由手势识别器处理手势
        mTouchGestureDetector = new TouchGestureDetector(getContext(), new TouchGestureDetector.OnTouchGestureListener() {

            RectF mRectF = new RectF();

            @Override
            public boolean onSingleTapUp(MotionEvent e) { // 单击选中
                boolean found = false;
                for (PathItem path : mPathList) { // 绘制涂鸦轨迹
                    path.mPath.computeBounds(mRectF, true); // 计算涂鸦轨迹的矩形范围
                    mRectF.offset(path.mX, path.mY); // 加上偏移
                    if (mRectF.contains(e.getX(), e.getY())) { // 判断是否点中涂鸦轨迹的矩形范围内
                        found = true;
                        mSelectedPathItem = path;
                        break;
                    }
                }
                if (!found) { // 没有点中任何涂鸦
                    mSelectedPathItem = null;
                }
                invalidate();
                return true;
            }

            @Override
            public void onScrollBegin(MotionEvent e) { // 滑动开始
                Log.d(TAG, "onScrollBegin: ");
                if (mSelectedPathItem == null) {
                    mCurrentPathItem = new PathItem(); // 新的涂鸦
                    mPathList.add(mCurrentPathItem); // 添加的集合中
                    mCurrentPathItem.mPath.moveTo(e.getX(), e.getY());
                    mLastX = e.getX();
                    mLastY = e.getY();
                }
                invalidate(); // 刷新
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { // 滑动中
                Log.d(TAG, "onScroll: " + e2.getX() + " " + e2.getY());
                if (mSelectedPathItem == null) { // 没有选中的涂鸦
                    mCurrentPathItem.mPath.quadTo(
                            mLastX,
                            mLastY,
                            (e2.getX() + mLastX) / 2,
                            (e2.getY() + mLastY) / 2); // 使用贝塞尔曲线 让涂鸦轨迹更圆滑
                    mLastX = e2.getX();
                    mLastY = e2.getY();
                } else { // 移动选中的涂鸦
                    mSelectedPathItem.mX = mSelectedPathItem.mX - distanceX;
                    mSelectedPathItem.mY = mSelectedPathItem.mY - distanceY;
                }
                invalidate(); // 刷新
                return true;
            }

            @Override
            public void onScrollEnd(MotionEvent e) { // 滑动结束
                Log.d(TAG, "onScrollEnd: ");
                if (mSelectedPathItem == null) {
                    mCurrentPathItem.mPath.quadTo(
                            mLastX,
                            mLastY,
                            (e.getX() + mLastX) / 2,
                            (e.getY() + mLastY) / 2); // 使用贝塞尔曲线 让涂鸦轨迹更圆滑
                    mCurrentPathItem = null; // 轨迹结束
                }
                invalidate(); // 刷新
            }

        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean consumed = mTouchGestureDetector.onTouchEvent(event); // 由手势识别器处理手势
        if (!consumed) {
            return super.dispatchTouchEvent(event);
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (PathItem path : mPathList) { // 绘制涂鸦轨迹
            canvas.save(); // 1.保存画布状态，下面要变换画布
            canvas.translate(path.mX, path.mY); // 根据涂鸦轨迹偏移值，偏移画布使其画在对应位置上
            if (mSelectedPathItem == path) {
                mPaint.setColor(Color.YELLOW); // 点中的为黄色
            } else {
                mPaint.setColor(Color.RED); // 其他为红色
            }
            canvas.drawPath(path.mPath, mPaint);
            canvas.restore(); // 2.恢复画布状态，绘制完一个涂鸦轨迹后取消上面的画布变换，不影响下一个
        }
    }

    /**
     * 封装涂鸦轨迹对象
     */
    private static class PathItem {
        Path mPath = new Path(); // 涂鸦轨迹
        float mX, mY; // 轨迹偏移值
    }
}
