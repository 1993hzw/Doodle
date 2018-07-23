package cn.hzw.doodle;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import cn.forward.androids.utils.ImageUtils;
import cn.forward.androids.utils.LogUtil;
import cn.forward.androids.utils.StatusBarUtil;
import cn.forward.androids.utils.Util;
import cn.hzw.doodle.core.IDoodle;
import cn.hzw.doodle.core.IDoodleColor;
import cn.hzw.doodle.core.IDoodleSelectableItem;
import cn.hzw.doodle.core.IDoodleTouchDetector;
import cn.hzw.doodle.dialog.ColorPickerDialog;
import cn.hzw.doodle.dialog.DialogController;
import cn.hzw.doodle.imagepicker.ImageSelectorView;
import cn.hzw.doodle.util.DrawUtil;

/**
 * 涂鸦界面，根据DoodleView的接口，提供页面交互
 * （这边代码和ui比较粗糙，主要目的是告诉大家DoodleView的接口具体能实现什么功能，实际需求中的ui和交互需另提别论）
 * Created by huangziwei(154330138@qq.com) on 2016/9/3.
 */
public class DoodleActivity extends Activity {

    public static final String TAG = "Doodle";
    public final static int DEFAULT_COPY_SIZE = 20; // 默认仿制大小
    public final static int DEFAULT_TEXT_SIZE = 17; // 默认文字大小
    public final static int DEFAULT_BITMAP_SIZE = 80; // 默认贴图大小

    public static final int RESULT_ERROR = -111; // 出现错误

    /**
     * 启动涂鸦界面
     *
     * @param activity
     * @param params      涂鸦参数
     * @param requestCode startActivityForResult的请求码
     * @see DoodleParams
     */
    public static void startActivityForResult(Activity activity, DoodleParams params, int requestCode) {
        Intent intent = new Intent(activity, DoodleActivity.class);
        intent.putExtra(DoodleActivity.KEY_PARAMS, params);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 启动涂鸦界面
     *
     * @param activity
     * @param imagePath   　图片路径
     * @param savePath    　保存路径
     * @param isDir       　保存路径是否为目录
     * @param requestCode 　startActivityForResult的请求码
     */
    @Deprecated
    public static void startActivityForResult(Activity activity, String imagePath, String savePath, boolean isDir, int requestCode) {
        DoodleParams params = new DoodleParams();
        params.mImagePath = imagePath;
        params.mSavePath = savePath;
        params.mSavePathIsDir = isDir;
        startActivityForResult(activity, params, requestCode);
    }

    /**
     * {@link DoodleActivity#startActivityForResult(Activity, String, String, boolean, int)}
     */
    @Deprecated
    public static void startActivityForResult(Activity activity, String imagePath, int requestCode) {
        DoodleParams params = new DoodleParams();
        params.mImagePath = imagePath;
        startActivityForResult(activity, params, requestCode);
    }

    public static final String KEY_PARAMS = "key_doodle_params";
    public static final String KEY_IMAGE_PATH = "key_image_path";

    private String mImagePath;

    private FrameLayout mFrameLayout;
    private IDoodle mDoodle;
    private DoodleView mDoodleView;

    private View.OnClickListener mOnClickListener;

    private SeekBar mPaintSizeBar;
    private TextView mPaintSizeView;

    private View mBtnColor;
    private View mBtnColorContainer;

    private View mBtnHidePanel, mSettingsPanel;
    private View mShapeModeContainer;
    private View mSelectedTextEditContainer;
    private View mColorContainer;

    private AlphaAnimation mViewShowAnimation, mViewHideAnimation; // view隐藏和显示时用到的渐变动画

    private DoodleParams mDoodleParams;

    // 触摸屏幕超过一定时间才判断为需要隐藏设置面板
    private Runnable mHideDelayRunnable;
    // 触摸屏幕超过一定时间才判断为需要显示设置面板
    private Runnable mShowDelayRunnable;

    private DoodleOnTouchGestureListener mTouchGestureListener;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_PARAMS, mDoodleParams);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        mDoodleParams = savedInstanceState.getParcelable(KEY_PARAMS);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setStatusBarTranslucent(this, true, false);
        if (mDoodleParams == null) {
            mDoodleParams = getIntent().getExtras().getParcelable(KEY_PARAMS);
        }
        if (mDoodleParams == null) {
            LogUtil.e("TAG", "mDoodleParams is null!");
            this.finish();
            return;
        }

        mImagePath = mDoodleParams.mImagePath;
        if (mImagePath == null) {
            LogUtil.e("TAG", "mImagePath is null!");
            this.finish();
            return;
        }
        LogUtil.d("TAG", mImagePath);
        if (mDoodleParams.mIsFullScreen) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        Bitmap bitmap = ImageUtils.createBitmapFromPath(mImagePath, this);
        if (bitmap == null) {
            LogUtil.e("TAG", "bitmap is null!");
            this.finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.doodle_layout);
        mFrameLayout = (FrameLayout) findViewById(R.id.doodle_container);

        mDoodle = mDoodleView = new DoodleView(this, bitmap, new IDoodleListener() {
            @Override
            public void onSaved(Bitmap bitmap, Runnable callback) { // 保存图片为jpg格式
                File doodleFile = null;
                File file = null;
                String savePath = mDoodleParams.mSavePath;
                boolean isDir = mDoodleParams.mSavePathIsDir;
                if (TextUtils.isEmpty(savePath)) {
                    File dcimFile = new File(Environment.getExternalStorageDirectory(), "DCIM");
                    doodleFile = new File(dcimFile, "Doodle");
                    //　保存的路径
                    file = new File(doodleFile, System.currentTimeMillis() + ".jpg");
                } else {
                    if (isDir) {
                        doodleFile = new File(savePath);
                        //　保存的路径
                        file = new File(doodleFile, System.currentTimeMillis() + ".jpg");
                    } else {
                        file = new File(savePath);
                        doodleFile = file.getParentFile();
                    }
                }
                doodleFile.mkdirs();

                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream);
                    ImageUtils.addImage(getContentResolver(), file.getAbsolutePath());
                    Intent intent = new Intent();
                    intent.putExtra(KEY_IMAGE_PATH, file.getAbsolutePath());
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                    onError(DoodleView.ERROR_SAVE, e.getMessage());
                } finally {
                    Util.closeQuietly(outputStream);
                }
            }

            @Override
            public void onError(int i, String msg) {
                setResult(RESULT_ERROR);
                finish();
            }

            @Override
            public void onReady() {
                float size = mDoodleParams.mPaintUnitSize > 0 ? mDoodleParams.mPaintUnitSize * mDoodle.getSizeUnit() : 0;
                if (size <= 0) {
                    size = mDoodleParams.mPaintPixelSize > 0 ? mDoodleParams.mPaintPixelSize : mDoodle.getSize();
                }
                // 设置初始值
                mDoodle.setSize(size);
                mPaintSizeBar.setProgress((int) (mDoodle.getSize() + 0.5f));
                mPaintSizeBar.setMax(Math.min(mDoodleView.getWidth(), mDoodleView.getHeight()));
                mPaintSizeView.setText("" + mPaintSizeBar.getProgress());
                // 选择画笔
                findViewById(R.id.btn_pen_hand).performClick();
                findViewById(R.id.btn_hand_write).performClick();
                if (mDoodleParams.mZoomerScale <= 0) {
                    findViewById(R.id.btn_zoomer).setVisibility(View.GONE);
                }
            }
        }, null);

        mTouchGestureListener = new DoodleOnTouchGestureListener(mDoodleView, new DoodleOnTouchGestureListener.ISelectionListener() {
            @Override
            public void onSelectedItem(IDoodleSelectableItem selectableItem, boolean selected) {
                if (selected) {
                    DoodleColor color = null;
                    if (selectableItem.getColor() instanceof DoodleColor) {
                        color = (DoodleColor) selectableItem.getColor();
                    }
                    if (color != null) {
                        if (color.getType() == DoodleColor.Type.BITMAP) {
                            mBtnColor.setBackgroundDrawable(new BitmapDrawable(color.getBitmap()));
                        } else {
                            mBtnColor.setBackgroundColor(color.getColor());
                        }
                    }
                    mPaintSizeBar.setProgress((int) (selectableItem.getSize() + 0.5f));
                    mSelectedTextEditContainer.setVisibility(View.VISIBLE);
                } else {
                    DoodleColor color = null;
                    if (mDoodle.getColor() instanceof DoodleColor) {
                        color = (DoodleColor) mDoodle.getColor();
                    }
                    if (color != null) {
                        if (color.getType() == DoodleColor.Type.BITMAP) {
                            mBtnColor.setBackgroundDrawable(new BitmapDrawable(color.getBitmap()));
                        } else {
                            mBtnColor.setBackgroundColor(color.getColor());
                        }
                    }

                    mPaintSizeBar.setProgress((int) (mDoodle.getSize() + 0.5f));
                    mSelectedTextEditContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCreateSelectableItem(float x, float y) {
                if (mDoodle.getPen() == DoodlePen.TEXT) {
                    createDoodleText(null, x, y);
                } else if (mDoodle.getPen() == DoodlePen.BITMAP) {
                    createDoodleBitmap(null, x, y);
                }
            }
        });

        IDoodleTouchDetector detector = new DoodleTouchDetector(getApplicationContext(), mTouchGestureListener);
        mDoodleView.setDefaultTouchDetector(detector);

        mDoodle.setIsDrawableOutside(mDoodleParams.mIsDrawableOutside);
        mFrameLayout.addView(mDoodleView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mOnClickListener = new DoodleOnClickListener();
        mDoodle.setDoodleMinScale(mDoodleParams.mMinScale);
        mDoodle.setDoodleMaxScale(mDoodleParams.mMaxScale);

        initView();
    }

    // 添加文字
    private void createDoodleText(final DoodleText doodleText, final float x, final float y) {
        Activity activity = this;
        if (isFinishing()) {
            return;
        }

        boolean fullScreen = (activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
        Dialog dialog = null;
        if (fullScreen) {
            dialog = new Dialog(activity,
                    android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        } else {
            dialog = new Dialog(activity,
                    android.R.style.Theme_Translucent_NoTitleBar);
        }
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();
        final Dialog finalDialog1 = dialog;
        activity.getWindow().getDecorView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {

            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                finalDialog1.dismiss();
            }
        });

        ViewGroup container = (ViewGroup) View.inflate(getApplicationContext(), R.layout.doodle_create_text, null);
        final Dialog finalDialog = dialog;
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalDialog.dismiss();
            }
        });
        dialog.setContentView(container);

        if (fullScreen) {
            DrawUtil.assistActivity(dialog.getWindow());
        }

        final EditText textView = (EditText) container.findViewById(R.id.doodle_selectable_edit);
        final View cancelBtn = container.findViewById(R.id.doodle_text_cancel_btn);
        final TextView enterBtn = (TextView) container.findViewById(R.id.doodle_text_enter_btn);

        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = (textView.getText() + "").trim();
                if (TextUtils.isEmpty(text)) {
                    enterBtn.setEnabled(false);
                    enterBtn.setTextColor(0xffb3b3b3);
                } else {
                    enterBtn.setEnabled(true);
                    enterBtn.setTextColor(0xff232323);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        textView.setText(doodleText == null ? "" : doodleText.getText());

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelBtn.setSelected(true);
                finalDialog.dismiss();
            }
        });

        enterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterBtn.setSelected(true);
                finalDialog.dismiss();
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (cancelBtn.isSelected()) {
                    mSettingsPanel.removeCallbacks(mHideDelayRunnable);
                    return;
                }
                if (!enterBtn.isSelected()) {
                    return;
                }
                String text = (textView.getText() + "").trim();
                if (TextUtils.isEmpty(text)) {
                    return;
                }
                if (doodleText == null) {
                    IDoodleSelectableItem item = new DoodleText(mDoodle, text, mDoodle.getSize(), mDoodle.getColor().copy(), x, y);
                    mDoodle.addItem(item);
                    mTouchGestureListener.setSelectedItem(item);
                } else {
                    doodleText.setText(text);
                }
                mDoodle.invalidate();
            }
        });

        if (doodleText == null) {
            mSettingsPanel.removeCallbacks(mHideDelayRunnable);
        }
    }

    // 添加贴图
    private void createDoodleBitmap(final DoodleBitmap doodleBitmap, final float x, final float y) {
        Activity activity = this;

        boolean fullScreen = (activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
        Dialog dialog = null;
        if (fullScreen) {
            dialog = new Dialog(activity,
                    android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        } else {
            dialog = new Dialog(activity,
                    android.R.style.Theme_Translucent_NoTitleBar);
        }
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();
        ViewGroup container = (ViewGroup) View.inflate(getApplicationContext(), R.layout.doodle_create_bitmap, null);
        final Dialog finalDialog = dialog;
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalDialog.dismiss();
            }
        });
        dialog.setContentView(container);

        ViewGroup selectorContainer = (ViewGroup) finalDialog.findViewById(R.id.doodle_image_selector_container);
        ImageSelectorView selectorView = new ImageSelectorView(this, false, 1, null, new ImageSelectorView.ImageSelectorListener() {
            @Override
            public void onCancel() {
                finalDialog.dismiss();
            }

            @Override
            public void onEnter(List<String> pathList) {
                finalDialog.dismiss();
                Bitmap bitmap = ImageUtils.createBitmapFromPath(pathList.get(0), mDoodleView.getWidth() / 4, mDoodleView.getHeight() / 4);

                if (doodleBitmap == null) {
                    IDoodleSelectableItem item = new DoodleBitmap(mDoodle, bitmap, mDoodle.getSize(), x, y);
                    mDoodle.addItem(item);
                    mTouchGestureListener.setSelectedItem(item);
                } else {
                    doodleBitmap.setBitmap(bitmap);
                }
                mDoodle.invalidate();
            }
        });
        selectorView.setColumnCount(4);
        selectorContainer.addView(selectorView);
    }


    //++++++++++++++++++以下为一些初始化操作和点击监听+++++++++++++++++++++++++++++++++++++++++

    //
    private void initView() {
        findViewById(R.id.btn_pen_hand).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_pen_copy).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_pen_eraser).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_pen_text).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_pen_bitmap).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_hand_write).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_arrow).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_line).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_holl_circle).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_fill_circle).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_holl_rect).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_fill_rect).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_clear).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_zoomer).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_undo).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_undo).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                findViewById(R.id.btn_clear).performClick();
                return true;
            }
        });
        findViewById(R.id.doodle_selectable_edit).setOnClickListener(mOnClickListener);
        findViewById(R.id.doodle_selectable_remove).setOnClickListener(mOnClickListener);
        findViewById(R.id.doodle_selectable_top).setOnClickListener(mOnClickListener);
        findViewById(R.id.doodle_selectable_bottom).setOnClickListener(mOnClickListener);
        findViewById(R.id.doodle_btn_rotate).setOnClickListener(mOnClickListener);
        mShapeModeContainer = findViewById(R.id.bar_shape_mode);
        mSelectedTextEditContainer = findViewById(R.id.doodle_selectable_edit_container);
        mBtnHidePanel = findViewById(R.id.doodle_btn_hide_panel);
        mBtnHidePanel.setOnClickListener(mOnClickListener);
        findViewById(R.id.doodle_btn_finish).setOnClickListener(mOnClickListener);
        findViewById(R.id.doodle_btn_back).setOnClickListener(mOnClickListener);
        mBtnColor = findViewById(R.id.btn_set_color);
        mBtnColorContainer = findViewById(R.id.btn_set_color_container);
        mBtnColor.setOnClickListener(mOnClickListener);
        mSettingsPanel = findViewById(R.id.doodle_panel);
        mColorContainer = findViewById(R.id.doodle_color_container);

        DoodleColor color = null;
        if (mDoodle.getColor() instanceof DoodleColor) {
            color = (DoodleColor) mDoodle.getColor();
        }
        if (color != null) {
            if (color.getType() == DoodleColor.Type.COLOR) {
                mBtnColor.setBackgroundColor(color.getColor());
            } else if (color.getType() == DoodleColor.Type.BITMAP) {
                mBtnColor.setBackgroundDrawable(new BitmapDrawable(color.getBitmap()));
            }
        }

        mPaintSizeBar = (SeekBar) findViewById(R.id.paint_size);
        mPaintSizeView = (TextView) findViewById(R.id.paint_size_text);
        // 设置画笔的进度条
        mPaintSizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    mPaintSizeBar.setProgress(1);
                    return;
                }
                mPaintSizeView.setText("" + progress);
                mDoodle.setSize(progress);
                if (mTouchGestureListener.getSelectedItem() != null) {
                    mTouchGestureListener.getSelectedItem().setSize(progress);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mDoodleView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 隐藏设置面板
                if (!mBtnHidePanel.isSelected()  // 设置面板没有被隐藏
                        && mDoodleParams.mChangePanelVisibilityDelay > 0) {
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:
                            mSettingsPanel.removeCallbacks(mHideDelayRunnable);
                            mSettingsPanel.removeCallbacks(mShowDelayRunnable);
                            //触摸屏幕超过一定时间才判断为需要隐藏设置面板
                            mSettingsPanel.postDelayed(mHideDelayRunnable, mDoodleParams.mChangePanelVisibilityDelay);
                            break;
                        case MotionEvent.ACTION_CANCEL:
                        case MotionEvent.ACTION_UP:
                            mSettingsPanel.removeCallbacks(mHideDelayRunnable);
                            mSettingsPanel.removeCallbacks(mShowDelayRunnable);
                            //离开屏幕超过一定时间才判断为需要显示设置面板
                            mSettingsPanel.postDelayed(mShowDelayRunnable, mDoodleParams.mChangePanelVisibilityDelay);
                            break;
                    }
                }

                return false;
            }
        });

        // 长按标题栏显示原图
        findViewById(R.id.doodle_txt_title).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mDoodle.setShowOriginal(true);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.setPressed(false);
                        mDoodle.setShowOriginal(false);
                        break;
                }
                return true;
            }
        });

        mViewShowAnimation = new AlphaAnimation(0, 1);
        mViewShowAnimation.setDuration(150);
        mViewHideAnimation = new AlphaAnimation(1, 0);
        mViewHideAnimation.setDuration(150);
        mHideDelayRunnable = new Runnable() {
            public void run() {
                hideView(mSettingsPanel);
            }

        };
        mShowDelayRunnable = new Runnable() {
            public void run() {
                showView(mSettingsPanel);
            }
        };
    }

    private class DoodleOnClickListener implements View.OnClickListener {

        private View mLastPenView, mLastShapeView;
        private boolean mDone = false;
        private ValueAnimator mRotateAnimator;

        @Override
        public void onClick(final View v) {
            mDone = false;
            if (v.getId() == R.id.btn_pen_hand) {
                if (mDoodle.getPen() != DoodlePen.BRUSH) {
                    mBtnColorContainer.setVisibility(View.VISIBLE);
                    mShapeModeContainer.setVisibility(View.VISIBLE);
                    mColorContainer.setVisibility(View.VISIBLE);
                    mTouchGestureListener.setSelectedItem(null);
                    mDoodle.setPen(DoodlePen.BRUSH);
                    Drawable colorBg = mBtnColor.getBackground();
                    if (colorBg instanceof ColorDrawable) {
                        mDoodle.setColor(new DoodleColor(((ColorDrawable) colorBg).getColor()));
                    } else {
                        mDoodle.setColor(new DoodleColor(((BitmapDrawable) colorBg).getBitmap()));
                    }
                    mPaintSizeBar.setProgress((int) (DoodleView.DEFAULT_SIZE * mDoodle.getSizeUnit()));
                }
                mDone = true;
            } else if (v.getId() == R.id.btn_pen_copy) {
                if (mDoodle.getPen() != DoodlePen.COPY) {
                    mBtnColorContainer.setVisibility(View.GONE);
                    mShapeModeContainer.setVisibility(View.VISIBLE);
                    mColorContainer.setVisibility(View.VISIBLE);
                    mTouchGestureListener.setSelectedItem(null);
                    mDoodle.setPen(DoodlePen.COPY);
                    mDoodle.setColor(new DoodleColor(mDoodle.getBitmap()));
                    mPaintSizeBar.setProgress((int) (DEFAULT_COPY_SIZE * mDoodle.getSizeUnit()));
                }
                mDone = true;
            } else if (v.getId() == R.id.btn_pen_eraser) {
                if (mDoodle.getPen() != DoodlePen.ERASER) {
                    mBtnColorContainer.setVisibility(View.GONE);
                    mShapeModeContainer.setVisibility(View.VISIBLE);
                    mColorContainer.setVisibility(View.VISIBLE);
                    mTouchGestureListener.setSelectedItem(null);
                    mDoodle.setPen(DoodlePen.ERASER);
                    mDoodle.setColor(new DoodleColor(mDoodle.getBitmap()));
                    mPaintSizeBar.setProgress((int) (DoodleView.DEFAULT_SIZE * mDoodle.getSizeUnit()));
                }
                mDone = true;
            } else if (v.getId() == R.id.btn_pen_text) {
                if (mDoodle.getPen() != DoodlePen.TEXT) {
                    mBtnColorContainer.setVisibility(View.VISIBLE);
                    mShapeModeContainer.setVisibility(View.GONE);
                    mColorContainer.setVisibility(View.VISIBLE);
                    mTouchGestureListener.setSelectedItem(null);
                    mDoodle.setPen(DoodlePen.TEXT);
                    Drawable colorBg = mBtnColor.getBackground();
                    if (colorBg instanceof ColorDrawable) {
                        mDoodle.setColor(new DoodleColor(((ColorDrawable) colorBg).getColor()));
                    } else {
                        mDoodle.setColor(new DoodleColor(((BitmapDrawable) colorBg).getBitmap()));
                    }
                    mPaintSizeBar.setProgress((int) (DEFAULT_TEXT_SIZE * mDoodle.getSizeUnit() + 0.5f));
                }
                mDone = true;
            } else if (v.getId() == R.id.btn_pen_bitmap) {
                if (mDoodle.getPen() != DoodlePen.BITMAP) {
                    mBtnColorContainer.setVisibility(View.GONE);
                    mShapeModeContainer.setVisibility(View.GONE);
                    mColorContainer.setVisibility(View.VISIBLE);
                    mTouchGestureListener.setSelectedItem(null);
                    mDoodle.setPen(DoodlePen.BITMAP);
                    mPaintSizeBar.setProgress((int) (DEFAULT_BITMAP_SIZE * mDoodle.getSizeUnit() + 0.5f));
                }
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
                if (!(DoodleParams.getDialogInterceptor() != null
                        && DoodleParams.getDialogInterceptor().onShow(DoodleActivity.this, mDoodle, DoodleParams.DialogType.CLEAR_ALL))) {
                    DialogController.showEnterCancelDialog(DoodleActivity.this,
                            getString(R.string.doodle_clear_screen), getString(R.string.doodle_cant_undo_after_clearing),
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mDoodle.clear();
                                    mTouchGestureListener.setSelectedItem(null);
                                }
                            }, null
                    );
                }
                mDone = true;
            } else if (v.getId() == R.id.btn_undo) {
                mTouchGestureListener.setSelectedItem(null);
                mDoodle.undo();
                mDone = true;
            } else if (v.getId() == R.id.btn_zoomer) {
                v.setSelected(!v.isSelected());
                if (v.isSelected()) {
                    mDoodle.setZoomerScale(mDoodleParams.mZoomerScale);
                    Toast.makeText(DoodleActivity.this, "x" + mDoodleParams.mZoomerScale, Toast.LENGTH_SHORT).show();
                } else {
                    mDoodle.setZoomerScale(0);
                }
                mDone = true;
            } else if (v.getId() == R.id.btn_set_color) {
                DoodleColor color = null;
                if (mDoodle.getColor() instanceof DoodleColor) {
                    color = (DoodleColor) mDoodle.getColor();
                }
                if (color != null) {
                    if (!(DoodleParams.getDialogInterceptor() != null
                            && DoodleParams.getDialogInterceptor().onShow(DoodleActivity.this, mDoodle, DoodleParams.DialogType.COLOR_PICKER))) {
                        boolean fullScreen = (getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
                        int themeId;
                        if (fullScreen) {
                            themeId = android.R.style.Theme_Translucent_NoTitleBar_Fullscreen;
                        } else {
                            themeId = android.R.style.Theme_Translucent_NoTitleBar;
                        }
                        new ColorPickerDialog(DoodleActivity.this, color.getColor(), "画笔颜色",
                                new ColorPickerDialog.OnColorChangedListener() {
                                    public void colorChanged(int color) {
                                        mBtnColor.setBackgroundColor(color);
                                        mDoodle.setColor(new DoodleColor(color));
                                        if (mTouchGestureListener.getSelectedItem() != null) {
                                            IDoodleColor c = mTouchGestureListener.getSelectedItem().getColor();
                                            if (c instanceof DoodleColor) {
                                                ((DoodleColor) c).setColor(color);
                                            }
                                        }
                                    }

                                    @Override
                                    public void colorChanged(Drawable color) {
                                        mBtnColor.setBackgroundDrawable(color);
                                        Bitmap bitmap = ImageUtils.getBitmapFromDrawable(color);
                                        mDoodle.setColor(new DoodleColor(bitmap));
                                        if (mTouchGestureListener.getSelectedItem() != null) {
                                            IDoodleColor c = mTouchGestureListener.getSelectedItem().getColor();
                                            if (c instanceof DoodleColor) {
                                                ((DoodleColor) c).setColor(bitmap);
                                            }
                                        }
                                    }
                                }, themeId).show();
                    }
                }
                mDone = true;
            }
            if (mDone) {
                return;
            }

            if (v.getId() == R.id.doodle_btn_hide_panel) {
                mSettingsPanel.removeCallbacks(mHideDelayRunnable);
                mSettingsPanel.removeCallbacks(mShowDelayRunnable);
                v.setSelected(!v.isSelected());
                if (!mBtnHidePanel.isSelected()) {
                    showView(mSettingsPanel);
                } else {
                    hideView(mSettingsPanel);
                }
                mDone = true;
            } else if (v.getId() == R.id.doodle_btn_finish) {
                mDoodle.save();
                mDone = true;
            } else if (v.getId() == R.id.doodle_btn_back) {
                if (mDoodle.getAllItem() == null || mDoodle.getAllItem().size() == 0) {
                    finish();
                    return;
                }
                if (!(DoodleParams.getDialogInterceptor() != null
                        && DoodleParams.getDialogInterceptor().onShow(DoodleActivity.this, mDoodle, DoodleParams.DialogType.SAVE))) {
                    DialogController.showEnterCancelDialog(DoodleActivity.this, getString(R.string.doodle_saving_picture), null, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDoodle.save();
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });
                }
                mDone = true;
            } else if (v.getId() == R.id.doodle_btn_rotate) {
                // 旋转图片
                if (mRotateAnimator == null) {
                    mRotateAnimator = new ValueAnimator();
                    mRotateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int value = (int) animation.getAnimatedValue();
                            mDoodle.setDoodleRotation(value);
                        }
                    });
                    mRotateAnimator.setDuration(250);
                }
                if (mRotateAnimator.isRunning()) {
                    return;
                }
                mRotateAnimator.setIntValues(mDoodle.getDoodleRotation(), mDoodle.getDoodleRotation() + 90);
                mRotateAnimator.start();

                mDone = true;
            }
            if (mDone) {
                return;
            }

            if (v.getId() == R.id.doodle_selectable_edit) {
                if (mTouchGestureListener.getSelectedItem() instanceof DoodleText) {
                    createDoodleText((DoodleText) mTouchGestureListener.getSelectedItem(), -1, -1);
                } else if (mTouchGestureListener.getSelectedItem() instanceof DoodleBitmap) {
                    createDoodleBitmap((DoodleBitmap) mTouchGestureListener.getSelectedItem(), -1, -1);
                }
                mDone = true;
            } else if (v.getId() == R.id.doodle_selectable_remove) {
                mDoodle.removeItem(mTouchGestureListener.getSelectedItem());
                mTouchGestureListener.setSelectedItem(null);
                mDone = true;
            } else if (v.getId() == R.id.doodle_selectable_top) {
                mDoodle.topItem(mTouchGestureListener.getSelectedItem());
                mDone = true;
            } else if (v.getId() == R.id.doodle_selectable_bottom) {
                mDoodle.bottomItem(mTouchGestureListener.getSelectedItem());
                mDone = true;
            }
            if (mDone) {
                return;
            }

            if (v.getId() == R.id.btn_hand_write) {
                mDoodle.setShape(DoodleShape.HAND_WRITE);
            } else if (v.getId() == R.id.btn_arrow) {
                mDoodle.setShape(DoodleShape.ARROW);
            } else if (v.getId() == R.id.btn_line) {
                mDoodle.setShape(DoodleShape.LINE);
            } else if (v.getId() == R.id.btn_holl_circle) {
                mDoodle.setShape(DoodleShape.HOLLOW_CIRCLE);
            } else if (v.getId() == R.id.btn_fill_circle) {
                mDoodle.setShape(DoodleShape.FILL_CIRCLE);
            } else if (v.getId() == R.id.btn_holl_rect) {
                mDoodle.setShape(DoodleShape.HOLLOW_RECT);
            } else if (v.getId() == R.id.btn_fill_rect) {
                mDoodle.setShape(DoodleShape.FILL_RECT);
            }

            if (mLastShapeView != null) {
                mLastShapeView.setSelected(false);
            }
            v.setSelected(true);
            mLastShapeView = v;
        }
    }

    @Override
    public void onBackPressed() { // 返回键监听
        findViewById(R.id.doodle_btn_back).performClick();
    }

    private void showView(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            return;
        }

        view.clearAnimation();
        view.startAnimation(mViewShowAnimation);
        view.setVisibility(View.VISIBLE);
    }

    private void hideView(View view) {
        if (view.getVisibility() != View.VISIBLE) {
            return;
        }
        view.clearAnimation();
        view.startAnimation(mViewHideAnimation);
        view.setVisibility(View.GONE);
    }

}
