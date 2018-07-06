package cn.hzw.graffiti.core;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * 图形
 */
public interface IGraffitiShape {

     public void draw(Canvas canvas, IGraffitiItem graffitiItem, Paint mPaint);
}
