package cn.hzw.graffiti;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import static cn.hzw.graffiti.util.DrawUtil.drawArrow;
import static cn.hzw.graffiti.util.DrawUtil.drawCircle;
import static cn.hzw.graffiti.util.DrawUtil.drawLine;
import static cn.hzw.graffiti.util.DrawUtil.drawRect;
import static cn.hzw.graffiti.util.DrawUtil.rotatePointInGraffiti;

/**
 * Created by huangziwei on 2017/3/16.
 */

public class GraffitiPath extends GraffitiItemBase {
    private Path mPath; // 画笔的路径

    private float mSx, mSy; // 映射后的起始坐标，（手指点击）
    private float mDx, mDy; // 映射后的终止坐标，（手指抬起）

    private Paint mPaint = new Paint();
    private PointF mLocationTemp = new PointF();
    private PointF mLocationTemp2 = new PointF();

    public GraffitiPath(IGraffiti graffiti) {
        super(graffiti);
    }

    public GraffitiPath(IGraffiti graffiti, GraffitiPaintAttrs attrs) {
        super(graffiti, attrs);
    }


    private PointF getSxSy(int currentDegree) {
        return rotatePointInGraffiti(mLocationTemp, currentDegree, getGraffitiRotate(), mSx, mSy, getOriginalPivotX(), getOriginalPivotY());
    }

    private PointF getDxDy(int currentDegree) {
        return rotatePointInGraffiti(mLocationTemp2, currentDegree, getGraffitiRotate(), mDx, mDy, getOriginalPivotX(), getOriginalPivotY());
    }

    public void reset(IGraffiti graffiti, float sx, float sy, float dx, float dy) {
        setGraffiti(graffiti);
        setPen(graffiti.getPen());
        setShape(graffiti.getShape());
        setSize(graffiti.getSize());
        setColor(graffiti.getColor());

        this.mSx = sx;
        this.mSy = sy;
        this.mDx = dx;
        this.mDy = dy;
    }

    public void reset(IGraffiti graffiti, Path p) {
        setGraffiti(graffiti);
        setPen(graffiti.getPen());
        setShape(graffiti.getShape());
        setSize(graffiti.getSize());
        setColor(graffiti.getColor());

        this.mPath = p;
    }


    public static GraffitiPath toShape(IGraffiti graffiti, float sx, float sy, float dx, float dy) {
        GraffitiPath path = new GraffitiPath(graffiti);
        path.setPen(graffiti.getPen());
        path.setShape(graffiti.getShape());
        path.setSize(graffiti.getSize());
        path.setColor(graffiti.getColor());

        path.mSx = sx;
        path.mSy = sy;
        path.mDx = dx;
        path.mDy = dy;
        return path;
    }

    public static GraffitiPath toPath(IGraffiti graffiti, Path p) {
        GraffitiPath path = new GraffitiPath(graffiti);
        path.setPen(graffiti.getPen());
        path.setShape(graffiti.getShape());
        path.setSize(graffiti.getSize());
        path.setColor(graffiti.getColor());

        path.mPath = p;
        return path;
    }

    @Override
    protected void doDraw(Canvas canvas) {
        mPaint.setStrokeWidth(getSize());
        mPaint.setStyle(Paint.Style.STROKE);
        getColor().initColor(mPaint, null);

        if (getShape() == IGraffiti.Shape.HAND_WRITE) { // 手写
            canvas.drawPath(mPath, mPaint);
        } else { // 画图形
            mLocationTemp = getSxSy(getGraffiti().getRotate());
            mLocationTemp2 = getDxDy(getGraffiti().getRotate());
            draw(canvas, mPaint, getShape(), mLocationTemp.x, mLocationTemp.y, mLocationTemp2.x, mLocationTemp2.y);
        }
    }

    private void draw(Canvas canvas, Paint paint, IGraffiti.Shape shape, float sx, float sy, float dx, float dy) {
        switch (shape) { // 绘制图形
            case ARROW:
                paint.setStyle(Paint.Style.FILL);
                drawArrow(canvas, sx, sy, dx, dy, paint);
                break;
            case LINE:
                drawLine(canvas, sx, sy, dx, dy, paint);
                break;
            case FILL_CIRCLE:
                paint.setStyle(Paint.Style.FILL);
            case HOLLOW_CIRCLE:
                drawCircle(canvas, sx, sy,
                        (float) Math.sqrt((sx - dx) * (sx - dx) + (sy - dy) * (sy - dy)), paint);
                break;
            case FILL_RECT:
                paint.setStyle(Paint.Style.FILL);
            case HOLLOW_RECT:
                drawRect(canvas, sx, sy, dx, dy, paint);
                break;
            default:
                throw new RuntimeException("unknown shape:" + shape);
        }
    }
}

