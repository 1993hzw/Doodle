package cn.hzw.graffiti;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;

/**
 * Created by huangziwei on 2017/3/16.
 */

public class GraffitiText extends GraffitiSelectableItemBase {


    private final static Paint paint = new Paint();
    private String mText;

    public GraffitiText(String text, float size, GraffitiColor color, int textRotate, int rotateDegree, float x, float y) {
        super(size, color, textRotate, rotateDegree, x, y);
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
        paint.setTextSize(getSize());
        paint.setStyle(Paint.Style.FILL);
        paint.getTextBounds(mText, 0, mText.length(), rect);
    }

    @Override
    public void doDraw(IGraffiti graffiti, Canvas canvas) {
        paint.setTextSize(getSize());
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText(mText, 0, 0, paint);
    }

}


