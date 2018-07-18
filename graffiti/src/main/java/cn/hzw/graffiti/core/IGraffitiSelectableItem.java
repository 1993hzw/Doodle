package cn.hzw.graffiti.core;

import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * Created on 27/06/2018.
 */

public interface IGraffitiSelectableItem extends IGraffitiItem {

    /**
     * 设置是否选中
     * @param isSelected
     */
    public void setSelected(boolean isSelected);

    /**
     * 是否选中
     * @return
     */
    public boolean isSelected();

    /**
     * item的矩形范围
     * @return
     */
    public Rect getBounds();

    /**
     * 判断点（x,y）是否在item内，用于判断是否点中item
     */
    public boolean contains(float x, float y);

    /**
     * 绘制选择时的背景
     *
     * @param canvas
     */
    public void drawSelectedBackground(Canvas canvas);

}
