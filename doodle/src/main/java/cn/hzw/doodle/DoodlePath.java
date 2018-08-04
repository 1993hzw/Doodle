package cn.hzw.doodle;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

import cn.hzw.doodle.core.IDoodle;
import cn.hzw.doodle.util.DrawUtil;

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
        super(doodle, 0, 0, 0);// 这里默认item旋转角度为0
    }

    public DoodlePath(IDoodle doodle, DoodlePaintAttrs attrs) {
        super(doodle, attrs, 0, 0, 0);
    }

    public void updateXY(float sx, float sy, float dx, float dy) {
        mSxy.set(sx, sy);
        mDxy.set(dx, dy);
        if (mPath == null) {
            mPath = new Path();
        }
        mPath.reset();

        if (DoodleShape.ARROW.equals(getShape())) {
            updateArrowPath(mPath, mSxy.x, mSxy.y, mDxy.x, mDxy.y, getSize());
        } else if (DoodleShape.LINE.equals(getShape())) {
            updateLinePath(mPath, mSxy.x, mSxy.y, mDxy.x, mDxy.y, getSize());
        } else if (DoodleShape.FILL_CIRCLE.equals(getShape()) || DoodleShape.HOLLOW_CIRCLE.equals(getShape())) {
            updateCirclePath(mPath, mSxy.x, mSxy.y, mDxy.x, mDxy.y, getSize());
        } else if (DoodleShape.FILL_RECT.equals(getShape()) || DoodleShape.HOLLOW_RECT.equals(getShape())) {
            updateRectPath(mPath, mSxy.x, mSxy.y, mDxy.x, mDxy.y, getSize());
        }
        // 改变中心点位置
        mPath.computeBounds(mBound, false);
        setPivotX(mBound.left + mBound.width() / 2);
        setPivotY(mBound.top + mBound.height() / 2);
    }

    public void updatePath(Path path) {
        this.mPath = path;
        if (mPath != null) {
            // 改变中心点位置
            mPath.computeBounds(mBound, false);
            setPivotX(mBound.left + mBound.width() / 2);
            setPivotY(mBound.top + mBound.height() / 2);
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

    private PointF getDxy() {
        return mDxy;
    }

    private PointF getSxy() {
        return mSxy;
    }

    public static DoodlePath toShape(IDoodle doodle, float sx, float sy, float dx, float dy) {
        DoodlePath path = new DoodlePath(doodle);
        path.setPen(doodle.getPen().copy());
        path.setShape(doodle.getShape().copy());
        path.setSize(doodle.getSize());
        path.setColor(doodle.getColor().copy());

        path.updateXY(sx, sy, dx, dy);
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

        path.updatePath(p);
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
        if (mPath != null) {
            int diff = (int) (getSize() / 2);
            mPath.computeBounds(mBound, false);
            if (getShape() == DoodleShape.ARROW || getShape() == DoodleShape.FILL_CIRCLE || getShape() == DoodleShape.FILL_RECT) {
                diff = (int) getDoodle().getUnitSize();
            }
            rect.set((int) (mBound.left - diff), (int) (mBound.top - diff), (int) (mBound.right + diff), (int) (mBound.bottom + diff));
        }
    }


    //---------计算Path
    private Path mArrowTrianglePath;

    private void updateArrowPath(Path path, float sx, float sy, float ex, float ey, float size) {
        float arrowSize = size;
        double H = arrowSize; // 箭头高度
        double L = arrowSize / 2; // 底边的一�?

        double awrad = Math.atan(L / 2 / H); // 箭头角度
        double arraow_len = Math.sqrt(L / 2 * L / 2 + H * H) - 5; // 箭头的长�?
        double[] arrXY_1 = DrawUtil.rotateVec(ex - sx, ey - sy, awrad, true, arraow_len);
        double[] arrXY_2 = DrawUtil.rotateVec(ex - sx, ey - sy, -awrad, true, arraow_len);
        float x_3 = (float) (ex - arrXY_1[0]); // (x3,y3)是第�?端点
        float y_3 = (float) (ey - arrXY_1[1]);
        float x_4 = (float) (ex - arrXY_2[0]); // (x4,y4)是第二端�?
        float y_4 = (float) (ey - arrXY_2[1]);
        // 画线
        path.moveTo(sx, sy);
        path.lineTo(x_3, y_3);
        path.lineTo(x_4, y_4);
        path.close();

        awrad = Math.atan(L / H); // 箭头角度
        arraow_len = Math.sqrt(L * L + H * H); // 箭头的长�?
        arrXY_1 = DrawUtil.rotateVec(ex - sx, ey - sy, awrad, true, arraow_len);
        arrXY_2 = DrawUtil.rotateVec(ex - sx, ey - sy, -awrad, true, arraow_len);
        x_3 = (float) (ex - arrXY_1[0]); // (x3,y3)是第�?端点
        y_3 = (float) (ey - arrXY_1[1]);
        x_4 = (float) (ex - arrXY_2[0]); // (x4,y4)是第二端�?
        y_4 = (float) (ey - arrXY_2[1]);
        if (mArrowTrianglePath == null) {
            mArrowTrianglePath = new Path();
        }
        mArrowTrianglePath.reset();
        mArrowTrianglePath.moveTo(ex, ey);
        mArrowTrianglePath.lineTo(x_4, y_4);
        mArrowTrianglePath.lineTo(x_3, y_3);
        mArrowTrianglePath.close();
        path.addPath(mArrowTrianglePath);
    }

    private void updateLinePath(Path path, float sx, float sy, float ex, float ey, float size) {
        path.moveTo(sx, sy);
        path.lineTo(ex, ey);
    }

    private void updateCirclePath(Path path, float sx, float sy, float dx, float dy, float size) {
        float radius = (float) Math.sqrt((sx - dx) * (sx - dx) + (sy - dy) * (sy - dy));
        path.addCircle(sx, sy, radius, Path.Direction.CCW);

    }

    private void updateRectPath(Path path, float sx, float sy, float dx, float dy, float size) {
        // 保证　左上角　与　右下角　的对应关系
        if (sx < dx) {
            if (sy < dy) {
                path.addRect(sx, sy, dx, dy, Path.Direction.CCW);
            } else {
                path.addRect(sx, dy, dx, sy, Path.Direction.CCW);
            }
        } else {
            if (sy < dy) {
                path.addRect(dx, sy, sx, dy, Path.Direction.CCW);
            } else {
                path.addRect(dx, dy, sx, sy, Path.Direction.CCW);
            }
        }
    }
}

