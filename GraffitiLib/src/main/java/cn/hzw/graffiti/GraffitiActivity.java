package cn.hzw.graffiti;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import cn.forward.androids.utils.ImageUtils;

/**
 * Created by Administrator on 2016/9/3.
 */
public class GraffitiActivity extends Activity {

    public static final String KEY_IMAGE_PATH = "key_image_path";
    private String picPath;
    private Bitmap galleryBitmap;

    private FrameLayout mFrameLayout;
    private GraffitiView mGraffitiView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        picPath = getIntent().getExtras().getString(KEY_IMAGE_PATH);
        if (picPath == null) {
            this.finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        galleryBitmap = ImageUtils.createBitmapFromPath(picPath, this);
        if (galleryBitmap == null) {
            this.finish();
            return;
        }
        setContentView(R.layout.layout_graffiti);
        mFrameLayout = (FrameLayout) findViewById(R.id.graffiti_container);

        mGraffitiView = new GraffitiView(this, galleryBitmap, new HandWrite.GraffitiListener() {
            @Override
            public void onSaved(Bitmap bitmap) {

            }

            @Override
            public void onError(int i, String msg) {

            }
        });
        mFrameLayout.addView(mGraffitiView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

    }
}
