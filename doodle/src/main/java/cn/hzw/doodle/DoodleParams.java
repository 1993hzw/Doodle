package cn.hzw.doodle;


import android.app.Activity;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import cn.hzw.doodle.core.IDoodle;

/**
 * 涂鸦参数
 */
public class DoodleParams implements Parcelable {

    /**
     * 图片路径
     */
    public String mImagePath;
    /**
     * 　保存路径，如果为null，则图片保存在根目录下/DCIM/Doodle/
     */
    public String mSavePath;
    /**
     * 　保存路径是否为目录，如果为目录，则在该目录生成由时间戳组成的图片名称
     */
    public boolean mSavePathIsDir;

    /**
     * 触摸时，图片区域外是否绘制涂鸦轨迹
     */
    public boolean mIsDrawableOutside;

    /**
     * 涂鸦时（手指按下）隐藏设置面板的延长时间(ms)，当小于等于0时则为不尝试隐藏面板（即保持面板当前状态不变）;当大于0时表示需要触摸屏幕超过一定时间后才隐藏
     * 或者手指抬起时展示面板的延长时间(ms)，或者表示需要离开屏幕超过一定时间后才展示
     * 默认为200ms
     */
    public long mChangePanelVisibilityDelay = 200; //ms

    /**
     * 设置放大镜的倍数，当小于等于0时表示不使用放大器功能
     * 放大器只有在设置面板被隐藏的时候才会出现
     * 默认为2.5倍
     */
    public float mZoomerScale = 2.5f;

    /**
     * 是否全屏显示，即是否隐藏状态栏
     * 默认为false，表示状态栏继承应用样式
     */
    public boolean mIsFullScreen = false;

    /**
     * 初始化的画笔大小,单位为像素
     */
    public float mPaintPixelSize = -1;

    /**
     * 初始化的画笔大小,单位为涂鸦坐标系中的单位大小，该单位参考dp，独立于图片
     * mPaintUnitSize值优先于mPaintPixelSize
     */
    public float mPaintUnitSize = -1;

    /**
     * 画布的最小/最大缩放倍数
     */
    public float mMinScale = DoodleView.MIN_SCALE;
    public float mMaxScale = DoodleView.MAX_SCALE;

    /**
     * 初始的画笔颜色
     */
    public int mPaintColor = Color.RED;

    public static final Creator<DoodleParams> CREATOR = new Creator<DoodleParams>() {
        @Override
        public DoodleParams createFromParcel(Parcel in) {
            DoodleParams params = new DoodleParams();
            params.mImagePath = in.readString();
            params.mSavePath = in.readString();
            params.mSavePathIsDir = in.readInt() == 1;
            params.mIsDrawableOutside = in.readInt() == 1;
            params.mChangePanelVisibilityDelay = in.readLong();
            params.mZoomerScale = in.readFloat();
            params.mIsFullScreen = in.readInt() == 1;
            params.mPaintPixelSize = in.readFloat();
            params.mPaintUnitSize = in.readFloat();
            params.mMinScale = in.readFloat();
            params.mMaxScale = in.readFloat();
            params.mPaintColor = in.readInt();

            return params;
        }

        @Override
        public DoodleParams[] newArray(int size) {
            return new DoodleParams[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mImagePath);
        dest.writeString(mSavePath);
        dest.writeInt(mSavePathIsDir ? 1 : 0);
        dest.writeInt(mIsDrawableOutside ? 1 : 0);
        dest.writeLong(mChangePanelVisibilityDelay);
        dest.writeFloat(mZoomerScale);
        dest.writeInt(mIsFullScreen ? 1 : 0);
        dest.writeFloat(mPaintPixelSize);
        dest.writeFloat(mPaintUnitSize);
        dest.writeFloat(mMinScale);
        dest.writeFloat(mMaxScale);
        dest.writeInt(mPaintColor);

    }

    @Override
    public int describeContents() {
        return 0;
    }

    private static DialogInterceptor sDialogInterceptor;

    /**
     * 设置涂鸦中对话框的拦截器，如点击返回按钮（或返回键）弹出保存对话框，可以进行拦截，弹出自定义的对话框
     * 切记：需要自行处理内存泄漏的问题！！！
     */
    public static void setDialogInterceptor(DialogInterceptor interceptor) {
        sDialogInterceptor = interceptor;
    }

    public static DialogInterceptor getDialogInterceptor() {
        return sDialogInterceptor;
    }

    public enum DialogType {
        SAVE, CLEAR_ALL, COLOR_PICKER;
    }

    public interface DialogInterceptor {
        /**
         * @param activity
         * @param doodle
         * @param dialogType   对话框类型
         * @return 返回true表示拦截
         */
        boolean onShow(Activity activity, IDoodle doodle, DialogType dialogType);
    }
}
