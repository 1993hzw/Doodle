package cn.hzw.graffiti;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.*;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.forward.androids.utils.ImageUtils;
import cn.forward.androids.utils.Util;

import static cn.hzw.graffiti.HandWrite.MODE_COPY;


public class HandWritingActivity extends Activity implements View.OnClickListener {

    public static final String KEY_IMAGE_PATH = "key_image_path";
    public static final String SHARE_PREFERS = "graffiti_share_prefers";
    public final static String TAG = "Graffiti";

    private HandWrite handWrite = null;
    public static final int FINISH = 1;
    public static final int FAIL = -1;
    private SeekBar seekBar;
    private RelativeLayout paneLayout;
    private FrameLayout previewLayout;
    private Button btnCopy, btnEarer, btnPainter, btnArrow, btnPaintMode,
            btnShapeMode;
    private View btnFillCircle, btnHollCircle, btnFillRect, btnHollRect, btnLine;
    private SharedPreferences mSharedPreferences;


    private View btnAmplify, btnReduce;
    private final int WHAT_PAINT = 1;
    private View btnSetColor;
    private TextView mSeekBarProgress;

    private final static String KEY_PAINTER_COLOR = "painter_color";
    private final static String KEY_PAINTER_SIZE = "painter_size";

    private View barPaintMode, barShapeMode;
    private String picPath;

    private Bitmap galleryBitmap = null;


    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_PAINT:
                    handWrite.invalidate();
                    break;
            }
        }
    };

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
            Log.w(TAG, "HandWritingActivity::onCreate -> galleryBitmap is null");
            this.finish();
            return;
        }
        setContentView(R.layout.layout_hand_writing);
        mSharedPreferences = getSharedPreferences(SHARE_PREFERS, Context.MODE_PRIVATE);

        TextView textView = (TextView) findViewById(R.id.title_bar_title);
        textView.setOnTouchListener(new TitleOnTouchListener());

        previewLayout = (FrameLayout) findViewById(R.id.handwriting_preview);
        paneLayout = (RelativeLayout) findViewById(R.id.panel);
        handWrite = new HandWrite(this, galleryBitmap, new HandWrite.GraffitiListener() {
            @Override
            public void onSaved(Bitmap bitmap) {

            }

            @Override
            public void onError(int i, String msg) {

            }
        });
        previewLayout.addView(handWrite, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        seekBar = (SeekBar) findViewById(R.id.paint_size);
        btnAmplify = findViewById(R.id.btn_amplifer);
        btnReduce = findViewById(R.id.btn_reduce);
        btnSetColor = findViewById(R.id.btn_set_color);

        btnFillCircle = findViewById(R.id.btn_fill_circle);
        btnHollCircle = findViewById(R.id.btn_holl_circle);
        btnFillRect = findViewById(R.id.btn_fill_rect);
        btnHollRect = findViewById(R.id.btn_holl_rect);
        btnLine = findViewById(R.id.btn_line);

        int c = mSharedPreferences.getInt(KEY_PAINTER_COLOR, Color.RED);
        btnSetColor.setBackgroundColor(c);
        handWrite.setColor(c);
        MyOnTouchListener l = new MyOnTouchListener();
        btnAmplify.setOnTouchListener(l);
        btnReduce.setOnTouchListener(l);
        mSeekBarProgress = (TextView) findViewById(R.id.seekbar_progress);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (progress < 1) {
                    progress = 1;
                }
                handWrite.setSize(progress);
                mSeekBarProgress.setText(progress + "");
            }
        });
        seekBar.setMax(Util.getScreenWidth(getApplicationContext()) / 3); // 最大的画笔为屏幕3分之一
        seekBar.setProgress(mSharedPreferences.getInt(KEY_PAINTER_SIZE, 12));
        btnCopy = (Button) findViewById(R.id.btn_copy);
        btnEarer = (Button) findViewById(R.id.btn_earer);
        btnPainter = (Button) findViewById(R.id.btn_painter);
        btnArrow = (Button) findViewById(R.id.btn_arrow);
        btnPaintMode = (Button) findViewById(R.id.btn_paint_mode);
        btnShapeMode = (Button) findViewById(R.id.btn_shape_mode);
        barPaintMode = findViewById(R.id.bar_paint_mode);
        barShapeMode = findViewById(R.id.bar_shape_mode);
        choosePaintMode(btnPaintMode);
        choosePanit(btnPainter);

        findViewById(R.id.btn_back).setOnClickListener(this);
        findViewById(R.id.title_bar_btn01).setOnClickListener(this);
        findViewById(R.id.title_bar_btn02).setOnClickListener(this);

    }

    private void showToast(int id) {
        Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 隐藏画板
     */
    private void hidePanel() {
        View view = findViewById(R.id.title_bar_btn01);
        view.setSelected(!view.isSelected());
        if (paneLayout.getVisibility() == View.VISIBLE) {
            paneLayout.setVisibility(View.GONE);
        } else {
            paneLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 撤销
     *
     * @param v
     */
    public void undo(View v) {
        handWrite.undo();
    }

    private float scale, maxSacle = 3;
    private final int timeSpan = 80;

    /**
     * 缩放
     *
     * @param v
     */
    public void scalePic(View v) {
        if (isScaling)
            return;
        isScaling = true;

        if (v.getId() == R.id.btn_amplifer) {
            new Thread(new Runnable() {
                public void run() {
                    do {
                        scale += 0.1f;
                        if (scale > maxSacle) {
                            scale = maxSacle;
                            isScaling = false;
                        }
                        handWrite.judgePic();
                        myHandler.sendEmptyMessage(WHAT_PAINT);
                        try {
                            Thread.sleep(timeSpan);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } while (isScaling);

                }
            }).start();
        } else if (v.getId() == R.id.btn_reduce) {
            new Thread(new Runnable() {
                public void run() {
                    do {
                        scale -= 0.1f;
                        if (scale < 0.5f) {
                            scale = 0.5f;
                            isScaling = false;
                        }
                        handWrite.judgePic();
                        myHandler.sendEmptyMessage(WHAT_PAINT);
                        try {
                            Thread.sleep(timeSpan);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } while (isScaling);
                }
            }).start();
        }
        handWrite.invalidate();
    }

    /**
     * 移动图片
     *
     * @param v
     */
    public void movePic(View v) {
        handWrite.setMovingPic(!handWrite.isMovingPic());
        v.setSelected(handWrite.isMovingPic());
        if (handWrite.isMovingPic()) {
            showToast(R.string.moving_pic);
        }
    }

    /**
     * 居中图片
     *
     * @param v
     */
    public void centrePic(View v) {
        isScaling = true;
        if (scale > 1) {
            new Thread(new Runnable() {
                public void run() {
                    do {
                        scale -= 0.2f;
                        if (scale <= 1) {
                            scale = 1;
                            isScaling = false;
                        }
                        handWrite.judgePic();
                        myHandler.sendEmptyMessage(WHAT_PAINT);
                        try {
                            Thread.sleep(timeSpan / 2);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } while (isScaling);

                }
            }).start();
        } else if (scale < 1) {
            new Thread(new Runnable() {
                public void run() {
                    do {
                        scale += 0.2f;
                        if (scale >= 1) {
                            scale = 1;
                            isScaling = false;
                        }
                        handWrite.judgePic();
                        myHandler.sendEmptyMessage(WHAT_PAINT);
                        try {
                            Thread.sleep(timeSpan / 2);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } while (isScaling);
                }
            }).start();
        } else {
            isScaling = false;
        }
    }


    public void choosePaintMode(View v) {
        if (v.isSelected())
            return;
        v.setSelected(true);
        barPaintMode.setVisibility(View.VISIBLE);
        barShapeMode.setVisibility(View.GONE);
        btnShapeMode.setSelected(!v.isSelected());
        btnEarer.setSelected(!v.isSelected());
        switch (handWrite.getMode()) {
            case MODE_COPY:
                chooseCopy(null);
                break;
            case HandWrite.Mode_PAINTER:
            default:
                choosePanit(null);
                break;
        }
    }

    public void chooseShapeMode(View v) {
        if (v.isSelected())
            return;
        v.setSelected(true);
        barPaintMode.setVisibility(View.GONE);
        barShapeMode.setVisibility(View.VISIBLE);
        btnEarer.setSelected(!v.isSelected());
        btnPaintMode.setSelected(!v.isSelected());
        if (handWrite.getShape() == -1) {
            chooseShape(btnArrow);
        }
        handWrite.setHasArrow(true);
    }

    public void chooseShape(View v) {
        v.setSelected(true);
        if (v.getId() == R.id.btn_arrow) {
            handWrite.setShape(HandWrite.MODE_ARROW);
            btnFillCircle.setSelected(false);
            btnHollCircle.setSelected(false);
            btnFillRect.setSelected(false);
            btnHollRect.setSelected(false);
            btnLine.setSelected(false);
        } else if (v.getId() == R.id.btn_fill_circle) {
            handWrite.setShape(HandWrite.MODE_FILL_CIRCLE);
            btnArrow.setSelected(false);
            btnHollCircle.setSelected(false);
            btnFillRect.setSelected(false);
            btnHollRect.setSelected(false);
            btnLine.setSelected(false);
        } else if (v.getId() == R.id.btn_holl_circle) {
            handWrite.setShape(HandWrite.MODE_HOLL_CIRCLE);
            btnFillCircle.setSelected(false);
            btnArrow.setSelected(false);
            btnFillRect.setSelected(false);
            btnHollRect.setSelected(false);
            btnLine.setSelected(false);
        } else if (v.getId() == R.id.btn_fill_rect) {
            handWrite.setShape(HandWrite.MODE_FILL_RECT);
            btnFillCircle.setSelected(false);
            btnHollCircle.setSelected(false);
            btnArrow.setSelected(false);
            btnHollRect.setSelected(false);
            btnLine.setSelected(false);
        } else if (v.getId() == R.id.btn_holl_rect) {
            handWrite.setShape(HandWrite.MODE_HOLL_RECT);
            btnFillCircle.setSelected(false);
            btnHollCircle.setSelected(false);
            btnFillRect.setSelected(false);
            btnArrow.setSelected(false);
            btnLine.setSelected(false);
        } else if (v.getId() == R.id.btn_line) {
            handWrite.setShape(HandWrite.MODE_LINE);
            btnFillCircle.setSelected(false);
            btnHollCircle.setSelected(false);
            btnFillRect.setSelected(false);
            btnArrow.setSelected(false);
            btnHollRect.setSelected(false);
        }
    }

    public void chooseCopy(View v) {
        btnCopy.setSelected(true);
        handWrite.setHasCopy(true);
        btnPainter.setSelected(false);
        handWrite.setMode(HandWrite.MODE_COPY);
    }

    public void choosePanit(View v) {
        btnPainter.setSelected(true);
        handWrite.setHasPaint(true);
        btnCopy.setSelected(false);
        handWrite.setMode(HandWrite.Mode_PAINTER);
    }

    public void chooseEarer(View v) {
        btnEarer.setSelected(true);
        handWrite.setHasEarer(true);
        btnPaintMode.setSelected(false);
        btnShapeMode.setSelected(false);
        barPaintMode.setVisibility(View.GONE);
        barShapeMode.setVisibility(View.GONE);
    }

    public void setColor(View v) {
        new ColorPickerDialog(this, handWrite.getColor(), "画笔颜色",
                new ColorPickerDialog.OnColorChangedListener() {
                    public void colorChanged(int color) {
                        btnSetColor.setBackgroundColor(color);
                        handWrite.setColor(color);
                    }
                }).show();
        ;
    }

    private void save() {
        handWrite.save();
        this.finish();
    }

    public void clear(View v) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.clear_screen)
                .setMessage(R.string.cant_undo_after_clearing)
                .setPositiveButton(R.string.enter, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        handWrite.clear();
                        showToast(R.string.successed);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void back() {
        if (!handWrite.isModified()) {
            this.finish();
            return;
        }

        new AlertDialog.Builder(this).setTitle(R.string.saving_picture)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(R.string.enter, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        save();
                        HandWritingActivity.this.finish();
                    }
                })
                .setNegativeButton(R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
//                        handWrite.originalBitmap.recycle();
                        HandWritingActivity.this.finish();
                    }
                }).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            back();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*Editor editor = mSharedPreferences.edit();
        editor.putInt(KEY_PAINTER_SIZE, handWrite.radius);
        editor.putInt(KEY_PAINTER_COLOR, handWrite.curColor);
        editor.commit();*/
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_back) {
            back();
        } else if (v.getId() == R.id.title_bar_btn02) {
            save();
        } else if (v.getId() == R.id.title_bar_btn01) {
            hidePanel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private class TitleOnTouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    handWrite.setJustDrawOriginal(true);
                    v.setSelected(true);
                    handWrite.invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    handWrite.setJustDrawOriginal(false);
                    v.setSelected(false);
                    handWrite.invalidate();
                    break;
            }
            return true;
        }
    }

    private boolean isScaling = false;

    private class MyOnTouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    scalePic(v);
                    v.setSelected(true);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isScaling = false;
                    v.setSelected(false);
                    break;
            }
            return true;
        }
    }

    public static String getFilePath(String originPath, String suffix) {
        int i = originPath.lastIndexOf(".");
        String name = originPath.substring(0, i) + " " + suffix + originPath.substring(i);
        if (!new File(name).exists()) {//不存在
            return name;
        } else {
            int n = 1;
            do {
                n++;
                name = originPath.substring(0, i) + " " + suffix + n + originPath.substring(i);
            } while (new File(name).exists());
        }
        return name;
    }


}

