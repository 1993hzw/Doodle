package cn.hzw.graffiti;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by huangziwei on 2017/3/16.
 */

public class GraffitiBitmap extends GraffitiSelectableItem {

    private Bitmap mBitmap;
    private Rect mSrcRect = new Rect();
    private Rect mDstRect = new Rect();

    public GraffitiBitmap(Bitmap bitmap, float size, GraffitiColor color, int textRotate, int rotateDegree, float x, float y, float px, float py) {
        super(size, color, textRotate, rotateDegree, x, y, px, py);
        this.mBitmap = bitmap;
        resetBounds(getBounds());
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        resetBounds(getBounds());
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }


    @Override
    public void resetBounds(Rect rect) {
        if (mBitmap == null) {
            return;
        }
        float size = getSize();
        rect.set(0, 0, (int) size, (int) (size * mBitmap.getHeight() / mBitmap.getWidth()));

        mSrcRect.set(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        mDstRect.set(0, 0, (int) size, (int) (size * mBitmap.getHeight()) / mBitmap.getWidth());

    }

    @Override
    public void draw(Canvas canvas, GraffitiView graffitiView, Paint paint) {
        canvas.drawBitmap(mBitmap, mSrcRect, mDstRect, null);
    }

}


