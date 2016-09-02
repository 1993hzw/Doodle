package cn.hzw.graffitidemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import cn.hzw.graffiti.HandWritingActivity;
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
                Intent intent = new Intent(getApplicationContext(), HandWritingActivity.class);
                intent.putExtra(HandWritingActivity.KEY_IMAGE_PATH, list.get(0));
                startActivityForResult(intent, REQ_CODE_GRAFFITI);
            }
        } else if (requestCode == REQ_CODE_GRAFFITI) {
            if (data == null) {
                return;
            }
            String path = data.getStringExtra(HandWritingActivity.KEY_IMAGE_PATH);
            if (TextUtils.isEmpty(path)) {
                return;
            }
            ImageLoader.getInstance(this).display(findViewById(R.id.img), path);
            mPath.setText(path);
        }
    }
}
