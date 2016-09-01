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
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
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
    private float scale = 1;
    private float transX = 0, transY = 0;
    private View btnAmplify, btnReduce;
    private final int WHAT_PAINT = 1, WHAT_RESIZE = 0;
    private View btnSetColor;
    private TextView mSeekBarProgress;

    private final static String KEY_PAINTER_COLOR = "painter_color";
    private final static String KEY_PAINTER_SIZE = "painter_size";

    private View barPaintMode, barShapeMode;
    private String picPath;

    private Bitmap galleryBitmap = null;
    private boolean isJustDrawOriginal; // 是否只绘制原图

    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_PAINT:
                    handWrite.invalidate();
                    break;
                case WHAT_RESIZE:
                    if (previewLayout.getWidth() * previewLayout.getHeight() != 0) {
                        handWrite.setBG();
                    } else {
                        myHandler.sendEmptyMessageDelayed(0, 250);
                    }
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
        handWrite = new HandWrite(this);
        previewLayout.addView(handWrite);
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
        myHandler.sendEmptyMessageDelayed(WHAT_RESIZE, 250);

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

    private void hidePanel() {
        View view = findViewById(R.id.title_bar_btn01);
        view.setSelected(!view.isSelected());
        if (paneLayout.getVisibility() == View.VISIBLE) {
            paneLayout.setVisibility(View.GONE);
        } else {
            paneLayout.setVisibility(View.VISIBLE);
        }
    }

    public void undo(View v) {
        handWrite.undo();
    }

    private boolean isScaling = false, isMovingPic = false;
    private final int timeSpan = 80, maxSacle = 3;

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

    public void movePic(View v) {
        isMovingPic = !isMovingPic;
        v.setSelected(!v.isSelected());
        if (isMovingPic) {
            showToast(R.string.moving_pic);
        }
    }

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

    int lastPaintMode = -1;
    int lastShapeMode = -1;
    final int Mode_PAINTER = 1, MODE_COPY = 2, MODE_ARROW = 3,
            MODE_FILL_CIRCLE = 4, MODE_HOLL_CIRCLE = 5, MODE_FILL_RECT = 6,
            MODE_HOLL_RECT = 7, MODE_LINE = 8;

    public void choosePaintMode(View v) {
        if (v.isSelected())
            return;
        v.setSelected(true);
        barPaintMode.setVisibility(View.VISIBLE);
        barShapeMode.setVisibility(View.GONE);
        btnShapeMode.setSelected(!v.isSelected());
        btnEarer.setSelected(!v.isSelected());
        switch (lastPaintMode) {
            case MODE_COPY:
                chooseCopy(null);
                break;
            case Mode_PAINTER:
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
        if (lastShapeMode == -1) {
            chooseShape(btnArrow);
        }
        handWrite.setHasArrow(true);
    }

    public void chooseShape(View v) {
        v.setSelected(true);
        if (v.getId() == R.id.btn_arrow) {
            lastShapeMode = MODE_ARROW;
            btnFillCircle.setSelected(false);
            btnHollCircle.setSelected(false);
            btnFillRect.setSelected(false);
            btnHollRect.setSelected(false);
            btnLine.setSelected(false);
        } else if (v.getId() == R.id.btn_fill_circle) {
            lastShapeMode = MODE_FILL_CIRCLE;
            btnArrow.setSelected(false);
            btnHollCircle.setSelected(false);
            btnFillRect.setSelected(false);
            btnHollRect.setSelected(false);
            btnLine.setSelected(false);
        } else if (v.getId() == R.id.btn_holl_circle) {
            lastShapeMode = MODE_HOLL_CIRCLE;
            btnFillCircle.setSelected(false);
            btnArrow.setSelected(false);
            btnFillRect.setSelected(false);
            btnHollRect.setSelected(false);
            btnLine.setSelected(false);
        } else if (v.getId() == R.id.btn_fill_rect) {
            lastShapeMode = MODE_FILL_RECT;
            btnFillCircle.setSelected(false);
            btnHollCircle.setSelected(false);
            btnArrow.setSelected(false);
            btnHollRect.setSelected(false);
            btnLine.setSelected(false);
        } else if (v.getId() == R.id.btn_holl_rect) {
            lastShapeMode = MODE_HOLL_RECT;
            btnFillCircle.setSelected(false);
            btnHollCircle.setSelected(false);
            btnFillRect.setSelected(false);
            btnArrow.setSelected(false);
            btnLine.setSelected(false);
        } else if (v.getId() == R.id.btn_line) {
            lastShapeMode = MODE_LINE;
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
        lastPaintMode = MODE_COPY;
    }

    public void choosePanit(View v) {
        btnPainter.setSelected(true);
        handWrite.setHasPaint(true);
        btnCopy.setSelected(false);
        lastPaintMode = Mode_PAINTER;
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
        if (!handWrite.hasModified) {
            handWrite.originalBitmap.recycle();
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
                        handWrite.originalBitmap.recycle();
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
        Editor editor = mSharedPreferences.edit();
        editor.putInt(KEY_PAINTER_SIZE, handWrite.radius);
        editor.putInt(KEY_PAINTER_COLOR, handWrite.curColor);
        editor.commit();
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
        if (handWrite != null)
            handWrite.originalBitmap.recycle();
    }

    private class MyPath {
        public static final int TYPE_PAINT = 1, TYPE_EARER = 0, TYPE_COPY = 3,
                TYPE_ARROW = 4;
        public Path path;
        public Paint paint;
        public int type = -1;
        public Rect srcRect;
        public RectF dstRect;
        public PointF src, dst;
        public float arrowSize = 1;
        public int shapeMode;

        public MyPath(Path path, Paint paint, int type) {
            this.path = path;
            this.paint = paint;
            this.type = type;
        }

        public MyPath(Path path, Paint paint, int type, Rect srcRect,
                      RectF dstRect) {
            this(path, paint, type);
            this.srcRect = srcRect;
            this.dstRect = dstRect;
        }

        public MyPath(int type, Paint paint, float arrowSize, int shapeMode, PointF src,
                      PointF dst) {
            this.src = src;
            this.dst = dst;
            this.type = type;
            this.paint = paint;
            this.arrowSize = arrowSize;
            this.shapeMode = shapeMode;
        }
    }

    private class HandWrite extends View {

        private boolean hasModified = false;
        private Paint paint = null;
        private Bitmap originalBitmap = null;
        private float clickX = 0, clickY = 0;
        private float lastX = 0, lastY = 0;
        private float startX = 0, startY = 0;
        private int color = Color.RED;
        private Canvas myCanvas;
        private Context context;
        private float n;
        private int radius;
        private Path earer;
        private boolean hasEarer, hasCopy, hasPaint, hasArrow;
        private Rect srcRect;
        private RectF dstRect;
        private int clickTimes = 1;// 用于判断在copy模式�?
        private Paint locationPaint, redPaint, bluePaint;
        private MyLocation myLocation;
        // private Path mPath;// 画笔路径
        private CopyOnWriteArrayList<MyPath> pathStack = new CopyOnWriteArrayList<MyPath>();
        private CopyOnWriteArrayList<MyPath> pathStackBackup = new CopyOnWriteArrayList<MyPath>();

        public HandWrite(Context context) {
            super(context);
            this.context = context;
            init();
        }

        private int curColor;

        public void setColor(int c) {
            curColor = c;
            paint = new Paint();
            paint.setStyle(Style.STROKE);
            paint.setAntiAlias(true);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);// 圆滑
            paint.setColor(c);
            paint.setStrokeWidth(radius);
        }

        public int getColor() {
            return curColor;
        }

        public void setSize(int s) {
            radius = s;
            paint = new Paint();
            paint.setStyle(Style.STROKE);
            paint.setAntiAlias(true);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);// 圆滑
            paint.setColor(curColor);
            paint.setStrokeWidth(radius);
            invalidate();
        }

        float oldCopyLastX, oldCopyLastY, oldCopyClikX, oldCopyClickY;

        public void setHasArrow(boolean b) {
            if (hasCopy) {
                oldCopyLastX = lastX;
                oldCopyLastY = lastY;
                oldCopyClikX = clickX;
                oldCopyClickY = clickY;
            }
            hasArrow = b;
            hasPaint = !b;
            hasCopy = !b;
            hasEarer = !b;
            isMovingCopy = true;
            invalidate();
            isMovingCopy = false;
        }

        public void setHasEarer(boolean b) {
            if (hasCopy) {
                oldCopyLastX = lastX;
                oldCopyLastY = lastY;
                oldCopyClikX = clickX;
                oldCopyClickY = clickY;
            }
            hasEarer = b;
            hasArrow = !b;
            hasPaint = !b;
            hasCopy = !b;
            isMovingCopy = true;
            invalidate();
            isMovingCopy = false;
        }

        public void setHasCopy(boolean b) {
            lastX = oldCopyLastX;
            lastY = oldCopyLastY;
            clickX = oldCopyClikX;
            clickY = oldCopyClickY;
            hasCopy = b;
            hasArrow = !b;
            hasPaint = !b;
            hasEarer = !b;
            isMovingCopy = true;
            if (radius < 35)
                seekBar.setProgress(35);

            if (myLocation == null) {
                myLocation = new MyLocation();// 在这里初始化是为了第�?次打�?仿制功能�?
                // 能显示mylocation，因为构造方法会调用update
            }

            invalidate();
            isMovingCopy = false;

        }

        public void setHasPaint(boolean b) {
            if (hasCopy) {
                oldCopyLastX = lastX;
                oldCopyLastY = lastY;
                oldCopyClikX = clickX;
                oldCopyClickY = clickY;
            }
            hasPaint = b;
            hasArrow = !b;
            hasCopy = !b;
            hasEarer = !b;
            isMovingCopy = true;
            invalidate();
            isMovingCopy = false;
        }

        public void init() {
            try {
                originalBitmap = Bitmap.createBitmap(galleryBitmap.getWidth(),
                        galleryBitmap.getHeight(), Config.ARGB_8888);
            } catch (Error e) {
                showToast("打开图片失败");
                HandWritingActivity.this.finish();
                return;
            }
            myCanvas = new Canvas(originalBitmap);
            hasModified = false;

            setColor(color);
            // earer = new Path();
            locationPaint = new Paint();
            locationPaint.setColor(Color.LTGRAY);
            oldCopyLastX = oldCopyLastY = 200;

            // mPath = new Path();

            redPaint = new Paint();
            redPaint.setColor(Color.RED);
            bluePaint = new Paint();
            bluePaint.setColor(Color.CYAN);

            // 初始
            srcRect = new Rect(0, 0, galleryBitmap.getWidth(),
                    galleryBitmap.getHeight());
            float x = (startX - lastX) / (n * scale);
            float y = (startY - lastY) / (n * scale);
            dstRect = new RectF(srcRect.left + x, srcRect.top + y,
                    srcRect.right + x, srcRect.bottom + y);

            arrowLineSize = Util.dip2px(context, 5);
        }

        public void move(float spanX, float spanY) {
            transX += spanX;
            transY += spanY;
            judgePic();
            invalidate();
        }

        public void judgePic() {
            if (scale > 1) {
                if (transX > 0)
                    transX = 0;
                else if (transX + width * scale < width)
                    transX = width - width * scale;
                if (transY > 0)
                    transY = 0;
                else if (transY + height * scale < height)
                    transY = height - height * scale;
            } else {
                if (transX + galleryBitmap.getWidth() * n * scale > width)// scale<1是preview.width不用乘scale
                    transX = width - galleryBitmap.getWidth() * n * scale;
                else if (transX < 0)
                    transX = 0;
                if (transY + galleryBitmap.getHeight() * n * scale > height)
                    transY = height - galleryBitmap.getHeight() * n * scale;
                else if (transY < 0)
                    transY = 0;
            }
        }

        private int height, width;// 包揽图片的框大小 并非为preview的大�?
        private float centreX, centreY;// 是图片居�?

        public void setBG() {// 不用resize preview
            int w = galleryBitmap.getWidth();
            int h = galleryBitmap.getHeight();
            float nw = w * 1f / previewLayout.getWidth();
            float nh = h * 1f / previewLayout.getHeight();
            if (nw > nh) {
                n = 1 / nw;
                width = previewLayout.getWidth();
                height = (int) (h * n);
            } else {
                n = 1 / nh;
                width = (int) (w * n);
                height = previewLayout.getHeight();
            }
            // 是图片居�?
            centreX = (previewLayout.getWidth() - width) / 2f;
            centreY = (previewLayout.getHeight() - height) / 2f;
            invalidate();
        }

        public void clear() {
            pathStack.clear();
            pathStackBackup.clear();
            hasModified = false;
            originalBitmap.recycle();
            originalBitmap = Bitmap.createBitmap(galleryBitmap.getWidth(),
                    galleryBitmap.getHeight(), Config.ARGB_8888);
            myCanvas = new Canvas(originalBitmap);
            invalidate();
        }

        public void undo() {
            if (pathStack.size() > 0) {
                pathStack.remove(pathStack.size() - 1);
                originalBitmap.recycle();
                originalBitmap = Bitmap.createBitmap(galleryBitmap.getWidth(),
                        galleryBitmap.getHeight(), Config.ARGB_8888);
                myCanvas = new Canvas(originalBitmap);
                drawItself(pathStackBackup);
                invalidate();
            } else if (pathStackBackup.size() > 0) {
                pathStackBackup.remove(pathStackBackup.size() - 1);
                originalBitmap.recycle();
                originalBitmap = Bitmap.createBitmap(galleryBitmap.getWidth(),
                        galleryBitmap.getHeight(), Config.ARGB_8888);
                myCanvas = new Canvas(originalBitmap);
                drawItself(pathStackBackup);
                invalidate();
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            handWriting(canvas);
        }

        private void drawItself(CopyOnWriteArrayList<MyPath> list) {
            for (MyPath p : list) {
                if (p.type == MyPath.TYPE_EARER) {
                    myCanvas.save();
                    myCanvas.clipPath(p.path);
                    myCanvas.drawBitmap(galleryBitmap, 0, 0, null);
                    myCanvas.restore();
                } else if (p.type == MyPath.TYPE_PAINT) {
                    myCanvas.drawPath(p.path, p.paint);
                } else if (p.type == MyPath.TYPE_COPY) {
                    myCanvas.save();
                    myCanvas.clipPath(p.path);
                    if (!galleryBitmap.isRecycled())
                        myCanvas.drawBitmap(galleryBitmap, p.srcRect,
                                p.dstRect, null);
                    myCanvas.restore();
                } else if (p.type == MyPath.TYPE_ARROW) {
                    switch (p.shapeMode) {
                        case MODE_FILL_CIRCLE:
                            myCanvas.drawCircle(p.src.x, p.src.y,
                                    (float) Math.sqrt((p.src.x - p.dst.x) * (p.src.x - p.dst.x) + (p.src.y - p.dst.y) * (p.src.y - p.dst.y)),
                                    p.paint);
                            break;
                        case MODE_HOLL_CIRCLE:
                            myCanvas.drawCircle(p.src.x, p.src.y,
                                    (float) Math.sqrt((p.src.x - p.dst.x) * (p.src.x - p.dst.x) + (p.src.y - p.dst.y) * (p.src.y - p.dst.y)),
                                    p.paint);
                            break;
                        case MODE_FILL_RECT:
                            myCanvas.drawRect(p.src.x, p.src.y, p.dst.x, p.dst.y,
                                    p.paint);
                            break;
                        case MODE_HOLL_RECT:
                            myCanvas.drawRect(p.src.x, p.src.y, p.dst.x, p.dst.y,
                                    p.paint);
                            break;
                        case MODE_LINE:
                            myCanvas.drawLine(p.src.x, p.src.y, p.dst.x, p.dst.y,
                                    p.paint);
                            break;
                        case MODE_ARROW:
                            drawArrow(myCanvas, p.src.x, p.src.y, p.dst.x, p.dst.y,
                                    p.paint, p.arrowSize);
                            break;
                    }
                }
            }
        }

        public void drawArrow(Canvas canvas, float sx, float sy, float ex,
                              float ey, Paint paint, float arrowSize) {
            double H = arrowSize; // 箭头高度
            double L = arrowSize / 2; // 底边的一�?

            double awrad = Math.atan(L / 2 / H); // 箭头角度
            double arraow_len = Math.sqrt(L / 2 * L / 2 + H * H) - 5; // 箭头的长�?
            double[] arrXY_1 = rotateVec(ex - sx, ey - sy, awrad, true,
                    arraow_len);
            double[] arrXY_2 = rotateVec(ex - sx, ey - sy, -awrad, true,
                    arraow_len);
            float x_3 = (float) (ex - arrXY_1[0]); // (x3,y3)是第�?端点
            float y_3 = (float) (ey - arrXY_1[1]);
            float x_4 = (float) (ex - arrXY_2[0]); // (x4,y4)是第二端�?
            float y_4 = (float) (ey - arrXY_2[1]);
            // 画线
            Path linePath = new Path();
            linePath.moveTo(sx, sy);
            linePath.lineTo(x_3, y_3);
            linePath.lineTo(x_4, y_4);
            linePath.close();
            canvas.drawPath(linePath, paint);

            awrad = Math.atan(L / H); // 箭头角度
            arraow_len = Math.sqrt(L * L + H * H); // 箭头的长�?
            arrXY_1 = rotateVec(ex - sx, ey - sy, awrad, true, arraow_len);
            arrXY_2 = rotateVec(ex - sx, ey - sy, -awrad, true, arraow_len);
            x_3 = (float) (ex - arrXY_1[0]); // (x3,y3)是第�?端点
            y_3 = (float) (ey - arrXY_1[1]);
            x_4 = (float) (ex - arrXY_2[0]); // (x4,y4)是第二端�?
            y_4 = (float) (ey - arrXY_2[1]);
            Path triangle = new Path();
            triangle.moveTo(ex, ey);
            triangle.lineTo(x_3, y_3);
            triangle.lineTo(x_4, y_4);
            triangle.close();
            canvas.drawPath(triangle, paint);
        }

        // 计算 向量（px,py�? 旋转ang角度后的新长�?
        public double[] rotateVec(float px, float py, double ang,
                                  boolean isChLen, double newLen) {
            double mathstr[] = new double[2];
            // 矢量旋转函数，参数含义分别是x分量、y分量、旋转角、是否改变长度�?�新长度
            double vx = px * Math.cos(ang) - py * Math.sin(ang);
            double vy = px * Math.sin(ang) + py * Math.cos(ang);
            if (isChLen) {
                double d = Math.sqrt(vx * vx + vy * vy);
                vx = vx / d * newLen;
                vy = vy / d * newLen;
            }
            mathstr[0] = vx;
            mathstr[1] = vy;
            return mathstr;
        }

        public void handWriting(Canvas canvas) {
            if (galleryBitmap.isRecycled())
                return;

            if (!isMovingCopy && !isMovingPic) {
                if (hasModified) {
                    if (isEarering || isPainting || isCopying || isShapping) {
                        if (isPainting && isJustClickOnce) {
                            isJustClickOnce = false;
                            paintPath.quadTo(clickX / (n * scale), clickY
                                    / (n * scale), (clickX + radius / 22 + 1)
                                    / (n * scale), clickY / (n * scale));
                        }
                        drawItself(pathStack);
                    }
                }
            }
            canvas.scale(n * scale, n * scale);
            if (!originalBitmap.isRecycled()) {
                // bg
                canvas.drawBitmap(galleryBitmap, (centreX + transX)
                        / (n * scale), (centreY + transY) / (n * scale), null);
                if (!isJustDrawOriginal) { // 是否只绘制原图
                    canvas.drawBitmap(originalBitmap, (centreX + transX)
                            / (n * scale), (centreY + transY) / (n * scale), null);
                }
            }
            if (isShapping && srcPoint != null) {
                float sx = srcPoint.x + (centreX + transX) / (n * scale);
                float sy = srcPoint.y + (centreY + transY) / (n * scale);
                float dx = clickX
                        / (n * scale) + (centreX + transX)
                        / (n * scale);
                float dy = clickY / (n * scale)
                        + (centreY + transY) / (n * scale);
                switch (lastShapeMode) {
                    case MODE_FILL_CIRCLE:
                        canvas.drawCircle(sx,
                                sy,
                                (float) Math.sqrt((sx - dx) * (sx - dx) + (sy - dy) * (sy - dy)),
                                shapePaint);
                        break;
                    case MODE_HOLL_CIRCLE:
                        canvas.drawCircle(sx,
                                sy,
                                (float) Math.sqrt((sx - dx) * (sx - dx) + (sy - dy) * (sy - dy)),
                                shapePaint);
                        break;
                    case MODE_FILL_RECT:
                        canvas.drawRect(sx, sy, dx, dy, shapePaint);
                        break;
                    case MODE_HOLL_RECT:
                        canvas.drawRect(sx, sy, dx, dy, shapePaint);
                        break;
                    case MODE_LINE:
                        canvas.drawLine(sx, sy, dx, dy, shapePaint);
                        break;
                    case MODE_ARROW:
                        drawArrow(canvas,
                                sx,
                                sy, dx, dy, shapePaint,
                                radius);
                        break;
                }

            }
            if (hasCopy)
                myLocation.draw(canvas, (centreX + transX) / (n * scale),
                        (centreY + transY) / (n * scale));

        }

        private float spacing(MotionEvent event) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return x * x + y * y;
        }

        private float oldDist, newDist, toucheCentreXOnWorld,
                toucheCentreYOnWorld, touchCentreX, touchCentreY;// 双指距离
        boolean isJustClickOnce = false;// 单击屏幕 画笔
        float lastMoveX, lastMoveY, clickMoveX, clickMoveY;
        private int mode;
        boolean isDoubleTouchScaling = false;
        // 是否正在移动copy坐标
        boolean isMovingCopy = false, isCopying = false, isPainting = false,
                isEarering = false, isReLocate = true, isShapping = false;
        float oldScale = 1;
        private Path paintPath;
        private PointF srcPoint;
        private int arrowLineSize;
        private Paint shapePaint;

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (isScaling)
                return true;
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    isCopying = isEarering = isPainting = isShapping = false;
                    if (pathStack.size() > 3) {// 当前栈大�?3，则拷贝到备份栈
                        pathStackBackup.addAll(pathStack);
                        pathStack.clear();
                    }
                    mode = 1;
                    if (isDoubleTouchScaling) {
                        isDoubleTouchScaling = false;
                    }
                    if (hasCopy && !isMovingPic && myLocation.isInIt(event)) {// 移动盖章
                        isMovingCopy = true;
                        isReLocate = true;
                        clickX = event.getX() - transX - centreX;
                        clickY = event.getY() - transY - centreY;// no -100
                        startX = startY = lastX = lastY = 0;
                        oldScale = scale;
                        invalidate();
                    } else {
                        if (hasCopy) {
                            isCopying = true;
                            isReLocate = false;
                        }
                        if (hasPaint) {
                            isPainting = true;
                        }
                        if (hasEarer)
                            isEarering = true;
                        if (hasArrow)
                            isShapping = true;

                        if (hasCopy && !isMovingPic && clickTimes == 1) {// 记下盖章位置
                            startX = event.getX() - transX - centreX;
                            startY = event.getY() - transY - centreY;
                            clickTimes++;
                            float x = (startX - lastX) / (n * scale);
                            float y = (startY - lastY) / (n * scale);
                            dstRect = new RectF(srcRect.left + x, srcRect.top + y,
                                    srcRect.right + x, srcRect.bottom + y);
                            oldScale = scale;

                            hasModified = true;
                            clickX = event.getX() - transX - centreX;
                            clickY = event.getY() - transY - centreY;
                            earer = new Path();
                            pathStack.add(new MyPath(earer, null, MyPath.TYPE_COPY,
                                    srcRect, dstRect));
                            earer.addCircle(clickX / (n * scale), clickY
                                    / (n * scale), radius / 2, Path.Direction.CCW);

                            invalidate();
                        } else if (isMovingPic) {
                            lastMoveX = event.getX();// 不用减trans
                            lastMoveY = event.getY();
                        } else {
                            hasModified = true;
                            clickX = event.getX() - transX - centreX;
                            clickY = event.getY() - transY - centreY;
                            // mPath.reset();
                            if (isPainting) {
                                paintPath = new Path();
                                pathStack.add(new MyPath(paintPath, paint,
                                        MyPath.TYPE_PAINT));
                                paintPath.moveTo(clickX / (n * scale), clickY
                                        / (n * scale));
                            } else if (isEarering) {
                                earer = new Path();
                                pathStack.add(new MyPath(earer, null,
                                        MyPath.TYPE_EARER));
                                earer.addCircle(clickX / (n * scale), clickY
                                                / (n * scale), radius / 2,
                                        Path.Direction.CCW);
                            } else if (isCopying) {
                                earer = new Path();
                                pathStack.add(new MyPath(earer, null,
                                        MyPath.TYPE_COPY, srcRect, dstRect));
                                earer.addCircle(clickX / (n * scale), clickY
                                                / (n * scale), radius / 2,
                                        Path.Direction.CCW);

                            } else if (isShapping) {
                                srcPoint = new PointF(clickX / (n * scale), clickY
                                        / (n * scale));
                                shapePaint = new Paint();
                                shapePaint.setAntiAlias(true);
                                shapePaint.setColor(paint.getColor());
                                switch (lastShapeMode) {
                                    case MODE_HOLL_CIRCLE:
                                    case MODE_HOLL_RECT:
                                    case MODE_LINE:
                                        shapePaint.setStyle(Style.STROKE);
                                        shapePaint.setStrokeWidth(radius);
                                        break;
                                    case MODE_FILL_CIRCLE:
                                    case MODE_FILL_RECT:
                                    case MODE_ARROW:
                                        shapePaint.setStyle(Style.FILL);
                                        shapePaint.setStrokeWidth(arrowLineSize);
                                        break;
                                }


                            }
                            isJustClickOnce = true;
                            invalidate();
                        }
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    mode = 0;
                    if (isMovingCopy) {
                        lastX = event.getX() - transX - centreX;
                        lastY = event.getY() - transY - centreY;
                        clickX = clickY = 0;
                        clickTimes = 1;// 标记已经选择好盖章位�?
                        isMovingCopy = false;
                    } else if (isShapping && !isMovingPic && !isScaling) {
                        PointF dstPoint = new PointF(
                                (event.getX() - transX - centreX) / (n * scale),
                                (event.getY() - transY - centreY) / (n * scale));
                        if ((srcPoint.x - dstPoint.x) * (srcPoint.x - dstPoint.x)
                                + (srcPoint.y - dstPoint.y)
                                * (srcPoint.y - dstPoint.y) > 10) {
                            pathStack.add(new MyPath(MyPath.TYPE_ARROW, shapePaint,
                                    radius, lastShapeMode, srcPoint, dstPoint));
                        }
                        srcPoint = null;
                    }
                    invalidate();
                    return true;
                case MotionEvent.ACTION_POINTER_UP:
                    mode -= 1;
                    return true;
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = spacing(event);// 两点按下时的距离
                    if (isMovingPic) {
                        touchCentreX = event.getX(0) + event.getX(1);// 不用减trans
                        touchCentreY = event.getX(0) + event.getX(1);
                    }
                    toucheCentreXOnWorld = (touchCentreX - 2 * (transX + centreX))
                            / 2 / (n * scale);
                    toucheCentreYOnWorld = (touchCentreY - 2 * (transY + centreY))
                            / 2 / (n * scale);
                    mode += 1;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (mode < 2) {// 单点滑动
                        if (isDoubleTouchScaling) {
                            return true;
                        }
                        if (isMovingCopy) {
                            clickX = event.getX() - transX - centreX;
                            clickY = event.getY() - transY - centreY;
                            isReLocate = true;
                            invalidate();
                        } else if (isMovingPic) {
                            clickMoveX = event.getX();// 不用减trans！！！！
                            clickMoveY = event.getY();
                            float spanX = clickMoveX - lastMoveX;
                            float spanY = clickMoveY - lastMoveY;
                            if (Math.abs(spanX) > 10 || Math.abs(spanY) > 10) {
                                move(spanX, spanY);
                            }
                            lastMoveX = clickMoveX;
                            lastMoveY = clickMoveY;
                        } else {
                            if (hasCopy && clickTimes < 2)
                                return true;
                            hasModified = true;
                            if (isPainting) {
                                paintPath.quadTo(clickX / (n * scale), clickY
                                        / (n * scale), (event.getX() - transX
                                        - centreX + clickX)
                                        / 2 / (n * scale), (event.getY() - transY
                                        - centreY + clickY)
                                        / 2 / (n * scale));
                            }
                            clickX = event.getX() - transX - centreX;
                            clickY = event.getY() - transY - centreY;
                            if (isEarering) {
                                earer.addCircle(clickX / (n * scale), clickY
                                                / (n * scale), radius / 2,
                                        Path.Direction.CCW);
                            } else if (isCopying) {
                                earer.addCircle(clickX / (n * scale), clickY
                                                / (n * scale), radius / 2,
                                        Path.Direction.CCW);
                            }
                            invalidate();
                        }
                    } else {// 多点
                        if (isMovingPic) {
                            newDist = spacing(event);// 两点滑动时的距离
                            if (Math.abs(newDist - oldDist) >= 2000) {
                                isDoubleTouchScaling = true;
                                if (newDist - oldDist > 0) {// 拉大
                                    scale += 0.05f;
                                    if (scale > maxSacle) {
                                        scale = maxSacle;
                                    }
                                    // toucheCentreXOnWorld=(touchCentreX- 2*(transX
                                    // +centreX) ) / 2 / (n * scale);
                                    // toucheCentreYOnWorld=(touchCentreY-
                                    // 2*(transY+centreY)) / 2/ (n * scale);
                                    // 由上式推导出下式，是图片以两点中心缩�?
                                    transX = (touchCentreX - toucheCentreXOnWorld
                                            * (2 * n * scale))
                                            / 2 - centreX;
                                    transY = (touchCentreY - toucheCentreYOnWorld
                                            * (2 * n * scale))
                                            / 2 - centreY;
                                    handWrite.judgePic();
                                    invalidate();
                                } else {// 拉小
                                    scale -= 0.05f;
                                    if (scale < 0.5f) {
                                        scale = 0.5f;
                                    }
                                    transX = (touchCentreX - toucheCentreXOnWorld
                                            * (2 * n * scale))
                                            / 2 - centreX;
                                    transY = (touchCentreY - toucheCentreYOnWorld
                                            * (2 * n * scale))
                                            / 2 - centreY;
                                    handWrite.judgePic();
                                    invalidate();
                                }
                            }
                        }
                        return true;
                    }
            }
            return super.onTouchEvent(event);
        }

        public void save() {
            final String savePath = Util.getFilePath(picPath, "涂鸦");
            FileOutputStream outputStream = null;
            try {
                originalBitmap.recycle();
                try {
                    originalBitmap = galleryBitmap.copy(Config.RGB_565, true);
//				   originalBitmap = galleryBitmap;//这样会报错，因为gallertBitmap时Immutable，不可修改的
                } catch (OutOfMemoryError error) {
                    int screenW = getWindowManager().getDefaultDisplay().getWidth();
                    int screenH = getWindowManager().getDefaultDisplay().getHeight();
                    galleryBitmap.recycle();
                    galleryBitmap = ImageUtils.createBitmapFromPath(picPath, HandWritingActivity.this,
                            (int) (screenW * 1.4f), (int) (screenH * 1.4f));//�?大占用内存改为挽救前的一�?
                    originalBitmap = galleryBitmap.copy(Config.RGB_565, true);
                }
                myCanvas = new Canvas(originalBitmap);
                drawItself(pathStackBackup);
                drawItself(pathStack);
                outputStream = new FileOutputStream(new File(savePath));
                if (!originalBitmap.compress(CompressFormat.JPEG, 95, outputStream)) {
                    outputStream.close();
                    originalBitmap.recycle();
                    galleryBitmap.recycle();
                    setResult(FAIL, null);
                    return;
                }
                outputStream.close();
                // 释放图片
                originalBitmap.recycle();
                galleryBitmap.recycle();
            } catch (Throwable e) {//异常 �? error
                e.printStackTrace();
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e1) {
                    }
                }
                new File(savePath).delete();
                setResult(FAIL, null);
                return;
            }
            Intent data = new Intent();
            data.putExtra(KEY_IMAGE_PATH, savePath);
            setResult(FINISH, data);
            ImageUtils.addImage(getContentResolver(), savePath);
        }

        private class MyLocation {
            float x, y;

            public MyLocation() {
                updateLocation();
            }

            public void updateLocation() {
                x = (lastX * scale / oldScale + clickX - startX * scale
                        / oldScale)
                        / (n * scale);// 和其他不同这里要加transX
                y = (lastY * scale / oldScale + clickY - startY * scale
                        / oldScale)
                        / (n * scale);
            }

            public void draw(Canvas c, float tx, float ty) {
                if (!isMovingPic && !isScaling && (isCopying || isMovingCopy))
                    updateLocation();
                c.drawCircle(x + tx, y + ty, radius / 2, locationPaint);
                if (isReLocate)
                    c.drawCircle(x + tx, y + ty, radius / 4, redPaint);
                else
                    c.drawCircle(x + tx, y + ty, radius / 4, bluePaint);
            }

            public boolean isInIt(MotionEvent e) {
                if (((e.getX() - transX - centreX) / (n * scale) - x)
                        * ((e.getX() - transX - centreX) / (n * scale) - x)
                        + ((e.getY() - transY - centreY) / (n * scale) - y)
                        * ((e.getY() - transY - centreY) / (n * scale) - y) <= radius
                        * radius)
                    return true;
                return false;
            }
        }
    }

    private class TitleOnTouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    isJustDrawOriginal = true;
                    v.setSelected(true);
                    handWrite.invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isJustDrawOriginal = false;
                    v.setSelected(false);
                    handWrite.invalidate();
                    break;
            }
            return true;
        }
    }


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

}

