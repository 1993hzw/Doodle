package cn.hzw.graffiti.core;

import android.graphics.Canvas;

public interface IGraffitiPen {

    /**
     * 画笔制作的item是否可选，用于旋转、移动等特定操作
     * @return
     */
    public boolean isSelectable();

    /**
     * 绘制画笔，由IGraffiti绘制，不属于IGraffitiItem的内容
     * 比如可以用于仿制功能时 定位器的绘制
     * @param canvas
     * @param graffiti
     */
    public void draw( Canvas canvas, IGraffiti graffiti);

}
