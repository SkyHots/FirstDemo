package com.example.myapplication.utils;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;

/**
 * Created by DVO on 2017/7/19 0014.
 *
 */

public class AlertDialogUtil {

    private static AlertDialog dialog;

    public static void showAlertDialog(Activity activity, int iconRes, String title, String msg,
                                       String positiveText, String negativeText, boolean
                                               cancelTouchOut, final DialogClickListener
                                               dialogClickListener) {
        View view = LayoutInflater.from(activity).inflate(R.layout.custom_dialog_layout, null,
                false);
        ImageView mIcon = (ImageView) view.findViewById(R.id.icon);
        TextView mTitle = (TextView) view.findViewById(R.id.title);
        TextView mMessage = (TextView) view.findViewById(R.id.message);
        Button positiveButton = (Button) view.findViewById(R.id.positiveButton);
        Button negativeButton = (Button) view.findViewById(R.id.negativeButton);
        mIcon.setImageResource(iconRes);
        mTitle.setText(title);
        mMessage.setText(msg);
        positiveButton.setText(positiveText);
        negativeButton.setText(negativeText);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogClickListener.clickPositive();
                dialog.dismiss();
            }
        });
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogClickListener.clickNegative();
                dialog.dismiss();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);

        builder.setCancelable(true);   //设置按钮是否可以按返回键取消,false则不可以取消
        //创建对话框
        dialog = builder.create();
        //        Window window = dialog.getWindow();
        //        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//设置圆角有白色背景加上这句
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(cancelTouchOut);   //设置弹出框失去焦点是否隐藏,即点击屏蔽其它地方是否隐藏
        dialog.show();
    }

    public interface DialogClickListener {
        void clickPositive();
        void clickNegative();
    }
}
