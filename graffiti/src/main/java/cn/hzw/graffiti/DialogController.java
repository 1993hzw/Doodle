package cn.hzw.graffiti;

import android.app.Activity;
import android.app.Dialog;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import cn.forward.androids.base.InjectionLayoutInflater;
import cn.forward.androids.utils.StatusBarUtil;

/**
 * Created by huangziwei on 2017/4/21.
 */

public class DialogController {
    public static Dialog showEnterCancelDialog(Activity activity, String title, String msg, final View.OnClickListener enterClick, final View.OnClickListener cancelClick) {
        return showMsgDialog(activity, title, msg, activity.getString(R.string.graffiti_cancel),
                activity.getString(R.string.graffiti_enter), enterClick, cancelClick);
    }

    public static Dialog showEnterDialog(Activity activity, String title, String msg, final View.OnClickListener enterClick) {
        return showMsgDialog(activity, title, msg, null,
                activity.getString(R.string.graffiti_enter), enterClick, null);
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


        View view = InjectionLayoutInflater.from(activity).inflate(R.layout.graffiti_dialog, null,
                InjectionLayoutInflater.getViewOnClickListenerInjector(onClickListener));
        dialog.setContentView(view);


        if (TextUtils.isEmpty(title))

        {
            dialog.findViewById(R.id.dialog_title).setVisibility(View.GONE);
            dialog.findViewById(R.id.dialog_list_title_divider).setVisibility(View.GONE);
        } else

        {
            TextView titleView = (TextView) dialog.findViewById(R.id.dialog_title);
            titleView.setText(title);
        }

        if (TextUtils.isEmpty(msg))

        {
            dialog.findViewById(R.id.dialog_enter_msg).setVisibility(View.GONE);
        } else

        {
            TextView titleView = (TextView) dialog.findViewById(R.id.dialog_enter_msg);
            titleView.setText(Html.fromHtml(msg));
        }

        if (TextUtils.isEmpty(btn01))

        {
            dialog.findViewById(R.id.dialog_enter_btn_01).setVisibility(View.GONE);
        } else

        {
            TextView textView = (TextView) dialog.findViewById(R.id.dialog_enter_btn_01);
            textView.setText(btn01);
        }

        if (TextUtils.isEmpty(btn02))

        {
            dialog.findViewById(R.id.dialog_enter_btn_02).setVisibility(View.GONE);
        } else

        {
            TextView textView = (TextView) dialog.findViewById(R.id.dialog_enter_btn_02);
            textView.setText(btn02);
        }

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
