package cn.hzw.doodle;

import android.graphics.Canvas;
import android.graphics.Paint;

import cn.hzw.doodle.core.IDoodleItem;
import cn.hzw.doodle.core.IDoodleShape;

/**
 * 常用图形
 */
public enum DoodleShape implements IDoodleShape {
    HAND_WRITE, // 手绘
    ARROW, // 箭头
    LINE, // 直线
    FILL_CIRCLE, // 实心圆
    HOLLOW_CIRCLE, // 空心圆
    FILL_RECT, // 实心矩形
    HOLLOW_RECT; // 空心矩形


    @Override
    public void draw(Canvas canvas, IDoodleItem doodleItem, Paint paint) {
        if (doodleItem.getShape() == DoodleShape.ARROW || doodleItem.getShape() == DoodleShape.FILL_CIRCLE || doodleItem.getShape() == DoodleShape.FILL_RECT) {
            paint.setStyle(Paint.Style.FILL);
        }
        DoodlePath doodlePath = (DoodlePath) doodleItem;
        canvas.drawPath(doodlePath.getPath(), paint);
    }

    @Override
    public IDoodleShape copy() {
        return this;
    }
}
