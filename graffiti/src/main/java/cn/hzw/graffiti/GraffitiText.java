package cn.hzw.graffiti;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;

/**
 * Created by huangziwei on 2017/3/16.
 */

public class GraffitiText extends GraffitiSelectableItem {


    private final static Paint sPaint = new Paint();
    private String mText;

    public GraffitiText(String text, float size, GraffitiColor color, int textRotate, int rotateDegree, float x, float y, float px, float py) {
        super(size, color, textRotate, rotateDegree, x, y, px, py);
        this.mText = text;
        resetBounds(getBounds());
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
        resetBounds(getBounds());
    }

    @Override
    public void resetBounds(Rect rect) {
        if (TextUtils.isEmpty(mText)) {
            return;
        }
        sPaint.setTextSize(getSize());
        sPaint.setStyle(Paint.Style.FILL);
        sPaint.getTextBounds(mText, 0, mText.length(), rect);
    }

    @Override
    public void draw(Canvas canvas, GraffitiView graffitiView, Paint paint) {
        paint.setTextSize(getSize());
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText(mText, 0, 0, paint);
    }

}


