package cn.hzw.doodle.core;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * 图形
 */
public interface IDoodleShape {


     /**
      * 配置图形
      * @param doodleItem
      * @param paint
      */
     public void config(IDoodleItem doodleItem, Paint paint);

     /**
      * 深度拷贝
      * @return
      */
     public IDoodleShape copy();

     /**
      * 绘制图形辅助工具，由IDoodle绘制，不属于IDoodleItem的内容
      * 比如可以用于仿制功能时 定位器的绘制
      *
      * @param canvas
      * @param doodle
      */
     public void drawHelpers(Canvas canvas, IDoodle doodle);
}
