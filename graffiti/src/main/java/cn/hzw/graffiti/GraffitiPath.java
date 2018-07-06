package cn.hzw.graffiti;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import cn.hzw.graffiti.core.IGraffiti;
import cn.hzw.graffiti.util.DrawUtil;

import static cn.hzw.graffiti.util.DrawUtil.drawArrow;
import static cn.hzw.graffiti.util.DrawUtil.drawCircle;
import static cn.hzw.graffiti.util.DrawUtil.drawLine;
import static cn.hzw.graffiti.util.DrawUtil.drawRect;

/**
 * 涂鸦轨迹
 * Created by huangziwei on 2017/3/16.
 */

public class GraffitiPath extends GraffitiItemBase {
    private Path mPath; // 画笔的路径

    private PointF mSxy = new PointF(); // 映射后的起始坐标，（手指点击）
    private PointF mDxy = new PointF(); // 映射后的终止坐标，（手指抬起）

    private Paint mPaint = new Paint();
    private PointF mLocationTemp = new PointF();
    private PointF mLocationTemp2 = new PointF();

    private Matrix mMatrix = new Matrix();
    private CopyLocation mCopyLocation;

    public GraffitiPath(IGraffiti graffiti) {
        super(graffiti);
    }

    public GraffitiPath(IGraffiti graffiti, GraffitiPaintAttrs attrs) {
        super(graffiti, attrs);
    }

    public void reset(IGraffiti graffiti, float sx, float sy, float dx, float dy) {
//        setGraffiti(graffiti);
        setPen(graffiti.getPen());
        setShape(graffiti.getShape());
        setSize(graffiti.getSize());
        setColor(graffiti.getColor().copy());

        mSxy.set(sx, sy);
        mDxy.set(dx, dy);
        if (graffiti instanceof GraffitiView) {
            mCopyLocation = ((GraffitiView) graffiti).getCopyLocation().copy();
        } else {
            mCopyLocation = null;
        }
    }

    public void reset(IGraffiti graffiti, Path p) {
//        setGraffiti(graffiti);
        setPen(graffiti.getPen());
        setShape(graffiti.getShape());
        setSize(graffiti.getSize());
        setColor(graffiti.getColor().copy());

        this.mPath = p;
        if (graffiti instanceof GraffitiView) {
            mCopyLocation = ((GraffitiView) graffiti).getCopyLocation().copy();
        } else {
            mCopyLocation = null;
        }
    }


    public static GraffitiPath toShape(IGraffiti graffiti, float sx, float sy, float dx, float dy) {
        GraffitiPath path = new GraffitiPath(graffiti);
        path.setPen(graffiti.getPen());
        path.setShape(graffiti.getShape());
        path.setSize(graffiti.getSize());
        path.setColor(graffiti.getColor().copy());

        path.mSxy.set(sx, sy);
        path.mDxy.set(dx, dy);
        if (graffiti instanceof GraffitiView) {
            path.mCopyLocation = ((GraffitiView) graffiti).getCopyLocation().copy();
        } else {
            path.mCopyLocation = null;
        }
        return path;
    }

    public static GraffitiPath toPath(IGraffiti graffiti, Path p) {
        GraffitiPath path = new GraffitiPath(graffiti);
        path.setPen(graffiti.getPen());
        path.setShape(graffiti.getShape());
        path.setSize(graffiti.getSize());
        path.setColor(graffiti.getColor().copy());

        path.mPath = p;
        if (graffiti instanceof GraffitiView) {
            path.mCopyLocation = ((GraffitiView) graffiti).getCopyLocation().copy();
        } else {
            path.mCopyLocation = null;
        }
        return path;
    }

    @Override
    protected void doDraw(Canvas canvas) {
        mPaint.setStrokeWidth(getSize());
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);
        mMatrix.reset();

        // 根据旋转值获取正确的旋转地图
        if (getPen() == IGraffiti.Pen.ERASER || getPen() == IGraffiti.Pen.COPY) {
            getColor().setColor(getGraffiti().getBitmap());
            float px = getOriginalPivotX(), py = getOriginalPivotY();
            int degree = getGraffiti().getRotate() - getGraffitiRotate();
            float transX = 0, transY = 0;
            float transXSpan = 0, transYSpan = 0;
            if (Math.abs(getGraffitiRotate()) == 90 || Math.abs(getGraffitiRotate()) == 270) { // 矫正当前旋转后图片的中心位置
                float t = px;
                px = py;
                py = t;
            }
            if (Math.abs(degree) == 90 || Math.abs(degree) == 270) { //　交换中心点的xy坐标
                transX += py - px;
                transY += px - py;
            }
            // 仿制时需要偏移图片
            if (getPen() == IGraffiti.Pen.COPY && mCopyLocation != null) {
                /*transXSpan = mCopyLocation.getTouchStartX() - mCopyLocation.getCopyStartX();
                transYSpan = mCopyLocation.getTouchStartY() - mCopyLocation.getCopyStartY();*/
                mLocationTemp = DrawUtil.rotatePointInGraffiti(mLocationTemp, getGraffiti().getRotate(), getGraffitiRotate(),
                        mCopyLocation.getTouchStartX(), mCopyLocation.getTouchStartY(), getOriginalPivotX(), getOriginalPivotY());
                mLocationTemp2 = DrawUtil.rotatePointInGraffiti(mLocationTemp2, getGraffiti().getRotate(), getGraffitiRotate(),
                        mCopyLocation.getCopyStartX(), mCopyLocation.getCopyStartY(), getOriginalPivotX(), getOriginalPivotY());
                transXSpan = mLocationTemp.x - mLocationTemp2.x;
                transYSpan = mLocationTemp.y - mLocationTemp2.y;
            }

            mMatrix.postTranslate(-transX + transXSpan, -transY + transYSpan);
            mMatrix.postRotate(-degree, px, py);
        } else {

        }
        getColor().initColor(mPaint, mMatrix);

        if (getShape() == IGraffiti.Shape.HAND_WRITE) { // 手写
            canvas.drawPath(mPath, mPaint);
        } else { // 画图形
            draw(canvas, mPaint, getShape(), mSxy.x, mSxy.y, mDxy.x, mDxy.y);
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

