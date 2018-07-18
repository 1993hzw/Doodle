package cn.hzw.graffiti;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;

import cn.hzw.graffiti.core.IGraffitiColor;
import cn.hzw.graffiti.core.IGraffitiItem;

/**
 * 涂鸦画笔颜色，底图为一张图片，可偏移底图位置，用于仿制
 */
public class GraffitiCopyColor implements IGraffitiColor {

    // bitmap相关
    private Shader.TileMode mTileX = Shader.TileMode.MIRROR;
    private Shader.TileMode mTileY = Shader.TileMode.MIRROR;  // 镜像
    private Bitmap mBitmap;

    private Matrix matrix = new Matrix();

    public GraffitiCopyColor(Bitmap bitmap) {
        this(bitmap, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR);
    }

    public GraffitiCopyColor(Bitmap bitmap, Shader.TileMode tileX, Shader.TileMode tileY) {
        mBitmap = bitmap;
        mTileX = tileX;
        mTileY = tileY;
    }

    @Override
    public void config(IGraffitiItem item, Paint paint) {
        GraffitiItemBase graffitiItem = (GraffitiItemBase) item;
        // 根据旋转值获取正确的旋转地图
        float transX = 0, transY = 0;
        float transXSpan = 0, transYSpan = 0;

        if (graffitiItem.getPen() == GraffitiPen.COPY) { // 仿制需要偏移图片
            CopyLocation copyLocation = ((GraffitiPath) item).getCopyLocation();
            // 仿制时需要偏移图片
            if (copyLocation != null) {
                transXSpan = copyLocation.getTouchStartX() - copyLocation.getCopyStartX();
                transYSpan = copyLocation.getTouchStartY() - copyLocation.getCopyStartY();
            }
        }

        matrix.reset();
        matrix.postTranslate(-transX + transXSpan, -transY + transYSpan);
        BitmapShader shader = new BitmapShader(mBitmap, mTileX, mTileY);
        shader.setLocalMatrix(matrix);
        paint.setShader(shader);
    }

    @Override
    public IGraffitiColor copy() {
        return new GraffitiCopyColor(mBitmap, mTileX, mTileY);
    }
}
