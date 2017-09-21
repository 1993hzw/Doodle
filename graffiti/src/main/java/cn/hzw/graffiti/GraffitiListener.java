package cn.hzw.graffiti;

import android.graphics.Bitmap;

/**
 * 涂鸦框架相关的回调
 * Created by huangziwei on 2017/3/17.
 */

public interface GraffitiListener {

    /**
     * 保存图片
     *
     * @param bitmap       涂鸦后的图片
     * @param bitmapEraser 橡皮擦底图
     */
    void onSaved(Bitmap bitmap, Bitmap bitmapEraser);

    /**
     * 出错
     *
     * @param i
     * @param msg
     */
    void onError(int i, String msg);

    /**
     * 准备工作已经完成
     */
    void onReady();

    /**
     * 选中
     *
     * @param selected 是否选中，false表示从选中变成不选中
     */
    void onSelectedItem(GraffitiSelectableItem selectableItem, boolean selected);

    /**
     * 新建一个可选的item
     * @param pen
     * @param x
     * @param y
     */
    void onCreateSelectableItem(GraffitiView.Pen pen, float x, float y);

}
