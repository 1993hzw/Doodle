package cn.hzw.graffiti;

import android.graphics.Bitmap;

/**
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
     * 选中字体
     *
     * @param selected 是否选中，false表示从选中变成不选中
     */
    void onSelectedText(boolean selected);

    /**
     * 编辑文本时回调，包括新建文本和修改文本
     *
     * @param showDialog true表示弹出输入框开始编辑，false表示隐藏输入框结束编辑
     * @param string     showDialog == true时，string表示需要编辑的文本，若为null则表示新建文本；
     *                   showDialog == false时，string表示编辑后的文本，若为null则表示取消了该次编辑
     */
    void onEditText(boolean showDialog, String string);
}
