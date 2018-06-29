package cn.hzw.graffiti;

import android.graphics.Canvas;

/**
 * Created on 27/06/2018.
 */

public interface IGraffitiSelectableItem extends IGraffitiItem {

    /**
     * 是否击中
     */
    public boolean isInIt(IGraffiti graffiti, float x, float y);

    /**
     * 绘制选别时的背景
     *
     * @param canvas
     * @param graffiti
     */
    public void drawSelectedBackground(IGraffiti graffiti, Canvas canvas);

    /**
     * 是否可以旋转
     */
    public boolean isCanRotate(IGraffiti graffiti, float x, float y);

}
