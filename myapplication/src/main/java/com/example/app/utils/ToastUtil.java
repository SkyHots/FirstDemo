package com.example.app.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app.R;


public class ToastUtil {

    private static Toast mToast;
    private static View view;
    private static TextView textToast;

    /**
     * 非阻塞试显示Toast,防止出现连续点击Toast时的显示问题
     */
    public static void showToast(Context context, CharSequence text, int duration) {
        if (mToast == null) {
            mToast=new Toast(context);
            view = LayoutInflater.from(context).inflate(R.layout.custom_toast,null);
            textToast = ((TextView) view.findViewById(R.id.text_toast));
            textToast.setText(text);
            mToast.setView(view);
//            mToast.setGravity(Gravity.CENTER,0,100);
            mToast.setDuration(duration);
        } else {
            textToast.setText(text);
            mToast.setView(view);
//            mToast.setGravity(Gravity.CENTER,0,100);
            mToast.setDuration(duration);
        }
        mToast.show();
    }

    public static void showToast(Context context, CharSequence text) {
        showToast(context, text, Toast.LENGTH_SHORT);
    }

}
