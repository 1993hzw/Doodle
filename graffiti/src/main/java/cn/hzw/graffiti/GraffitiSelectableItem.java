package cn.hzw.graffiti;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * 可选择的涂鸦条目，例如文字、图片
 * Created by huangziwei on 2017/7/16.
 */

public interface GraffitiSelectableItem {

    public final static int ITEM_CAN_ROTATE_BOUND = 80;

    public float getSize();

    public void setSize(float size);

    public void setXy(int currentRotate, float x, float y);

    public float[] getXy(int currentDegree);

    int getGraffitiRotate();

    public void setItemRotate(float textRotate);

    public float getItemRotate();

    // 判断xy是否在文字范围内
    public boolean isInIt(int currentRotate, float x, float y, GraffitiView.Pen pen);

    public boolean isCanRotate(int currentRotate, float x, float y);

    public Rect getBounds(int currentRotate);

    public GraffitiColor getColor();

    public void setColor(GraffitiColor color);

    void draw(Canvas canvas, GraffitiView graffitiView, Paint paint);
}
