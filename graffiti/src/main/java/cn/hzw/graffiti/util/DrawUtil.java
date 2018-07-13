package cn.hzw.graffiti.util;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.FrameLayout;

import cn.forward.androids.utils.LogUtil;

/**
 * Created by Administrator on 2016/9/3.
 */
public class DrawUtil {

    // 涂鸦系统中的单位，根据图片居中时缩放倍数决定。一单位在视觉上的尺寸相同
//    public static float GRAFFITI_PIXEL_UNIT = 1;

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

    // 计算 向量（px,py) 旋转ang角度后的新长度
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

    // 顺时针旋转
    public static PointF rotatePoint(PointF coords, int degree, float x, float y, float px, float py) {
        /*角度变成弧度*/
        float radian = (float) (degree * Math.PI / 180);
        coords.x = (float) ((x - px) * Math.cos(radian) - (y - py) * Math.sin(radian) + px);
        coords.y = (float) ((x - px) * Math.sin(radian) + (y - py) * Math.cos(radian) + py);

        return coords;
    }

    public static void main(String[] args) {
        /*PointF pointF = new PointF(0,0);
        restoreRotatePointInGraffiti(pointF,90,0,0,0,100,100);
        System.out.printf(pointF.toString());*/
    }

    public static void assistActivity(Window activity) {
        new AndroidBug5497Workaround(activity);
    }

    public static class AndroidBug5497Workaround {

        // For more information, see https://issuetracker.google.com/issues/36911528
        // To use this class, simply invoke assistActivity() on an Activity that already has its content view set.

        private View mChildOfContent;
        private int usableHeightPrevious;
        private FrameLayout.LayoutParams frameLayoutParams;

        private AndroidBug5497Workaround(Window window) {
            FrameLayout content = (FrameLayout) window.findViewById(android.R.id.content);
            mChildOfContent = content.getChildAt(0);
            mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    possiblyResizeChildOfContent();
                }
            });
            frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
        }

        private void possiblyResizeChildOfContent() {
            int usableHeightNow = computeUsableHeight();
            if (usableHeightNow != usableHeightPrevious) {
                int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
                int heightDifference = usableHeightSansKeyboard - usableHeightNow;
                if (heightDifference > (usableHeightSansKeyboard / 4)) {
                    // keyboard probably just became visible
                    frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
                } else {
                    // keyboard probably just became hidden
                    frameLayoutParams.height = usableHeightSansKeyboard;
                }
                mChildOfContent.requestLayout();
                usableHeightPrevious = usableHeightNow;
            }
        }

        private int computeUsableHeight() {
            Rect r = new Rect();
            mChildOfContent.getWindowVisibleDisplayFrame(r);
            return r.bottom;
        }
    }
}
