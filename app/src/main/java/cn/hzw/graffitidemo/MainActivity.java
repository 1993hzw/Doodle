package cn.hzw.graffitidemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import cn.forward.androids.utils.LogUtil;
import cn.hzw.graffiti.GraffitiActivity;
import cn.hzw.imageselector.ImageLoader;
import cn.hzw.imageselector.ImageSelectorActivity;

public class MainActivity extends Activity {

    public static final int REQ_CODE_SELECT_IMAGE = 100;
    public static final int REQ_CODE_GRAFFITI = 101;
    private TextView mPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_select_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageSelectorActivity.startActivityForResult(REQ_CODE_SELECT_IMAGE, MainActivity.this, null, false);
            }
        });
        mPath = (TextView) findViewById(R.id.img_path);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SELECT_IMAGE) {
            if (data == null) {
                return;
            }
            ArrayList<String> list = data.getStringArrayListExtra(ImageSelectorActivity.KEY_PATH_LIST);
            if (list != null && list.size() > 0) {
                LogUtil.d("Graffiti", list.get(0));

                // 涂鸦参数
                GraffitiActivity.GraffitiParams params = new GraffitiActivity.GraffitiParams();
                // 图片路径
                params.mImagePath = list.get(0);

                /*
                // 橡皮擦底图，如果为null，则底图为当前图片路径
                params.mEraserPath = "/storage/emulated/0/tencent/MicroMsg/WeiXin/mmexport1485172092678.jpg";
                //  橡皮擦底图是否调整大小，如果为true则调整到跟当前涂鸦图片一样的大小．
                params.mEraserImageIsResizeable = true;
                // 设置放大镜的倍数，当小于等于0时表示不使用放大器功能.放大器只有在设置面板被隐藏的时候才会出现
                params.mAmplifierScale = 2.5f;
                */

                GraffitiActivity.startActivityForResult(MainActivity.this, params, REQ_CODE_GRAFFITI);
            }
        } else if (requestCode == REQ_CODE_GRAFFITI) {
            if (data == null) {
                return;
            }
            if (resultCode == GraffitiActivity.RESULT_OK) {
                String path = data.getStringExtra(GraffitiActivity.KEY_IMAGE_PATH);
                if (TextUtils.isEmpty(path)) {
                    return;
                }
                ImageLoader.getInstance(this).display(findViewById(R.id.img), path);
                mPath.setText(path);
            } else if (resultCode == GraffitiActivity.RESULT_ERROR) {
                Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
