package cn.hzw.graffiti;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

import static cn.hzw.graffiti.DrawUtil.drawArrow;
import static cn.hzw.graffiti.DrawUtil.drawCircle;
import static cn.hzw.graffiti.DrawUtil.drawLine;
import static cn.hzw.graffiti.DrawUtil.drawRect;
import static cn.hzw.graffiti.DrawUtil.rotatePoint;
import static cn.hzw.graffiti.DrawUtil.rotatePointInGraffiti;

/**
 * Created by huangziwei on 2017/3/16.
 */

public class GraffitiPath implements IGraffitiItem {
    GraffitiView.Pen mPen; // 画笔类型
    GraffitiView.Shape mShape; // 画笔形状
    float mStrokeWidth; // 大小
    GraffitiColor mColor; // 颜色
    Path mPath; // 画笔的路径
    float mSx, mSy; // 映射后的起始坐标，（手指点击）
    float mDx, mDy; // 映射后的终止坐标，（手指抬起）
    private Matrix mMatrix = new Matrix(); //　图片的偏移矩阵
    int mRotateDegree = 0; // 旋转的角度（围绕图片中心旋转）
    float mPivotX, mPivotY;
    CopyLocation mCopy;
    private Paint mPaint = new Paint();
    private IGraffiti mGraffiti;
    private Matrix mShaderMatrixColor = new Matrix(); // 画笔图片底图的变化矩阵


    @Override
    public void setGraffiti(IGraffiti graffiti) {
        mGraffiti = graffiti;
        if (graffiti == null) {
            return;
        }
        mPivotX = graffiti.getBitmapWidth() / 2;
        mPivotY = graffiti.getBitmapHeight() / 2;
        if (mPen == IGraffiti.Pen.ERASER || mPen == IGraffiti.Pen.COPY) {
            mColor = new GraffitiColor(graffiti.getBitmap());
        }
    }

    public Path getPath(int currentDegree) {
        int degree = currentDegree - mRotateDegree;
        if (degree == 0) {
            return mPath;
        }
        Path path = new Path(mPath);
        Matrix matrix = new Matrix();

        float px = mPivotX, py = mPivotY;
        if (mRotateDegree == 90 || mRotateDegree == 270) { //　交换中心点的xy坐标
            float t = px;
            px = py;
            py = t;
        }

        matrix.setRotate(degree, px, py);
        if (Math.abs(degree) == 90 || Math.abs(degree) == 270) {
            matrix.postTranslate((py - px), -(py - px));
        }
        path.transform(matrix);
        return path;
    }

    public float[] getDxDy(int currentDegree) {

        return rotatePointInGraffiti(currentDegree, mRotateDegree, mDx, mDy, mPivotX, mPivotY);
    }

    public float[] getSxSy(int currentDegree) {

        return rotatePointInGraffiti(currentDegree, mRotateDegree, mSx, mSy, mPivotX, mPivotY);
    }

    public Matrix getMatrix(int currentDegree) {
        if (mMatrix == null) {
            return null;
        }
        if (mPen == GraffitiView.Pen.COPY) { // 仿制，加上mCopyLocation记录的偏移
            mMatrix.reset();

            int degree = currentDegree - mRotateDegree;
            if (degree == 0) {
                mMatrix.postTranslate(mCopy.getTouchStartX() - mCopy.getCopyStartX(), mCopy.getTouchStartY() - mCopy.getCopyStartY());
                return mMatrix;
            }
            float px = mPivotX, py = mPivotY;
            if (mRotateDegree == 90 || mRotateDegree == 270) { //　交换中心点的xy坐标
                float t = px;
                px = py;
                py = t;
            }
            float[] coords = rotatePoint(degree, mCopy.getTouchStartX(), mCopy.getTouchStartY(), px, py);
            float[] coordsCopy = rotatePoint(degree, mCopy.getCopyStartX(), mCopy.getCopyStartY(), px, py);
            if (Math.abs(degree) == 90 || Math.abs(degree) == 270) {
                coords[0] += (py - px);
                coords[1] += -(py - px);
                coordsCopy[0] += (py - px);
                coordsCopy[1] += -(py - px);
            }
            mMatrix.postTranslate(coords[0] - coordsCopy[0], coords[1] - coordsCopy[1]);
            return mMatrix;
        } else {
            return mMatrix;
        }

    }

    static GraffitiPath toShape(GraffitiView.Pen pen, GraffitiView.Shape shape, float width, GraffitiColor color,
                                float sx, float sy, float dx, float dy, int degree, CopyLocation copyLocation) {
        GraffitiPath path = new GraffitiPath();
        path.mPen = pen;
        path.mShape = shape;
        path.mStrokeWidth = width;
        path.mColor = color;
        path.mSx = sx;
        path.mSy = sy;
        path.mDx = dx;
        path.mDy = dy;
        path.mRotateDegree = degree;
        path.mCopy = copyLocation;
        return path;
    }

    static GraffitiPath toPath(GraffitiView.Pen pen, GraffitiView.Shape shape, float width, GraffitiColor color, Path p, int degree, CopyLocation copyLocation) {
        GraffitiPath path = new GraffitiPath();
        path.mPen = pen;
        path.mShape = shape;
        path.mStrokeWidth = width;
        path.mColor = color;
        path.mPath = p;
        path.mRotateDegree = degree;
        path.mCopy = copyLocation;
        return path;
    }

    @Override
    public void draw(IGraffiti graffiti, Canvas canvas) {
        mPaint.setStrokeWidth(mStrokeWidth);
        if (mShape == IGraffiti.Shape.HAND_WRITE) { // 手写
            draw(canvas, mPen, mPaint, getPath(graffiti.getRotate()), getMatrix(graffiti.getRotate()), mColor);
        } else { // 画图形
            float[] sxy = getSxSy(graffiti.getRotate());
            float[] dxy = getDxDy(graffiti.getRotate());
            draw(canvas, mPen, mShape, mPaint,
                    sxy[0], sxy[1], dxy[0], dxy[1], getMatrix(graffiti.getRotate()), mColor);
        }
    }

    private void draw(Canvas canvas, IGraffiti.Pen pen, Paint paint, Path path, Matrix matrix, GraffitiColor color) {
        resetPaint(pen, paint, matrix, color);

        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint);
    }

    private void draw(Canvas canvas, IGraffiti.Pen pen, IGraffiti.Shape shape, Paint paint, float sx, float sy, float dx, float dy, Matrix matrix, GraffitiColor color) {
        resetPaint(pen, paint, matrix, color);

        paint.setStyle(Paint.Style.STROKE);

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

    private void resetPaint(IGraffiti.Pen pen, Paint paint, Matrix matrix, GraffitiColor color) {
        mPaint.setColor(Color.BLACK);
        switch (pen) { // 设置画笔
            case HAND:
            case TEXT:
                paint.setShader(null);
                mShaderMatrixColor.reset();

                if (color.getType() == GraffitiColor.Type.BITMAP) { // 旋转底图
                    if (mGraffiti.getRotate() != 0) {
                        float px = mPivotX, py = mPivotY;
                        if (mGraffiti.getRotate() == 90 || mGraffiti.getRotate() == 270) { //　交换中心点的xy坐标
                            float t = px;
                            px = py;
                            py = t;
                        }
                        mShaderMatrixColor.postRotate(mGraffiti.getRotate(), px, py);
                        if (Math.abs(mGraffiti.getRotate()) == 90 || Math.abs(mGraffiti.getRotate()) == 270) {
                            mShaderMatrixColor.postTranslate((py - px), -(py - px));
                        }
                    }
                }

                color.initColor(paint, mShaderMatrixColor);
                break;
            case COPY:
                // 调整copy图片位置
                color.initColor(paint, mShaderMatrixColor);
                break;
            case ERASER:
                color.initColor(paint, mShaderMatrixColor);
                break;
        }
    }
}

