package cn.hzw.graffiti;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import cn.hzw.graffiti.core.IGraffiti;

/**
 * 涂鸦轨迹
 * Created by huangziwei on 2017/3/16.
 */

public class GraffitiPath extends GraffitiItemBase {
    private Path mPath; // 画笔的路径

    private PointF mSxy = new PointF(); // 映射后的起始坐标，（手指点击）
    private PointF mDxy = new PointF(); // 映射后的终止坐标，（手指抬起）

    private Paint mPaint = new Paint();

    private CopyLocation mCopyLocation;

    public GraffitiPath(IGraffiti graffiti) {
        super(graffiti);
    }

    public GraffitiPath(IGraffiti graffiti, GraffitiPaintAttrs attrs) {
        super(graffiti, attrs);
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

    public static GraffitiPath toShape(IGraffiti graffiti, float sx, float sy, float dx, float dy) {
        GraffitiPath path = new GraffitiPath(graffiti);
        path.setPen(graffiti.getPen().copy());
        path.setShape(graffiti.getShape().copy());
        path.setSize(graffiti.getSize());
        path.setColor(graffiti.getColor().copy());

        path.mSxy.set(sx, sy);
        path.mDxy.set(dx, dy);
        if(path.getPen()==GraffitiPen.COPY) {
            if (graffiti instanceof GraffitiView) {
                path.mCopyLocation = GraffitiPen.COPY.getCopyLocation().copy();
            }
        }
        return path;
    }

    public static GraffitiPath toPath(IGraffiti graffiti, Path p) {
        GraffitiPath path = new GraffitiPath(graffiti);
        path.setPen(graffiti.getPen().copy());
        path.setShape(graffiti.getShape().copy());
        path.setSize(graffiti.getSize());
        path.setColor(graffiti.getColor().copy());

        path.mPath = p;
        if (graffiti instanceof GraffitiView) {
            path.mCopyLocation = GraffitiPen.COPY.getCopyLocation().copy();
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

