package cn.hzw.graffiti;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import cn.hzw.graffiti.core.IGraffiti;
import cn.hzw.graffiti.core.IGraffitiItem;
import cn.hzw.graffiti.core.IGraffitiPen;

/**
 * 常用画笔
 */
public enum GraffitiPen implements IGraffitiPen {

    HAND, // 手绘
    COPY, // 仿制
    ERASER, // 橡皮擦
    TEXT(true), // 文本
    BITMAP(true); // 贴图

    private boolean mIsSelectable = false; // 画笔绘制的item是否可选
    private CopyLocation mCopyLocation;
    private Matrix mMatrix;

    GraffitiPen() {
        this(false);
    }

    GraffitiPen(boolean isSelectable) {
        mIsSelectable = isSelectable;
    }

    @Override
    public void config(IGraffitiItem item, Paint paint) {
        GraffitiItemBase graffitiItem = (GraffitiItemBase) item;
        if (graffitiItem.getPen() == GraffitiPen.COPY) { // 仿制需要偏移图片
            // 根据旋转值获取正确的旋转底图
            float transX = 0, transY = 0;
            float transXSpan = 0, transYSpan = 0;
            CopyLocation copyLocation = ((GraffitiPath) item).getCopyLocation();
            // 仿制时需要偏移图片
            if (copyLocation != null) {
                transXSpan = copyLocation.getTouchStartX() - copyLocation.getCopyStartX();
                transYSpan = copyLocation.getTouchStartY() - copyLocation.getCopyStartY();
            }
            mMatrix.reset();
            mMatrix.postTranslate(-transX + transXSpan, -transY + transYSpan);
            if (item.getColor() instanceof GraffitiColor) {
                ((GraffitiColor) item.getColor()).setMatrix(mMatrix);
            }
        }
    }

    /**
     * 画笔制作的item是否可选，用于旋转、移动等特定操作
     *
     * @return
     */
    public boolean isSelectable() {
        return mIsSelectable;
    }

    public CopyLocation getCopyLocation() {
        if (this != COPY) {
            return null;
        }
        if (mCopyLocation == null) {
            synchronized (this) {
                if (mCopyLocation == null) {
                    mCopyLocation = new CopyLocation();
                    mMatrix = new Matrix();
                }
            }
        }
        return mCopyLocation;
    }

    @Override
    public void drawHelpers(Canvas canvas, IGraffiti graffiti) {
        if (this == COPY) {
            mCopyLocation.drawItSelf(canvas, graffiti.getSize());
        }
    }

    @Override
    public IGraffitiPen copy() {
        return this;
    }
}
