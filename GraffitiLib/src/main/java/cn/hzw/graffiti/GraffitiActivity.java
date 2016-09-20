package cn.hzw.graffiti;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

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

    private View.OnClickListener mOnClickListener;

    private SeekBar mPaintSizeBar;
    private TextView mPaintSize;

    private View mBtnColor;

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
        mOnClickListener = new GraffitiOnClickListener();
        initView();
    }

    private void initView() {
        findViewById(R.id.btn_pen_hand).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_pen_copy).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_pen_eraser).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_hand_write).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_arrow).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_line).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_holl_circle).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_fill_circle).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_holl_rect).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_fill_rect).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_clear).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_undo).setOnClickListener(mOnClickListener);
        mBtnColor = findViewById(R.id.btn_set_color);
        mBtnColor.setOnClickListener(mOnClickListener);
        mBtnColor.setBackgroundColor(mGraffitiView.getGraffitiColor().getColor());

        findViewById(R.id.btn_pen_hand).performClick();
        findViewById(R.id.btn_hand_write).performClick();

        mPaintSizeBar = (SeekBar) findViewById(R.id.paint_size);
        mPaintSize = (TextView) findViewById(R.id.paint_size_text);

        mPaintSizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPaintSize.setText("" + progress);
                mGraffitiView.setPaintSize(progress);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mPaintSizeBar.setProgress((int) mGraffitiView.getPaintSize());
    }

    private class GraffitiOnClickListener implements View.OnClickListener {

        private View mLastPenView, mLastShapeView;
        private boolean mDone = false;

        @Override
        public void onClick(View v) {
            mDone = false;
            if (v.getId() == R.id.btn_pen_hand) {
                mGraffitiView.setPen(GraffitiView.Pen.HAND);
                mDone = true;
            } else if (v.getId() == R.id.btn_pen_copy) {
                mGraffitiView.setPen(GraffitiView.Pen.COPY);
                mDone = true;
            } else if (v.getId() == R.id.btn_pen_eraser) {
                mGraffitiView.setPen(GraffitiView.Pen.ERASER);
                mDone = true;
            }
            if (mDone) {
                if (mLastPenView != null) {
                    mLastPenView.setSelected(false);
                }
                v.setSelected(true);
                mLastPenView = v;
                return;
            }

            if (v.getId() == R.id.btn_clear) {
                mGraffitiView.clear();
                mDone = true;
            } else if (v.getId() == R.id.btn_undo) {
                mGraffitiView.undo();
                mDone = true;
            } else if (v.getId() == R.id.btn_set_color) {
                new ColorPickerDialog(GraffitiActivity.this, mGraffitiView.getGraffitiColor().getColor(), "画笔颜色",
                        new ColorPickerDialog.OnColorChangedListener() {
                            public void colorChanged(int color) {
                                mBtnColor.setBackgroundColor(color);
                                mGraffitiView.setColor(color);
                            }

                            @Override
                            public void colorChanged(Drawable color) {
                                mBtnColor.setBackgroundDrawable(color);
                                mGraffitiView.setColor(ImageUtils.getBitmapFromDrawable(color));
                            }
                        }).show();
                mDone = true;
            }
            if (mDone) {
                return;
            }


            if (v.getId() == R.id.btn_hand_write) {
                mGraffitiView.setShape(GraffitiView.Shape.HAND_WRITE);
            } else if (v.getId() == R.id.btn_arrow) {
                mGraffitiView.setShape(GraffitiView.Shape.ARROW);
            } else if (v.getId() == R.id.btn_line) {
                mGraffitiView.setShape(GraffitiView.Shape.LINE);
            } else if (v.getId() == R.id.btn_holl_circle) {
                mGraffitiView.setShape(GraffitiView.Shape.HOLLOW_CIRCLE);
            } else if (v.getId() == R.id.btn_fill_circle) {
                mGraffitiView.setShape(GraffitiView.Shape.FILL_CIRCLE);
            } else if (v.getId() == R.id.btn_holl_rect) {
                mGraffitiView.setShape(GraffitiView.Shape.HOLLOW_RECT);
            } else if (v.getId() == R.id.btn_fill_rect) {
                mGraffitiView.setShape(GraffitiView.Shape.FILL_RECT);
            }

            if (mLastShapeView != null) {
                mLastShapeView.setSelected(false);
            }
            v.setSelected(true);
            mLastShapeView = v;
        }
    }
}
