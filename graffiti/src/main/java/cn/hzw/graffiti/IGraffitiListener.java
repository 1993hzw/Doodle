package cn.hzw.graffiti;

import android.graphics.Bitmap;

import cn.hzw.graffiti.core.IGraffitiSelectableItem;

/**
 * 涂鸦框架相关的回调
 * Created by huangziwei on 2017/3/17.
 */

public interface IGraffitiListener {

    /**
     * 保存图片
     *
     * @param graffitiBitmap       涂鸦后的图片
     * @param callback  保存后的回调，如果需要继续涂鸦，必须调用该回调
     */
    void onSaved(Bitmap graffitiBitmap, Runnable callback);

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

}
