package cn.hzw.graffiti.core;

import android.graphics.Canvas;

public interface IGraffitiPen {

    /**
     * 画笔制作的item是否可选，用于旋转、移动等特定操作
     * @return
     */
    public boolean isSelectable();

    public void draw(Canvas canvas);

}
