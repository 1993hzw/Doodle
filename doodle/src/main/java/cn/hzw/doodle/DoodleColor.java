package cn.hzw.doodle;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;

import cn.hzw.doodle.core.IDoodleColor;
import cn.hzw.doodle.core.IDoodleItem;

/**
 * 涂鸦画笔颜色，用于手绘
 */
public class DoodleColor implements IDoodleColor {

    public enum Type {
        COLOR, // 颜色值
        BITMAP // 图片
    }

    private int mColor;
    private Bitmap mBitmap;
    private Type mType;
    private Matrix mMatrix;

    private int mLevel = 1;

    // bitmap相关
    private Shader.TileMode mTileX = Shader.TileMode.MIRROR;
    private Shader.TileMode mTileY = Shader.TileMode.MIRROR;  // 镜像

    public DoodleColor(int color) {
        mType = Type.COLOR;
        mColor = color;
    }

    public DoodleColor(Bitmap bitmap) {
        this(bitmap, null);
    }

    public DoodleColor(Bitmap bitmap, Matrix matrix) {
        this(bitmap, matrix, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR);
    }

    public DoodleColor(Bitmap bitmap, Matrix matrix, Shader.TileMode tileX, Shader.TileMode tileY) {
        mType = Type.BITMAP;
        mMatrix = matrix;
        mBitmap = bitmap;
        mTileX = tileX;
        mTileY = tileY;
    }

    @Override
    public void config(IDoodleItem item, Paint paint) {
        DoodleItemBase doodleItem = (DoodleItemBase) item;
        if (mType == Type.COLOR) {
            paint.setColor(mColor);
            paint.setShader(null);
        } else if (mType == Type.BITMAP) {
            BitmapShader shader = new BitmapShader(mBitmap, mTileX, mTileY);
            shader.setLocalMatrix(mMatrix);
            paint.setShader(shader);
        }
    }

    public void setColor(int color) {
        mType = Type.COLOR;
        mColor = color;
    }

    public void setColor(Bitmap bitmap) {
        mType = Type.BITMAP;
        mBitmap = bitmap;
    }

    public void setColor(Bitmap bitmap, Matrix matrix) {
        mType = Type.BITMAP;
        mMatrix = matrix;
        mBitmap = bitmap;
    }

    public void setColor(Bitmap bitmap, Matrix matrix, Shader.TileMode tileX, Shader.TileMode tileY) {
        mType = Type.BITMAP;
        mBitmap = bitmap;
        mMatrix = matrix;
        mTileX = tileX;
        mTileY = tileY;
    }

    public void setMatrix(Matrix matrix) {
        mMatrix = matrix;
    }

    public Matrix getMatrix() {
        return mMatrix;
    }

    public int getColor() {
        return mColor;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public Type getType() {
        return mType;
    }

    @Override
    public IDoodleColor copy() {
        DoodleColor color = null;
        if (mType == Type.COLOR) {
            color = new DoodleColor(mColor);
        } else {
            color = new DoodleColor(mBitmap);
        }
        color.mTileX = mTileX;
        color.mTileY = mTileY;
        color.mMatrix = new Matrix(mMatrix);
        color.mLevel = mLevel;
        return color;
    }

    public void setLevel(int level) {
        mLevel = level;
    }

    public int getLevel() {
        return mLevel;
    }
}


