package cn.hzw.doodle;

import android.graphics.Bitmap;

import cn.hzw.doodle.core.IDoodle;

/**
 * 涂鸦框架相关的回调
 * Created by huangziwei on 2017/3/17.
 */

public interface IDoodleListener {

    /**
     * called when save the doodled iamge. 保存涂鸦图像时调用
     *
     * @param doodle
     * @param doodleBitmap       涂鸦后的图片
     * @param callback  called after saving the bitmap, if you continue to doodle. 保存后的回调，如果需要继续涂鸦，必须调用该回调
     */
    void onSaved(IDoodle doodle, Bitmap doodleBitmap, Runnable callback);

    /**
     * called when it is ready to doodle because the view has been measured. Now, you can set size, color, pen, shape, etc.
     * 此时view已经测量完成，涂鸦前的准备工作已经完成，在这里可以设置大小、颜色、画笔、形状等。
     * @param doodle
     */
    void onReady(IDoodle doodle);

}
