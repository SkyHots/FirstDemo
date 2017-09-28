package com.example.myapplication.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * Created by DVO on 2017/7/20 0020.
 */

public class NetUtil {

    public static boolean isNetConnected(Context activity) {
        boolean netConnected = false;
        ConnectivityManager manager = (ConnectivityManager) activity.getSystemService(CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo[] infos = manager.getAllNetworkInfo();
            if (infos != null) {
                for (NetworkInfo ni : infos) {
                    if (ni.isConnected()) {
                        netConnected = true;
                    }
                }
            }
        }
        return netConnected;
    }
}
