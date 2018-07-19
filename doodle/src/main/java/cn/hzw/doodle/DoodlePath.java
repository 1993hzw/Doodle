package cn.hzw.doodle;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import cn.hzw.doodle.core.IDoodle;

/**
 * 涂鸦轨迹
 * Created by huangziwei on 2017/3/16.
 */

public class DoodlePath extends DoodleItemBase {
    private Path mPath; // 画笔的路径

    private PointF mSxy = new PointF(); // 映射后的起始坐标，（手指点击）
    private PointF mDxy = new PointF(); // 映射后的终止坐标，（手指抬起）

    private Paint mPaint = new Paint();

    private CopyLocation mCopyLocation;

    public DoodlePath(IDoodle doodle) {
        super(doodle);
    }

    public DoodlePath(IDoodle doodle, DoodlePaintAttrs attrs) {
        super(doodle, attrs);
    }

    public void updateXY(float sx, float sy, float dx, float dy) {
        mSxy.set(sx, sy);
        mDxy.set(dx, dy);
    }

    public void updatePath(Path path) {
        this.mPath = path;
    }

    public void updateCopy(float touchStartX, float touchStartY, float copyStartX, float copyStartY){
        if(mCopyLocation==null){
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
        if(path.getPen()==DoodlePen.COPY) {
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


}

