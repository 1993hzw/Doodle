package cn.hzw.doodledemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import cn.hzw.doodle.DoodleColor;
import cn.hzw.doodle.DoodleOnTouchGestureListener;
import cn.hzw.doodle.DoodleShape;
import cn.hzw.doodle.DoodleTouchDetector;
import cn.hzw.doodle.DoodleView;
import cn.hzw.doodle.IDoodleListener;
import cn.hzw.doodle.core.IDoodle;
import cn.hzw.doodle.core.IDoodleItem;
import cn.hzw.doodle.core.IDoodlePen;

/**
 * Mosaic effect
 */
public class MosaicDemo extends Activity {
    private DoodleView doodleView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mosaic);

        // step 1
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.thelittleprince2);
        doodleView = new DoodleView(this, bitmap, new IDoodleListener() {
            @Override
            public void onSaved(IDoodle doodle, Bitmap doodleBitmap, Runnable callback) {
                Toast.makeText(MosaicDemo.this, "onSaved", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onReady(IDoodle doodle) {
                doodle.setSize(30 * doodle.getUnitSize());
            }
        });

        // step 2
        DoodleOnTouchGestureListener touchGestureListener = new DoodleOnTouchGestureListener(doodleView, null);
        DoodleTouchDetector touchDetector = new DoodleTouchDetector(this, touchGestureListener);
        doodleView.setDefaultTouchDetector(touchDetector);

        // step 3
        doodleView.setPen(new MosaicPen());
        doodleView.setShape(DoodleShape.HAND_WRITE);
        findViewById(R.id.btn_mosaic_x3).performClick(); // see setMosaicLevel(View view)

        // step 4
        ViewGroup container = findViewById(R.id.doodle_container);
        container.addView(doodleView);
    }

    private DoodleColor getMosaicColor(float scale) {
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        Bitmap mosaicBitmap = Bitmap.createBitmap(doodleView.getBitmap(),
                0, 0, doodleView.getBitmap().getWidth(), doodleView.getBitmap().getHeight(), matrix, true);
        matrix.reset();
        matrix.setScale(1 / scale, 1 / scale);
        return new DoodleColor(mosaicBitmap, matrix);
    }

    /*
     Though setting a new pen here is not necessary, the design-based specification should do this.
     虽然这里设置新的画笔不是必要的，但是基于设计的规范应该这样做。马赛克画笔在概念上不同于其他画笔，
     */
    public void setMosaicLevel(View view) {
        if (view.getId() == R.id.btn_mosaic_x1) {
            doodleView.setColor(getMosaicColor(0.3f));
        } else if (view.getId() == R.id.btn_mosaic_x2) {
            doodleView.setColor(getMosaicColor(0.1f));
        } else if (view.getId() == R.id.btn_mosaic_x3) {
            doodleView.setColor(getMosaicColor(0.03f));
        }
    }

    private static class MosaicPen implements IDoodlePen {
        @Override
        public void config(IDoodleItem doodleItem, Paint paint) {
        }

        @Override
        public void drawHelpers(Canvas canvas, IDoodle doodle) {
        }

        @Override
        public IDoodlePen copy() {
            return this;
        }
    }
}
