package cn.hzw.graffiti;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;

import cn.hzw.graffiti.core.IGraffitiColor;
import cn.hzw.graffiti.core.IGraffitiItem;
import cn.hzw.graffiti.core.IGraffitiPen;
import cn.hzw.graffiti.util.DrawUtil;

/**
 * 涂鸦画笔颜色
 */
public class GraffitiColor implements IGraffitiColor{

    public enum Type {
        COLOR, // 颜色值
        BITMAP // 图片
    }

    private int mColor;
    private Bitmap mBitmap;
    private Type mType;

    // bitmap相关
    private Shader.TileMode mTileX = Shader.TileMode.MIRROR;
    private Shader.TileMode mTileY = Shader.TileMode.MIRROR;  // 镜像

    public GraffitiColor(int color) {
        mType = Type.COLOR;
        mColor = color;
    }

    public GraffitiColor(Bitmap bitmap) {
        mType = Type.BITMAP;
        mBitmap = bitmap;
    }

    public GraffitiColor(Bitmap bitmap, Shader.TileMode tileX, Shader.TileMode tileY) {
        mType = Type.BITMAP;
        mBitmap = bitmap;
        mTileX = tileX;
        mTileY = tileY;
    }

    @Override
    public void initColor(Paint paint, IGraffitiItem item) {
        GraffitiItemBase graffitiItem = (GraffitiItemBase) item;
        if (mType == Type.COLOR) {
            paint.setColor(mColor);
        } else if (mType == Type.BITMAP) {
            BitmapShader shader = new BitmapShader(mBitmap, mTileX, mTileY);
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

    public void setColor(Bitmap bitmap, Shader.TileMode tileX, Shader.TileMode tileY) {
        mType = Type.BITMAP;
        mBitmap = bitmap;
        mTileX = tileX;
        mTileY = tileY;
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
    public IGraffitiColor copy() {
        GraffitiColor color = null;
        if (mType == Type.COLOR) {
            color = new GraffitiColor(mColor);
        } else {
            color = new GraffitiColor(mBitmap);
        }
        color.mTileX = mTileX;
        color.mTileY = mTileY;
        return color;
    }
}


