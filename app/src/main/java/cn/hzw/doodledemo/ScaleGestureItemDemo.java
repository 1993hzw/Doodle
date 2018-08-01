package cn.hzw.doodledemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import cn.forward.androids.ScaleGestureDetectorApi27;
import cn.hzw.doodle.DoodleColor;
import cn.hzw.doodle.DoodleOnTouchGestureListener;
import cn.hzw.doodle.DoodlePen;
import cn.hzw.doodle.DoodleShape;
import cn.hzw.doodle.DoodleText;
import cn.hzw.doodle.DoodleTouchDetector;
import cn.hzw.doodle.DoodleView;
import cn.hzw.doodle.IDoodleListener;
import cn.hzw.doodle.core.IDoodle;
import cn.hzw.doodle.core.IDoodleItem;
import cn.hzw.doodle.core.IDoodleSelectableItem;
import cn.hzw.doodle.dialog.DialogController;

/**
 * Change text's size by scale gesture
 * 通过手势缩放item
 */
public class ScaleGestureItemDemo extends Activity {
    private DoodleView doodleView;
    private DoodleOnTouchGestureListener touchGestureListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scale_gesture_item);

        // step 1
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.thelittleprince2);
        doodleView = new DoodleView(this, bitmap, new IDoodleListener() {
            @Override
            public void onSaved(IDoodle doodle, Bitmap doodleBitmap, Runnable callback) {
                Toast.makeText(ScaleGestureItemDemo.this, "onSaved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReady(IDoodle doodle) {
                doodle.setSize(40 * doodle.getUnitSize());
                IDoodleSelectableItem item = new DoodleText(doodle, "Hello, world", doodle.getSize(), doodle.getColor(), 10, doodleView.getBitmap().getHeight()/2);
                touchGestureListener.setSelectedItem(item);
                doodle.addItem(item);
            }
        });

        // step 2
        touchGestureListener = new ScaleItemOnTouchGestureListener(doodleView, new DoodleOnTouchGestureListener.ISelectionListener() {
            @Override
            public void onSelectedItem(IDoodle doodle, IDoodleSelectableItem selectableItem, boolean selected) {
            }

            @Override
            public void onCreateSelectableItem(final IDoodle doodle, final float x, final float y) {
                if (doodle.getPen() != DoodlePen.TEXT) {
                    return;
                }

                DialogController.showInputTextDialog(ScaleGestureItemDemo.this, null,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String text = (v.getTag() + "").trim();
                                if (TextUtils.isEmpty(text)) {
                                    return;
                                }
                                IDoodleSelectableItem item = new DoodleText(doodle, text, doodle.getSize(), doodle.getColor().copy(), x, y);
                                doodle.addItem(item);
                                touchGestureListener.setSelectedItem(item);
                                doodle.refresh();
                            }
                        }, null);
            }
        });
        DoodleTouchDetector touchDetector = new DoodleTouchDetector(this, touchGestureListener);
        doodleView.setDefaultTouchDetector(touchDetector);

        // step 3
        doodleView.setPen(DoodlePen.TEXT);
        doodleView.setShape(DoodleShape.HAND_WRITE);
        doodleView.setColor(new DoodleColor(Color.RED));

        // step 4
        ViewGroup container = findViewById(R.id.doodle_container);
        container.addView(doodleView);
    }

    /**
     * Change selected item's size by scale gesture.
     * Note that this class extends DoodleOnTouchGestureListener and does not affect the original gesture logic.
     * <p>
     * 注意，这里继承了DoodleOnTouchGestureListener，不影响原有的手势逻辑
     */
    private static class ScaleItemOnTouchGestureListener extends DoodleOnTouchGestureListener {

        public ScaleItemOnTouchGestureListener(DoodleView doodle, ISelectionListener listener) {
            super(doodle, listener);
        }

        @Override
        public boolean onScale(ScaleGestureDetectorApi27 detector) {
            if (getSelectedItem() != null) {
                IDoodleItem item = getSelectedItem();
                item.setSize(item.getSize() * detector.getScaleFactor());
                return true;
            } else {
                return super.onScale(detector);
            }
        }
    }

}
