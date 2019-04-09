package cn.hzw.doodle.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import cn.forward.androids.utils.Util;
import cn.hzw.doodle.DoodleColor;
import cn.hzw.doodle.R;
import cn.hzw.doodle.core.IDoodle;
import cn.hzw.doodle.util.DrawUtil;

public class ColorPickerDialog extends Dialog {
    private final boolean debug = true;
    private final String TAG = "ColorPicker";

    Context context;
    private OnColorChangedListener mListener;

    /**
     * @param context
     * @param listener 回调
     */
    public ColorPickerDialog(Context context, OnColorChangedListener listener, int themeResId) {
        super(context, themeResId);
        this.context = context;
        mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public void show(IDoodle iDoodle, Drawable drawable, int maxSize) {
        super.show();
        int height = Util.dp2px(context, 220);
        int width = Util.dp2px(context, 180);

        final ViewGroup viewGroup = (ViewGroup) View.inflate(context, R.layout.doodle_color_selector_dialog, null);
        final EditText sizeView = (EditText) viewGroup.findViewById(R.id.doodle_txtview_size);
        final SeekBar seekBar = (SeekBar) viewGroup.findViewById(R.id.doodle_seekbar_size);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    seekBar.setProgress(1);
                    return;
                }
                sizeView.setText("" + progress);
                sizeView.setSelection(sizeView.getText().toString().length());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekBar.setMax(maxSize);
        seekBar.setProgress((int) iDoodle.getSize());

        ViewGroup container = (ViewGroup) viewGroup.findViewById(R.id.doodle_color_selector_container);
        final ColorPickerView colorPickerView = new ColorPickerView(context, Color.BLACK, height, width, null);
        if (drawable instanceof BitmapDrawable) {
            colorPickerView.setDrawable((BitmapDrawable) drawable);
        } else if (drawable instanceof ColorDrawable) {
            colorPickerView.setColor(((ColorDrawable) drawable).getColor());
        }
        container.addView(colorPickerView, 0, new ViewGroup.LayoutParams(height, width));

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView imageView = (ImageView) v;
                colorPickerView.setDrawable((BitmapDrawable) imageView.getDrawable());
            }
        };

        ViewGroup shaderContainer = (ViewGroup) viewGroup.findViewById(R.id.doodle_shader_container);
        for (int i = 0; i < shaderContainer.getChildCount(); i++) {
            shaderContainer.getChildAt(i).setOnClickListener(listener);
        }

        sizeView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int p = Integer.parseInt(s.toString());
                    if (p <= 0) {
                        p = 1;
                    }
                    if (p == seekBar.getProgress()) {
                        return;
                    }
                    seekBar.setProgress(p);
                    sizeView.setText("" + seekBar.getProgress());
                    sizeView.setSelection(sizeView.getText().toString().length());
                } catch (Exception e) {
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        viewGroup.findViewById(R.id.doodle_txtview_reduce).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setProgress(Math.max(1, seekBar.getProgress() - 1));
            }
        });
        viewGroup.findViewById(R.id.doodle_txtview_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setProgress(Math.min(seekBar.getMax(), seekBar.getProgress() + 1));
            }
        });

        viewGroup.findViewById(R.id.dialog_enter_btn_01).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        viewGroup.findViewById(R.id.dialog_enter_btn_02).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (colorPickerView.getDrawable() != null) {
                    mListener.colorChanged(colorPickerView.getDrawable(), seekBar.getProgress());
                } else {
                    mListener.colorChanged(colorPickerView.getColor(), seekBar.getProgress());
                }
                dismiss();
            }
        });

        setContentView(viewGroup, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setCanceledOnTouchOutside(false);

        DrawUtil.assistActivity(getWindow());

    }

    /**
     * 回调接口
     *
     * @author <a href="clarkamx@gmail.com">LynK</a>
     * <p/>
     * Create on 2012-1-6 上午8:21:05
     */
    public interface OnColorChangedListener {
        /**
         * 回调函数
         *
         * @param color 选中的颜色
         */
        void colorChanged(int color, int size);

        void colorChanged(Drawable color, int size);
    }
}  
