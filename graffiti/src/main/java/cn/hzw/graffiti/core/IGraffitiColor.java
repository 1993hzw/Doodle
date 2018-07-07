package cn.hzw.graffiti.core;

import android.graphics.Paint;

public interface IGraffitiColor {

    /**
     * 深度拷贝
     * @return
     */
    public IGraffitiColor copy();

    /**
     * 配置画笔
     * @param graffitiItem
     * @param paint
     */
    public void config(IGraffitiItem graffitiItem, Paint paint);
}
