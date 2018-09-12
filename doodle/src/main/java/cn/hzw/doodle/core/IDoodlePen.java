package cn.hzw.doodle.core;

import android.graphics.Canvas;
import android.graphics.Paint;

public interface IDoodlePen {

    /**
     * 配置画笔
     * @param doodleItem
     * @param paint
     */
    public void config(IDoodleItem doodleItem, Paint paint);

    /**
     * 深度拷贝
     * @return
     */
    public IDoodlePen copy();

    /**
     * 绘制画笔辅助工具，由IDoodle绘制，不属于IDoodleItem的内容
     * 比如可以用于仿制功能时 定位器的绘制
     *
     * @param canvas
     * @param doodle
     */
    public void drawHelpers(Canvas canvas, IDoodle doodle);

}
