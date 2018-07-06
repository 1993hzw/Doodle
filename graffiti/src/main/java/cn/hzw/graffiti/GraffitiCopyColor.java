package cn.hzw.graffiti;

import android.graphics.BitmapShader;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Shader;

import cn.hzw.graffiti.core.IGraffitiColor;
import cn.hzw.graffiti.core.IGraffitiItem;
import cn.hzw.graffiti.util.DrawUtil;

public class GraffitiCopyColor implements IGraffitiColor {

    private PointF mLocationTemp = new PointF();
    private PointF mLocationTemp2 = new PointF();

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
    public void initColor(Paint paint, IGraffitiItem item) {
        GraffitiItemBase graffitiItem = (GraffitiItemBase) item;
        // 根据旋转值获取正确的旋转地图
        float px = graffitiItem.getOriginalPivotX(), py = graffitiItem.getOriginalPivotY();
        int degree = graffitiItem.getGraffiti().getRotate() - graffitiItem.getGraffitiRotate();
        float transX = 0, transY = 0;
        float transXSpan = 0, transYSpan = 0;
        if (Math.abs(graffitiItem.getGraffitiRotate()) == 90 || Math.abs(graffitiItem.getGraffitiRotate()) == 270) { // 矫正当前旋转后图片的中心位置
            float t = px;
            px = py;
            py = t;
        }
        if (Math.abs(degree) == 90 || Math.abs(degree) == 270) { //　交换中心点的xy坐标
            transX += py - px;
            transY += px - py;
        }

        if(graffitiItem.getPen() == GraffitiPen.COPY) {
            CopyLocation mCopyLocation = ((GraffitiPath) item).getCopyLocation();
            // 仿制时需要偏移图片
            if (mCopyLocation != null) {
                /*transXSpan = mCopyLocation.getTouchStartX() - mCopyLocation.getCopyStartX();
                transYSpan = mCopyLocation.getTouchStartY() - mCopyLocation.getCopyStartY();*/
                mLocationTemp = DrawUtil.rotatePointInGraffiti(mLocationTemp, graffitiItem.getGraffiti().getRotate(), graffitiItem.getGraffitiRotate(),
                        mCopyLocation.getTouchStartX(), mCopyLocation.getTouchStartY(), graffitiItem.getOriginalPivotX(), graffitiItem.getOriginalPivotY());
                mLocationTemp2 = DrawUtil.rotatePointInGraffiti(mLocationTemp2, graffitiItem.getGraffiti().getRotate(), graffitiItem.getGraffitiRotate(),
                        mCopyLocation.getCopyStartX(), mCopyLocation.getCopyStartY(), graffitiItem.getOriginalPivotX(), graffitiItem.getOriginalPivotY());
                transXSpan = mLocationTemp.x - mLocationTemp2.x;
                transYSpan = mLocationTemp.y - mLocationTemp2.y;
            }
        }

        matrix.reset();
        matrix.postTranslate(-transX + transXSpan, -transY + transYSpan);
        matrix.postRotate(-degree, px, py);
        BitmapShader shader = new BitmapShader(graffitiItem.getGraffiti().getBitmap(), mTileX, mTileY);
        shader.setLocalMatrix(matrix);
        paint.setShader(shader);
    }

    @Override
    public IGraffitiColor copy() {
        return new GraffitiCopyColor(mTileX,mTileY);
    }
}
