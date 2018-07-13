package cn.hzw.graffiti;

import android.graphics.BitmapShader;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;

import cn.hzw.graffiti.core.IGraffitiColor;
import cn.hzw.graffiti.core.IGraffitiItem;

/**
 * 涂鸦画笔颜色，底图为涂鸦原图，用于橡皮擦、仿制
 */
public class GraffitiCopyColor implements IGraffitiColor {

    // bitmap相关
    private Shader.TileMode mTileX = Shader.TileMode.MIRROR;
    private Shader.TileMode mTileY = Shader.TileMode.MIRROR;  // 镜像

    private Matrix matrix = new Matrix();

    public GraffitiCopyColor() {

    }

    public GraffitiCopyColor(Shader.TileMode tileX, Shader.TileMode tileY) {
        mTileX = tileX;
        mTileY = tileY;
    }

    @Override
    public void config(IGraffitiItem item, Paint paint) {
        GraffitiItemBase graffitiItem = (GraffitiItemBase) item;
        // 根据旋转值获取正确的旋转地图
        float transX = 0, transY = 0;
        float transXSpan = 0, transYSpan = 0;

        if(graffitiItem.getPen() == GraffitiPen.COPY) { // 仿制需要偏移图片
            CopyLocation copyLocation = ((GraffitiPath) item).getCopyLocation();
            // 仿制时需要偏移图片
            if (copyLocation != null) {
                transXSpan = copyLocation.getTouchStartX() - copyLocation.getCopyStartX();
                transYSpan = copyLocation.getTouchStartY() - copyLocation.getCopyStartY();
            }
        }

        matrix.reset();
        matrix.postTranslate(-transX + transXSpan, -transY + transYSpan);
        BitmapShader shader = new BitmapShader(graffitiItem.getGraffiti().getBitmap(), mTileX, mTileY);
        shader.setLocalMatrix(matrix);
        paint.setShader(shader);
    }

    @Override
    public IGraffitiColor copy() {
        return new GraffitiCopyColor(mTileX,mTileY);
    }
}
