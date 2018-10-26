package cn.hzw.doodledemo.guide;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.ViewGroup;

import cn.hzw.doodledemo.R;

public class DoodleGuideActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        // 初级涂鸦
        ViewGroup simpleContainer = findViewById(R.id.container_simple_doodle);
        SimpleDoodleView simpleDoodleView = new SimpleDoodleView(this);
        simpleContainer.addView(simpleDoodleView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // 中级涂鸦
        ViewGroup middleContainer = findViewById(R.id.container_middle_doodle);
        MiddleDoodleView middleDoodleView = new MiddleDoodleView(this);
        middleContainer.addView(middleDoodleView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // 高级级涂鸦
        ViewGroup advancedContainer = findViewById(R.id.container_advanced_doodle);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.thelittleprince2);
        AdvancedDoodleView advancedDoodleView = new AdvancedDoodleView(this, bitmap);
        advancedContainer.addView(advancedDoodleView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    }
}
