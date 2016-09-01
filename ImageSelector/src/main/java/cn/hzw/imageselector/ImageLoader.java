package cn.hzw.imageselector;

import android.content.Context;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;

/**
 * Created by huangziwei on 16-9-1.
 */
public class ImageLoader {

    private static BitmapUtils mBitmapUtils;
    private static BitmapDisplayConfig mBitmapDisplayConfig;

    private static ImageLoader sInstance;

    public static ImageLoader getInstance(Context context) {
        if (sInstance == null) {
            synchronized (ImageLoader.class) {
                if (sInstance == null) {
                    sInstance = new ImageLoader(context);
                }
            }
        }
        return sInstance;
    }

    private ImageLoader(Context context) {
        context = context.getApplicationContext();
        int memoryCacheSize = (int) Runtime.getRuntime().maxMemory() / 6;
//        int memoryCacheSize = Math.round(context.getMemoryClass() * 1024.0F * 1024.0F / 6);

        mBitmapUtils = new BitmapUtils(context, null, memoryCacheSize);
        mBitmapDisplayConfig = new BitmapDisplayConfig();
        mBitmapDisplayConfig.setLoadFailedDrawable(context.getResources().getDrawable(R.drawable.loading));
        mBitmapDisplayConfig.setLoadingDrawable(context.getResources().getDrawable(R.drawable.loading));
       /* Animation animation = new AlphaAnimation(0, 1);
        animation.setDuration(100);
        mBitmapDisplayConfig.setAnimation(animation);*/
        mBitmapUtils.configDefaultDisplayConfig(mBitmapDisplayConfig);
    }

    public <T extends View> void display(T container, String uri) {
        mBitmapUtils.display(container, uri);
    }

}
