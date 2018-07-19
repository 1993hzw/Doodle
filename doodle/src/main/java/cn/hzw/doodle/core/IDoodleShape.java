package cn.hzw.doodle.core;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * 图形
 */
public interface IDoodleShape {

     /**
      * 绘制
      * @param canvas
      * @param doodleItem
      * @param paint 绘制的画笔
      */
     public void draw(Canvas canvas, IDoodleItem doodleItem, Paint paint);

     /**
      * 深度拷贝
      * @return
      */
     public IDoodleShape copy();
}
