package cn.hzw.graffiti;

import android.graphics.Matrix;
import android.graphics.Path;

import static cn.hzw.graffiti.DrawUtil.rotatePoint;
import static cn.hzw.graffiti.DrawUtil.rotatePointInGraffiti;

/**
 * Created by huangziwei on 2017/3/16.
 */

public class GraffitiPath implements Undoable {
    GraffitiView.Pen mPen; // 画笔类型
    GraffitiView.Shape mShape; // 画笔形状
    float mStrokeWidth; // 大小
    GraffitiColor mColor; // 颜色
    Path mPath; // 画笔的路径
    float mSx, mSy; // 映射后的起始坐标，（手指点击）
    float mDx, mDy; // 映射后的终止坐标，（手指抬起）
    Matrix mMatrix = new Matrix(); //　图片的偏移矩阵
    int mRotateDegree = 0; // 旋转的角度（围绕图片中心旋转）
    float mPivotX, mPivotY;
    CopyLocation mCopy;

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
                                float sx, float sy, float dx, float dy, int degree, float px, float py, CopyLocation copyLocation) {
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
        path.mPivotX = px;
        path.mPivotY = py;
        path.mCopy = copyLocation;
        return path;
    }

    static GraffitiPath toPath(GraffitiView.Pen pen, GraffitiView.Shape shape, float width, GraffitiColor color, Path p, int degree, float px, float py, CopyLocation copyLocation) {
        GraffitiPath path = new GraffitiPath();
        path.mPen = pen;
        path.mShape = shape;
        path.mStrokeWidth = width;
        path.mColor = color;
        path.mPath = p;
        path.mRotateDegree = degree;
        path.mPivotX = px;
        path.mPivotY = py;
        path.mCopy = copyLocation;
        return path;
    }
}

