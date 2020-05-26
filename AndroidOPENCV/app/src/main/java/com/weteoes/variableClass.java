package com.weteoes;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;

import com.android.opencv.MainActivity;
import com.weteoes.application.SocketClientClass;
import com.weteoes.model.MessageClass;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class variableClass {
    public static SocketClientClass socketClientClass = new SocketClientClass();
    public static MessageClass messageClass = new MessageClass();
    public static List<byte[]> socketPngByte = new ArrayList<>();

    public static Activity activity; // 窗口
    public static com.android.opencv.MainActivity mainActivity; // 窗口class
    public static DisplayMetrics activitySize; // 窗口Size


    public final static int handler_Toast = 1;
    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case handler_Toast:
                    variableClass.messageClass.Toast(variableClass.activity, (String)msg.obj);
                    break;
            }
            super.handleMessage(msg);
        }
    };
}
