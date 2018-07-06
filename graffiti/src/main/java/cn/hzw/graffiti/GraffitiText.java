package cn.hzw.graffiti;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;

import cn.hzw.graffiti.core.IGraffiti;

/**
 * 文字item
 * Created by huangziwei on 2017/3/16.
 */

public class GraffitiText extends GraffitiSelectableItemBase {


    private final Paint mPaint = new Paint();
    private String mText;

    public GraffitiText(IGraffiti graffiti, String text, float size, GraffitiColor color, int textRotate, float x, float y) {
        super(graffiti, textRotate, x, y);
        this.mText = text;
        setSize(size);
        setColor(color);
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
        mPaint.setTextSize(getSize());
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.getTextBounds(mText, 0, mText.length(), rect);
    }

    @Override
    public void doDraw(Canvas canvas) {
        getColor().initColor(mPaint, null);
        mPaint.setTextSize(getSize());
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawText(mText, 0, 0, mPaint);
    }

}


