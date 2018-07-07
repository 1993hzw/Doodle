package cn.hzw.graffiti;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import cn.hzw.graffiti.core.IGraffitiItem;
import cn.hzw.graffiti.core.IGraffitiShape;

import static cn.hzw.graffiti.util.DrawUtil.drawArrow;
import static cn.hzw.graffiti.util.DrawUtil.drawCircle;
import static cn.hzw.graffiti.util.DrawUtil.drawLine;
import static cn.hzw.graffiti.util.DrawUtil.drawRect;

/**
 * 常用图形
 */
public enum GraffitiShape implements IGraffitiShape {
    HAND_WRITE, // 手绘
    ARROW, // 箭头
    LINE, // 直线
    FILL_CIRCLE, // 实心圆
    HOLLOW_CIRCLE, // 空心圆
    FILL_RECT, // 实心矩形
    HOLLOW_RECT; // 空心矩形


    @Override
    public void draw(Canvas canvas, IGraffitiItem graffitiItem, Paint paint) {
        GraffitiPath graffitiPath = (GraffitiPath) graffitiItem;
        if (this == GraffitiShape.HAND_WRITE) { // 手写
            canvas.drawPath(graffitiPath.getPath(), paint);
        } else { // 画图形
            PointF mSxy = graffitiPath.getSxy();
            PointF mDxy = graffitiPath.getDxy();
            draw(canvas, paint, (GraffitiShape) graffitiItem.getShape(), mSxy.x, mSxy.y, mDxy.x, mDxy.y);
        }
    }

    private void draw(Canvas canvas, Paint paint, GraffitiShape shape, float sx, float sy, float dx, float dy) {
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
}
