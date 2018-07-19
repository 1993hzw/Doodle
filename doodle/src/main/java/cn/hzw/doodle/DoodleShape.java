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
        if (this == DoodleShape.HAND_WRITE) { // 手写
            canvas.drawPath(doodlePath.getPath(), paint);
        } else { // 画图形
            PointF mSxy = doodlePath.getSxy();
            PointF mDxy = doodlePath.getDxy();
            draw(canvas, paint, (DoodleShape) doodleItem.getShape(), mSxy.x, mSxy.y, mDxy.x, mDxy.y);
        }
    }

    private void draw(Canvas canvas, Paint paint, DoodleShape shape, float sx, float sy, float dx, float dy) {
        switch (shape) { // 绘制图形
            case ARROW:
                paint.setStyle(Paint.Style.FILL);
                drawArrow(canvas, sx, sy, dx, dy, paint);
                break;
            case LINE:
                drawLine(canvas, sx, sy, dx, dy, paint);
                break;
            case FILL_CIRCLE:
                paint.setStyle(Paint.Style.FILL);
            case HOLLOW_CIRCLE:
                drawCircle(canvas, sx, sy,
                        (float) Math.sqrt((sx - dx) * (sx - dx) + (sy - dy) * (sy - dy)), paint);
                break;
            case FILL_RECT:
                paint.setStyle(Paint.Style.FILL);
            case HOLLOW_RECT:
                drawRect(canvas, sx, sy, dx, dy, paint);
                break;
            default:
                throw new RuntimeException("unknown shape:" + shape);
        }
    }

    @Override
    public IDoodleShape copy() {
        return this;
    }
}
