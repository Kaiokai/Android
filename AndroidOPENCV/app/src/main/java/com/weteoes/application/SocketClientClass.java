package com.weteoes.application;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.weteoes.model.SocketClass;
import com.weteoes.variableClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class SocketClientClass extends SocketClass {
    public socketRusult socket = null;
    public boolean socketing = false; // 是否正在发送(暂时无用)
    public boolean socketSendWhile = false; // 是否正在循环
    public boolean socketSendNext = true; // 是否发送下一个数据
    public List<byte[]> socketSendDataList = new ArrayList<>();
    public Bitmap response;

    // 连接
    public boolean a_connect() {
        if (socket == null) {
            socket = connectLongSocket("192.168.2.250", 6000, "hello world!!!");
            if (socket == null) {
                return false;
            }
        }
        return true;
    }

    // 发送函数 String
    public String a_send(String data) {
        if (socketing) {
            return null;
        }
        socketing = true;

        if (socket == null) {
            Message a = new Message();
            a.what = variableClass.handler_Toast;
            a.obj = "socket null";
            variableClass.handler.sendMessage(a);
            socketing = false;
            return null;
        }
        String result = socketSend_s(socket.socket, data, false);
        if (result == null) {
            socket = null;
        }
        socketing = false;
        return result;
    }
    // 发送函数 Byte[]
    public String a_send(byte[] data) {
        if (socketing) {
            return null;
        }
        socketing = true;

        if (socket == null) {
            Message a = new Message();
            a.what = variableClass.handler_Toast;
            a.obj = "socket null";
            variableClass.handler.sendMessage(a);
            socketing = false;
            return null;
        }
        String result = socketSend_b(socket.socket, data, false);
        if (result == null) {
            socket = null;
        }
        socketing = false;
        return result;
    }

    static int i = 0;

    // 处理数据
    @Override
    public void SocketRunShell(String data) {
        if (data.equals("00000")) {
            return;
        }
        socketSendNext = true;
        // String 转 Bitmap
        try {
            // string To Bytes
            byte[] a = data.getBytes("ISO-8859-1");
            // Bytes To Bitmap
            response = BitmapFactory.decodeByteArray(a,0,a.length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //SurfaceDrawVariableClass.pointList.add(data);
        // 保存
//        try {
//            byte[] a = data.getBytes("ISO-8859-1");
//            String sdCardDir = Environment.getExternalStorageDirectory() + "/Weteoes/camera/socketPoint";
//            File dirFile = new File(sdCardDir);
//            dirFile.mkdirs();
//            File file = new File(sdCardDir, i + ".png");
//            i++;
//            FileOutputStream out = new FileOutputStream(file);
//            out.write(a,0, a.length);
//            out.flush();
//            out.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
    // 循环发送
    @Override
    public void SendSocketWhile(Socket socket) {
        try {
            if (socketSendWhile) {
                return;
            }
            socketSendWhile = true;
            while (true) {
                if (!socketSendNext) {
                    Thread.sleep(1500);
                }
                int s = socketSendDataList.size();
                if (s == 0) {
                    continue;
                }
                final byte[] data = socketSendDataList.get(s - 1);
                for (int i = s - 1; i >= 0; i--) {
                    socketSendDataList.remove(i);
                }

                socketSendNext = false;
                a_send(data);

//                final int aai = i;
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            byte[] a = data;
//                            String sdCardDir = Environment.getExternalStorageDirectory() + "/Weteoes/camera/socketSendPoint";
//                            File dirFile = new File(sdCardDir);
//                            dirFile.mkdirs();
//                            File file = new File(sdCardDir, aai + ".png");
//                            i++;
//                            FileOutputStream out = new FileOutputStream(file);
//                            out.write(a, 0, a.length);
//                            out.flush();
//                            out.close();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}