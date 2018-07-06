package cn.hzw.graffiti.core;

import android.graphics.Paint;

public interface IGraffitiColor {

    public IGraffitiColor copy();
    public void initColor(Paint paint, IGraffitiItem graffitiItem);
}
