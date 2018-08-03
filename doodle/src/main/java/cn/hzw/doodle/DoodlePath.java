package cn.hzw.doodle;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

import cn.hzw.doodle.core.IDoodle;

import static cn.hzw.doodle.DoodleShape.ARROW;
import static cn.hzw.doodle.DoodleShape.FILL_CIRCLE;
import static cn.hzw.doodle.DoodleShape.FILL_RECT;
import static cn.hzw.doodle.DoodleShape.HOLLOW_CIRCLE;
import static cn.hzw.doodle.DoodleShape.HOLLOW_RECT;
import static cn.hzw.doodle.DoodleShape.LINE;
import static cn.hzw.doodle.util.DrawUtil.drawArrow;
import static cn.hzw.doodle.util.DrawUtil.drawCircle;
import static cn.hzw.doodle.util.DrawUtil.drawLine;
import static cn.hzw.doodle.util.DrawUtil.drawRect;

/**
 * 涂鸦轨迹
 * Created by huangziwei on 2017/3/16.
 */

public class DoodlePath extends DoodleRotatableItemBase {
    private Path mPath; // 画笔的路径

    private PointF mSxy = new PointF(); // 映射后的起始坐标，（手指点击）
    private PointF mDxy = new PointF(); // 映射后的终止坐标，（手指抬起）

    private Paint mPaint = new Paint();

    private CopyLocation mCopyLocation;

    public DoodlePath(IDoodle doodle) {
        super(doodle, -doodle.getDoodleRotation(), 0, 0);
    }

    public DoodlePath(IDoodle doodle, DoodlePaintAttrs attrs) {
        super(doodle, attrs, -doodle.getDoodleRotation(), 0, 0);
    }

    public void updateXY(float sx, float sy, float dx, float dy) {
        mSxy.set(sx, sy);
        mDxy.set(dx, dy);
    }

    public void updatePath(Path path) {
        this.mPath = path;
        if (mPath != null) {
            mPath.computeBounds(mBound, false);
            setPivotX(mBound.left);
            setPivotY(mBound.top);
        }
    }

    public void updateCopy(float touchStartX, float touchStartY, float copyStartX, float copyStartY) {
        if (mCopyLocation == null) {
            return;
        }
        mCopyLocation.setStartPosition(touchStartX, touchStartY, copyStartX, copyStartY);
    }

    public CopyLocation getCopyLocation() {
        return mCopyLocation;
    }

    public Path getPath() {
        return mPath;
    }

    public PointF getDxy() {
        return mDxy;
    }

    public PointF getSxy() {
        return mSxy;
    }

    public static DoodlePath toShape(IDoodle doodle, float sx, float sy, float dx, float dy) {
        DoodlePath path = new DoodlePath(doodle);
        path.setPen(doodle.getPen().copy());
        path.setShape(doodle.getShape().copy());
        path.setSize(doodle.getSize());
        path.setColor(doodle.getColor().copy());

        path.mSxy.set(sx, sy);
        path.mDxy.set(dx, dy);
        if (path.getPen() == DoodlePen.COPY) {
            if (doodle instanceof DoodleView) {
                path.mCopyLocation = DoodlePen.COPY.getCopyLocation().copy();
            }
        }
        return path;
    }

    public static DoodlePath toPath(IDoodle doodle, Path p) {
        DoodlePath path = new DoodlePath(doodle);
        path.setPen(doodle.getPen().copy());
        path.setShape(doodle.getShape().copy());
        path.setSize(doodle.getSize());
        path.setColor(doodle.getColor().copy());

        path.mPath = p;
        if (doodle instanceof DoodleView) {
            path.mCopyLocation = DoodlePen.COPY.getCopyLocation().copy();
        } else {
            path.mCopyLocation = null;
        }
        return path;
    }

    @Override
    protected void doDraw(Canvas canvas) {
        mPaint.reset();
        mPaint.setStrokeWidth(getSize());
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);

        getPen().config(this, mPaint);
        getColor().config(this, mPaint);
        getShape().draw(canvas, this, mPaint);
    }

    private RectF mBound = new RectF();

    @Override
    protected void resetBounds(Rect rect) {
        int diff = (int) (getSize() / 2);
        if (mPath != null) {
            mPath.computeBounds(mBound, false);
            rect.set((int) (mBound.left - diff), (int) (mBound.top - diff), (int) (mBound.right + diff), (int) (mBound.bottom + diff));
        } else {
            if (DoodleShape.ARROW.equals(getShape())) {
            } else if (DoodleShape.LINE.equals(getShape())) {
            } else if (DoodleShape.FILL_CIRCLE.equals(getShape()) || DoodleShape.HOLLOW_CIRCLE.equals(getShape())) {
                float radius = (float) Math.sqrt((mSxy.x - mDxy.x) * (mSxy.y - mDxy.y) + (mSxy.x - mDxy.x) * (mSxy.y - mDxy.y));
                rect.set((int) (mSxy.x - radius - diff), (int) (mSxy.y - radius - diff), (int) (mSxy.x + radius + diff), (int) (mSxy.y + radius + diff));
            } else if (DoodleShape.FILL_RECT.equals(getShape()) || DoodleShape.HOLLOW_RECT.equals(getShape())) {
                // 保证　左上角　与　右下角　的对应关系
                if (mSxy.x < mDxy.x) {
                    if (mSxy.y < mDxy.y) {
                        rect.set((int) mSxy.x, (int) mSxy.y, (int) mDxy.x, (int) mDxy.y);
                    } else {
                        rect.set((int) mSxy.x, (int) mDxy.y, (int) mDxy.x, (int) mSxy.y);
                    }
                } else {
                    if (mSxy.y < mDxy.y) {
                        rect.set((int) mDxy.x, (int) mSxy.y, (int) mSxy.x, (int) mDxy.y);
                    } else {
                        rect.set((int) mDxy.x, (int) mDxy.y, (int) mSxy.x, (int) mSxy.y);
                    }
                }
                rect.set(rect.left - diff, rect.top - diff, rect.right + diff, rect.bottom + diff);
            }
        }
    }
}

