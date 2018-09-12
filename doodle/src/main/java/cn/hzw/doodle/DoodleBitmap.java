package cn.hzw.doodle;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import cn.hzw.doodle.core.IDoodle;

/**
 * 图片item
 * Created by huangziwei on 2017/3/16.
 */

public class DoodleBitmap extends DoodleRotatableItemBase {

    private Bitmap mBitmap;
    private Rect mSrcRect = new Rect();
    private Rect mDstRect = new Rect();

    public DoodleBitmap(IDoodle doodle, Bitmap bitmap, float size, float x, float y) {
        super(doodle, -doodle.getDoodleRotation(), x, y); // 设置item旋转角度，使其在当前状态下显示为“无旋转”效果
        setPen(DoodlePen.BITMAP);
        setPivotX(x);
        setPivotY(y);
        this.mBitmap = bitmap;
        setSize(size);
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        resetBounds(getBounds());
        setPivotX(getLocation().x + getBounds().width() / 2);
        setPivotY(getLocation().y + getBounds().height() / 2);
    }

    @Override
    public void setSize(float size) {
        float oldPivotX = getPivotX();
        float oldPivotY = getPivotY();
        super.setSize(size);
        setPivotX(getLocation().x + getBounds().width() / 2);
        setPivotY(getLocation().y + getBounds().height() / 2);
        setLocation(getLocation().x - (getPivotX() - oldPivotX), getLocation().y - (getPivotY() - oldPivotY));
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
    public void doDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, mSrcRect, mDstRect, null);
    }

}


