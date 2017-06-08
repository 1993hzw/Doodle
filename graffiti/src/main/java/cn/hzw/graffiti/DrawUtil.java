package cn.hzw.graffiti;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import cn.forward.androids.utils.LogUtil;

/**
 * Created by Administrator on 2016/9/3.
 */
public class DrawUtil {

    public static float GRAFFITI_PIXEL_UNIT = 1;

    public static void drawArrow(Canvas canvas, float sx, float sy, float ex,
                                 float ey, Paint paint) {
        float arrowSize = paint.getStrokeWidth();
        double H = arrowSize; // 箭头高度
        double L = arrowSize / 2; // 底边的一�?

        double awrad = Math.atan(L / 2 / H); // 箭头角度
        double arraow_len = Math.sqrt(L / 2 * L / 2 + H * H) - 5; // 箭头的长�?
        double[] arrXY_1 = rotateVec(ex - sx, ey - sy, awrad, true,
                arraow_len);
        double[] arrXY_2 = rotateVec(ex - sx, ey - sy, -awrad, true,
                arraow_len);
        float x_3 = (float) (ex - arrXY_1[0]); // (x3,y3)是第�?端点
        float y_3 = (float) (ey - arrXY_1[1]);
        float x_4 = (float) (ex - arrXY_2[0]); // (x4,y4)是第二端�?
        float y_4 = (float) (ey - arrXY_2[1]);
        // 画线
        Path linePath = new Path();
        linePath.moveTo(sx, sy);
        linePath.lineTo(x_3, y_3);
        linePath.lineTo(x_4, y_4);
        linePath.close();
        canvas.drawPath(linePath, paint);

        awrad = Math.atan(L / H); // 箭头角度
        arraow_len = Math.sqrt(L * L + H * H); // 箭头的长�?
        arrXY_1 = rotateVec(ex - sx, ey - sy, awrad, true, arraow_len);
        arrXY_2 = rotateVec(ex - sx, ey - sy, -awrad, true, arraow_len);
        x_3 = (float) (ex - arrXY_1[0]); // (x3,y3)是第�?端点
        y_3 = (float) (ey - arrXY_1[1]);
        x_4 = (float) (ex - arrXY_2[0]); // (x4,y4)是第二端�?
        y_4 = (float) (ey - arrXY_2[1]);
        Path triangle = new Path();
        triangle.moveTo(ex, ey);
        triangle.lineTo(x_3, y_3);
        triangle.lineTo(x_4, y_4);
        triangle.close();
        canvas.drawPath(triangle, paint);
    }

    // 计算 向量（px,py�? 旋转ang角度后的新长�?
    public static double[] rotateVec(float px, float py, double ang,
                                     boolean isChLen, double newLen) {
        double mathstr[] = new double[2];
        // 矢量旋转函数，参数含义分别是x分量、y分量、旋转角、是否改变长度�?�新长度
        double vx = px * Math.cos(ang) - py * Math.sin(ang);
        double vy = px * Math.sin(ang) + py * Math.cos(ang);
        if (isChLen) {
            double d = Math.sqrt(vx * vx + vy * vy);
            vx = vx / d * newLen;
            vy = vy / d * newLen;
        }
        mathstr[0] = vx;
        mathstr[1] = vy;
        return mathstr;
    }

    public static void drawLine(Canvas canvas, float sx, float sy, float dx, float dy, Paint paint) {
        canvas.drawLine(sx, sy, dx, dy, paint);
    }

    public static void drawCircle(Canvas canvas, float cx, float cy, float radius, Paint paint) {
        canvas.drawCircle(cx, cy, radius, paint);
    }

    public static void drawRect(Canvas canvas, float sx, float sy, float dx, float dy, Paint paint) {

        // 保证　左上角　与　右下角　的对应关系
        if (sx < dx) {
            if (sy < dy) {
                canvas.drawRect(sx, sy, dx, dy, paint);
            } else {
                canvas.drawRect(sx, dy, dx, sy, paint);
            }
        } else {
            if (sy < dy) {
                canvas.drawRect(dx, sy, sx, dy, paint);
            } else {
                canvas.drawRect(dx, dy, sx, sy, paint);
            }
        }
    }

    /**
     * 计算点p2绕p1顺时针旋转的角度
     *
     * @param px1
     * @param py1
     * @param px2
     * @param py2
     * @return 旋转的角度
     */
    public static float computeAngle(float px1, float py1, float px2, float py2) {

        float x = px2 - px1;
        float y = py2 - py1;

        float arc = (float) Math.atan(y / x);

        float angle = (float) (arc / (Math.PI * 2) * 360);

        if (x >= 0 && y == 0) {
            angle = 0;
        } else if (x < 0 && y == 0) {
            angle = 180;
        } else if (x == 0 && y > 0) {
            angle = 90;
        } else if (x == 0 && y < 0) {
            angle = 270;
        } else if (x > 0 && y > 0) { // 1

        } else if (x < 0 && y > 0) { //2
            angle = 180 + angle;
        } else if (x < 0 && y < 0) { //3
            angle = 180 + angle;
        } else if (x > 0 && y < 0) { //4
            angle = 360 + angle;
        }

        LogUtil.i("hzw", "[" + px1 + "," + py1 + "]:[" + px2 + "," + py2 + "] = " + angle);

        return angle;
    }

    // xy为在涂鸦中旋转后的坐标，该函数逆向计算出未旋转前的坐标
    public static float[] restoreRotatePointInGraffiti(int nowDegree, int oldDegree, float x, float y, float mOriginalPivotX, float mOriginalPivotY) {
        int degree = nowDegree - oldDegree;
        if (degree != 0) {
            float px = mOriginalPivotX, py = mOriginalPivotY;
            if (oldDegree == 90 || oldDegree == 270) { //　交换中心点的xy坐标
                float t = px;
                px = py;
                py = t;
            }
            if (Math.abs(degree) == 90 || Math.abs(degree) == 270) {
                x -= (py - px);
                y -= -(py - px);
            }

            float[] coords = rotatePoint(-degree, x,
                    y, px, py);

            return coords;
        }
        return new float[]{x, y};
    }

    // 顺时针旋转
    public static float[] rotatePoint(int degree, float x, float y, float px, float py) {
        float[] coords = new float[2];
        /*角度变成弧度*/
        float radian = (float) (degree * Math.PI / 180);
        coords[0] = (float) ((x - px) * Math.cos(radian) - (y - py) * Math.sin(radian) + px);
        coords[1] = (float) ((x - px) * Math.sin(radian) + (y - py) * Math.cos(radian) + py);

        return coords;
    }

    public static float[] rotatePointInGraffiti(int nowDegree, int oldDegree, float x, float y, float mOriginalPivotX, float mOriginalPivotY) {
        int degree = nowDegree - oldDegree;
        if (degree != 0) {
            float px = mOriginalPivotX, py = mOriginalPivotY;
            if (oldDegree == 90 || oldDegree == 270) { //　交换中心点的xy坐标
                float t = px;
                px = py;
                py = t;
            }

            float[] coords = rotatePoint(degree, x,
                    y, px, py); // 绕（px,py）旋转
            if (Math.abs(degree) == 90 || Math.abs(degree) == 270) { // 偏移
                coords[0] += (py - px);
                coords[1] += -(py - px);
            }
            return coords;
        }
        return new float[]{x, y};
    }

    /**
     * 1dp在图片在适应屏幕时的像素点数
     *
     * @return 根据此值可以获取相对于当前图片的像素单位，比如文字的大小默认为30*getPixelUnit()，那么在所有涂鸦图片上的默认大小在视觉上看到的大小都一样。
     */
    public static float getGraffitiPixelUnit() {
        return GRAFFITI_PIXEL_UNIT;
    }

    public static void setGraffitiPixelUnit(float graffitiPixelUnit) {
        DrawUtil.GRAFFITI_PIXEL_UNIT = graffitiPixelUnit;
    }

    public static void main(String[] args){

    }
}
