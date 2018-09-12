package cn.hzw.doodle;

import android.graphics.Canvas;
import android.graphics.Paint;

import cn.hzw.doodle.core.IDoodle;
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
    public void config(IDoodleItem doodleItem, Paint paint) {
        if (doodleItem.getShape() == DoodleShape.ARROW || doodleItem.getShape() == DoodleShape.FILL_CIRCLE || doodleItem.getShape() == DoodleShape.FILL_RECT) {
            paint.setStyle(Paint.Style.FILL);
        } else {
            paint.setStyle(Paint.Style.STROKE);
        }
    }

    @Override
    public IDoodleShape copy() {
        return this;
    }

    @Override
    public void drawHelpers(Canvas canvas, IDoodle doodle) {

    }
}
