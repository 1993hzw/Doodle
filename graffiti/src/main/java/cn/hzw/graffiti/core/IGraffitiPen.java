package cn.hzw.graffiti.core;

import android.graphics.Canvas;
import android.graphics.Paint;

public interface IGraffitiPen {

    /**
     * 配置画笔
     * @param graffitiItem
     * @param paint
     */
    public void config(IGraffitiItem graffitiItem, Paint paint);

    /**
     * 画笔制作的item是否可选，用于旋转、移动等特定操作
     *
     * @return
     */
    public boolean isSelectable();

    /**
     * 绘制画笔辅助工具，由IGraffiti绘制，不属于IGraffitiItem的内容
     * 比如可以用于仿制功能时 定位器的绘制
     *
     * @param canvas
     * @param graffiti
     */
    public void drawHelpers(Canvas canvas, IGraffiti graffiti);

    /**
     * 深度拷贝
     * @return
     */
    public IGraffitiPen copy();

}
