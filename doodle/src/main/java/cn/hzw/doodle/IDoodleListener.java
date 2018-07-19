package cn.hzw.doodle;

import android.graphics.Bitmap;

import cn.hzw.doodle.core.IDoodleSelectableItem;

/**
 * 涂鸦框架相关的回调
 * Created by huangziwei on 2017/3/17.
 */

public interface IDoodleListener {

    /**
     * 保存图片
     *
     * @param doodleBitmap       涂鸦后的图片
     * @param callback  保存后的回调，如果需要继续涂鸦，必须调用该回调
     */
    void onSaved(Bitmap doodleBitmap, Runnable callback);

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
