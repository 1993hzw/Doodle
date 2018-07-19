package cn.hzw.doodledemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import cn.forward.androids.utils.LogUtil;
import cn.hzw.doodle.DoodleActivity;
import cn.hzw.doodle.DoodleParams;
import cn.hzw.doodle.DoodleView;
import cn.hzw.imageselector.ImageLoader;
import cn.hzw.imageselector.ImageSelectorActivity;

public class MainActivity extends Activity {

    public static final int REQ_CODE_SELECT_IMAGE = 100;
    public static final int REQ_CODE_DOODLE = 101;
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
                LogUtil.d("Doodle", list.get(0));

                // 涂鸦参数
                DoodleParams params = new DoodleParams();
                params.mIsFullScreen = true;
                // 图片路径
                params.mImagePath = list.get(0);
                // 初始画笔大小
                params.mPaintUnitSize = DoodleView.DEFAULT_SIZE;
                params.mAmplifierScale = 0;
                // 启动涂鸦页面
                DoodleActivity.startActivityForResult(MainActivity.this, params, REQ_CODE_DOODLE);
            }
        } else if (requestCode == REQ_CODE_DOODLE) {
            if (data == null) {
                return;
            }
            if (resultCode == DoodleActivity.RESULT_OK) {
                String path = data.getStringExtra(DoodleActivity.KEY_IMAGE_PATH);
                if (TextUtils.isEmpty(path)) {
                    return;
                }
                ImageLoader.getInstance(this).display(findViewById(R.id.img), path);
                mPath.setText(path);
            } else if (resultCode == DoodleActivity.RESULT_ERROR) {
                Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
