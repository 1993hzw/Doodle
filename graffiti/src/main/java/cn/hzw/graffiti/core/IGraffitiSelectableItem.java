package cn.hzw.graffiti.core;

import android.graphics.Canvas;

/**
 * Created on 27/06/2018.
 */

public interface IGraffitiSelectableItem extends IGraffitiItem {

    /**
     * 是否击中
     */
    public boolean isInIt(float x, float y);

    /**
     * 绘制选别时的背景
     *
     * @param canvas
     */
    public void drawSelectedBackground(Canvas canvas);

    /**
     * 是否可以旋转
     */
    public boolean isCanRotate(float x, float y);

    /**
     * 是否正在旋转
     *
     * @return
     */
    public boolean isRotating();

    /**
     * 设置正在旋转
     *
     * @param isRotating
     */
    public void setIsRotating(boolean isRotating);

}
