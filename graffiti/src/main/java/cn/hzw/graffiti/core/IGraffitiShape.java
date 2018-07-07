package cn.hzw.graffiti.core;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * 图形
 */
public interface IGraffitiShape {

     /**
      * 绘制
      * @param canvas
      * @param graffitiItem
      * @param paint 绘制的画笔
      */
     public void draw(Canvas canvas, IGraffitiItem graffitiItem, Paint paint);
}
