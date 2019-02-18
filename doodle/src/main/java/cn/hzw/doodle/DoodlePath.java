package cn.hzw.doodle;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

import java.util.HashMap;
import java.util.WeakHashMap;

import cn.hzw.doodle.core.IDoodle;
import cn.hzw.doodle.core.IDoodleColor;
import cn.hzw.doodle.util.DrawUtil;

/**
 * 涂鸦轨迹
 * Created by huangziwei on 2017/3/16.
 */

public class DoodlePath extends DoodleRotatableItemBase {

    public static final int MOSAIC_LEVEL_1 = 5;
    public static final int MOSAIC_LEVEL_2 = 20;
    public static final int MOSAIC_LEVEL_3 = 50;

    private final Path mPath = new Path(); // 画笔的路径
    private final Path mOriginPath = new Path();

    private PointF mSxy = new PointF(); // 映射后的起始坐标，（手指点击）
    private PointF mDxy = new PointF(); // 映射后的终止坐标，（手指抬起）

    private Paint mPaint = new Paint();

    private CopyLocation mCopyLocation;

    private final Matrix mTransform = new Matrix();
    private Rect mRect = new Rect();
    private Matrix mBitmapColorMatrix = new Matrix();

    public DoodlePath(IDoodle doodle) {
        super(doodle, 0, 0, 0);// 这里默认item旋转角度为0
    }

    public DoodlePath(IDoodle doodle, DoodlePaintAttrs attrs) {
        super(doodle, attrs, 0, 0, 0);
    }

    public void updateXY(float sx, float sy, float dx, float dy) {
        mSxy.set(sx, sy);
        mDxy.set(dx, dy);
        mOriginPath.reset();

        if (DoodleShape.ARROW.equals(getShape())) {
            updateArrowPath(mOriginPath, mSxy.x, mSxy.y, mDxy.x, mDxy.y, getSize());
        } else if (DoodleShape.LINE.equals(getShape())) {
            updateLinePath(mOriginPath, mSxy.x, mSxy.y, mDxy.x, mDxy.y, getSize());
        } else if (DoodleShape.FILL_CIRCLE.equals(getShape()) || DoodleShape.HOLLOW_CIRCLE.equals(getShape())) {
            updateCirclePath(mOriginPath, mSxy.x, mSxy.y, mDxy.x, mDxy.y, getSize());
        } else if (DoodleShape.FILL_RECT.equals(getShape()) || DoodleShape.HOLLOW_RECT.equals(getShape())) {
            updateRectPath(mOriginPath, mSxy.x, mSxy.y, mDxy.x, mDxy.y, getSize());
        }

        adjustPath(true);
    }

    public void updatePath(Path path) {
        mOriginPath.reset();
        this.mOriginPath.addPath(path);
        adjustPath(true);
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
        getShape().config(this, mPaint);

        canvas.drawPath(getPath(), mPaint);
    }

    private RectF mBound = new RectF();

    private void resetLocationBounds(Rect rect) {
        if (mOriginPath == null) {
            return;
        }

        int diff = (int) (getSize() / 2 + 0.5f);
        mOriginPath.computeBounds(mBound, false);
        if (getShape() == DoodleShape.ARROW || getShape() == DoodleShape.FILL_CIRCLE || getShape() == DoodleShape.FILL_RECT) {
            diff = (int) getDoodle().getUnitSize();
        }
        rect.set((int) (mBound.left - diff), (int) (mBound.top - diff), (int) (mBound.right + diff), (int) (mBound.bottom + diff));
    }

    @Override
    protected void resetBounds(Rect rect) {
        resetLocationBounds(rect);
        rect.set(0, 0, rect.width(), rect.height());
    }

    @Override
    public boolean isDoodleEditable() {
        if (getPen() == DoodlePen.ERASER) { // eraser is not editable
            return false;
        }

        return super.isDoodleEditable();
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

    private static WeakHashMap<IDoodle, HashMap<Integer, Bitmap>> sMosaicBitmapMap = new WeakHashMap<>();

    public static DoodleColor getMosaicColor(IDoodle doodle, int level) {
        HashMap<Integer, Bitmap> map = sMosaicBitmapMap.get(doodle);
        if (map == null) {
            map = new HashMap<>();
            sMosaicBitmapMap.put(doodle, map);
        }
        Matrix matrix = new Matrix();
        matrix.setScale(1f / level, 1f / level);
        Bitmap mosaicBitmap = map.get(level);
        if (mosaicBitmap == null) {
            mosaicBitmap = Bitmap.createBitmap(doodle.getBitmap(),
                    0, 0, doodle.getBitmap().getWidth(), doodle.getBitmap().getHeight(), matrix, true);
            map.put(level, mosaicBitmap);
        }
        matrix.reset();
        matrix.setScale(level, level);
        DoodleColor doodleColor = new DoodleColor(mosaicBitmap, matrix, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        doodleColor.setLevel(level);
        return doodleColor;
    }

    @Override
    public void setLocation(float x, float y, boolean changePivot) {
        super.setLocation(x, y, changePivot);
        adjustMosaic();
    }

    @Override
    public void setColor(IDoodleColor color) {
        super.setColor(color);
        if (getPen() == DoodlePen.MOSAIC) {
            setLocation(getLocation().x, getLocation().y, false);
        }
        adjustPath(false);
    }

    @Override
    public void setSize(float size) {
        super.setSize(size);


        if (mTransform == null) {
            return;
        }

        if (DoodleShape.ARROW.equals(getShape())) {
            mOriginPath.reset();
            updateArrowPath(mOriginPath, mSxy.x, mSxy.y, mDxy.x, mDxy.y, getSize());
        }

        adjustPath(false);
    }

    @Override
    public void setScale(float scale) {
        super.setScale(scale);
        adjustMosaic();
    }

    private void adjustMosaic() {
        if (getPen() == DoodlePen.MOSAIC
                && getColor() instanceof DoodleColor) {
            DoodleColor doodleColor = ((DoodleColor) getColor());
            Matrix matrix = doodleColor.getMatrix();
            matrix.reset();
            matrix.preScale(1 / getScale(), 1 / getScale(), getPivotX(), getPivotY()); // restore scale
            matrix.preTranslate(-getLocation().x * getScale(), -getLocation().y * getScale());
            matrix.preRotate(-getItemRotate(), getPivotX(), getPivotY());
            matrix.preScale(doodleColor.getLevel(), doodleColor.getLevel());
            doodleColor.setMatrix(matrix);
            refresh();
        }
    }

    @Override
    public void setItemRotate(float textRotate) {
        super.setItemRotate(textRotate);
        adjustMosaic();
    }

    private void adjustPath(boolean changePivot) {
        resetLocationBounds(mRect);
        mPath.reset();
        this.mPath.addPath(mOriginPath);
        mTransform.reset();
        mTransform.setTranslate(-mRect.left, -mRect.top);
        mPath.transform(mTransform);
        if (changePivot) {
            setPivotX(mRect.left + mRect.width() / 2);
            setPivotY(mRect.top + mRect.height() / 2);
            setLocation(mRect.left, mRect.top, false);
        }

        if ((getColor() instanceof DoodleColor)) {
            DoodleColor color = (DoodleColor) getColor();
            if (color.getType() == DoodleColor.Type.BITMAP && color.getBitmap() != null) {
                mBitmapColorMatrix.reset();

                if (getPen() == DoodlePen.MOSAIC) {
                    adjustMosaic();
                } else {
                    if (getPen() == DoodlePen.COPY) {
                        // 根据旋转值获取正确的旋转底图
                        float transXSpan = 0, transYSpan = 0;
                        CopyLocation copyLocation = getCopyLocation();
                        // 仿制时需要偏移图片
                        if (copyLocation != null) {
                            transXSpan = copyLocation.getTouchStartX() - copyLocation.getCopyStartX();
                            transYSpan = copyLocation.getTouchStartY() - copyLocation.getCopyStartY();
                        }
                        resetLocationBounds(mRect);
                        mBitmapColorMatrix.setTranslate(transXSpan - mRect.left, transYSpan - mRect.top);
                    } else {
                        mBitmapColorMatrix.setTranslate(-mRect.left, -mRect.top);
                    }

                    int level = color.getLevel();
                    mBitmapColorMatrix.preScale(level, level);
                    color.setMatrix(mBitmapColorMatrix);
                    refresh();
                }
            }
        }

        refresh();
    }
}

