package cn.hzw.graffiti;

import android.graphics.Canvas;

import cn.hzw.graffiti.core.IGraffitiPen;

/**
 * 常用画笔
 */
public enum GraffitiPen implements IGraffitiPen {

    HAND, // 手绘
    COPY, // 仿制
    ERASER, // 橡皮擦
    TEXT(true), // 文本
    BITMAP(true); // 贴图

    private boolean mIsSelectable = false; // 画笔绘制的item是否可选

    GraffitiPen() {
        this(false);
    }

    GraffitiPen(boolean isSelectable) {
        mIsSelectable = isSelectable;
    }

    /**
     * 画笔制作的item是否可选，用于旋转、移动等特定操作
     *
     * @return
     */
    public boolean isSelectable() {
        return mIsSelectable;
    }

    @Override
    public void draw(Canvas canvas) {

    }
}
