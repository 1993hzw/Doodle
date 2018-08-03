package cn.hzw.doodle;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import cn.hzw.doodle.core.IDoodleColor;
import cn.hzw.doodle.core.IDoodleItem;
import cn.hzw.doodle.core.IDoodleShape;

import static cn.hzw.doodle.util.DrawUtil.drawArrow;
import static cn.hzw.doodle.util.DrawUtil.drawCircle;
import static cn.hzw.doodle.util.DrawUtil.drawLine;
import static cn.hzw.doodle.util.DrawUtil.drawRect;

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
        DoodlePath doodlePath = (DoodlePath) doodleItem;
        canvas.drawPath(doodlePath.getPath(), paint);
    }

    @Override
    public IDoodleShape copy() {
        return this;
    }
}
