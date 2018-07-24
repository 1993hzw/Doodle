package cn.hzw.doodle.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import cn.forward.androids.base.InjectionLayoutInflater;
import cn.forward.androids.utils.ImageUtils;
import cn.forward.androids.utils.StatusBarUtil;
import cn.hzw.doodle.DoodleBitmap;
import cn.hzw.doodle.DoodleText;
import cn.hzw.doodle.R;
import cn.hzw.doodle.core.IDoodleSelectableItem;
import cn.hzw.doodle.imagepicker.ImageSelectorView;
import cn.hzw.doodle.util.DrawUtil;

/**
 * Created by huangziwei on 2017/4/21.
 */

public class DialogController {
    public static Dialog showEnterCancelDialog(Activity activity, String title, String msg, final View.OnClickListener enterClick, final View.OnClickListener cancelClick) {
        return showMsgDialog(activity, title, msg, activity.getString(R.string.doodle_cancel),
                activity.getString(R.string.doodle_enter), enterClick, cancelClick);
    }

    public static Dialog showEnterDialog(Activity activity, String title, String msg, final View.OnClickListener enterClick) {
        return showMsgDialog(activity, title, msg, null,
                activity.getString(R.string.doodle_enter), enterClick, null);
    }

    public static Dialog showMsgDialog(Activity activity, String title, String msg, String btn01, String btn02, final View.OnClickListener enterClick, final View.OnClickListener cancelClick) {

        final Dialog dialog = getDialog(activity);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        StatusBarUtil.setStatusBarTranslucent(dialog.getWindow(), true, false);
        dialog.show();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        final Dialog finalDialog = dialog;
        View.OnClickListener onClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                if (v.getId() == R.id.dialog_bg) {
                    finalDialog.dismiss();
                } else if (v.getId() == R.id.dialog_enter_btn_02) {
                    finalDialog.dismiss();
                    if (enterClick != null) {
                        enterClick.onClick(v);
                    }
                } else if (v.getId() == R.id.dialog_enter_btn_01) {
                    finalDialog.dismiss();
                    if (cancelClick != null) {
                        cancelClick.onClick(v);
                    }
                }
            }
        };

        View view = InjectionLayoutInflater.from(activity).inflate(R.layout.doodle_dialog, null,
                InjectionLayoutInflater.getViewOnClickListenerInjector(onClickListener));
        dialog.setContentView(view);

        if (TextUtils.isEmpty(title)) {
            dialog.findViewById(R.id.dialog_title).setVisibility(View.GONE);
            dialog.findViewById(R.id.dialog_list_title_divider).setVisibility(View.GONE);
        } else {
            TextView titleView = (TextView) dialog.findViewById(R.id.dialog_title);
            titleView.setText(title);
        }

        if (TextUtils.isEmpty(msg)) {
            dialog.findViewById(R.id.dialog_enter_msg).setVisibility(View.GONE);
        } else {
            TextView titleView = (TextView) dialog.findViewById(R.id.dialog_enter_msg);
            titleView.setText(Html.fromHtml(msg));
        }

        if (TextUtils.isEmpty(btn01)) {
            dialog.findViewById(R.id.dialog_enter_btn_01).setVisibility(View.GONE);
        } else {
            TextView textView = (TextView) dialog.findViewById(R.id.dialog_enter_btn_01);
            textView.setText(btn01);
        }

        if (TextUtils.isEmpty(btn02)) {
            dialog.findViewById(R.id.dialog_enter_btn_02).setVisibility(View.GONE);
        } else {
            TextView textView = (TextView) dialog.findViewById(R.id.dialog_enter_btn_02);
            textView.setText(btn02);
        }

        return dialog;
    }

    public static Dialog showInputTextDialog(Activity activity, final String text, final View.OnClickListener enterClick, final View.OnClickListener cancelClick) {
        boolean fullScreen = (activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
        final Dialog dialog = getDialog(activity);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();

        ViewGroup container = (ViewGroup) View.inflate(activity, R.layout.doodle_create_text, null);
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
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
        textView.setText(text == null ? "" : text);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (cancelClick != null) {
                    cancelClick.onClick(cancelBtn);
                }
            }
        });

        enterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (enterClick != null) {
                    enterBtn.setTag((textView.getText() + "").trim());
                    enterClick.onClick(enterBtn);
                }
            }
        });
        return dialog;
    }

    public static Dialog showSelectImageDialog(Activity activity, final ImageSelectorView.ImageSelectorListener listener) {
        final Dialog dialog = getDialog(activity);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();
        ViewGroup container = (ViewGroup) View.inflate(activity, R.layout.doodle_create_bitmap, null);
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setContentView(container);

        ViewGroup selectorContainer = (ViewGroup) dialog.findViewById(R.id.doodle_image_selector_container);
        ImageSelectorView selectorView = new ImageSelectorView(activity, false, 1, null, new ImageSelectorView.ImageSelectorListener() {
            @Override
            public void onCancel() {
                dialog.dismiss();
                if (listener != null) {
                    listener.onCancel();
                }
            }

            @Override
            public void onEnter(List<String> pathList) {
                dialog.dismiss();
                if (listener != null) {
                    listener.onEnter(pathList);
                }

            }
        });
        selectorView.setColumnCount(4);
        selectorContainer.addView(selectorView);
        return dialog;
    }

    private static Dialog getDialog(Activity activity) {
        boolean fullScreen = (activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
        Dialog dialog = null;
        if (fullScreen) {
            dialog = new Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        } else {
            dialog = new Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar);
        }
        return dialog;
    }

}
