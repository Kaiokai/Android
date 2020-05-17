package com.android.opencv;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class socketDemo {
    // 主线程Handler 用于将从服务器获取的消息显示出来
    public Handler mMainHandler;
    // Socket变量
    private Socket socket;
    // 线程池
    // 为了方便展示,此处直接采用线程池进行线程管理,而没有一个个开线程
    public ExecutorService mThreadPool;

    // 接收服务器消息 变量-------------------------------------
    // 输入流对象
    InputStream is;
    // 输入流读取器对象
    InputStreamReader isr;
    BufferedReader br;
    // 接收服务器发送过来的消息
    String response;

    // 发送消息到服务器 变量-----------------------------------
    // 输出流对象
    OutputStream outputStream;

    // 创建客户端 服务器对象
    public void setConnect() {
        // 利用线程池开启一个线程 & 执行
        mThreadPool.execute(new Runnable() {
            @SuppressLint("LongLogTag")
            @Override
            public void run() {
                try {
                    // 创建Socket对象 & 指定服务器端口及IP
                    socket = new Socket("192.168.2.250",8800);
                    // 判断客户端和服务器是否连接成功
                    Log.i("kaio socket isConnected：", String.valueOf(socket.isConnected()));
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
    }
    // 接收 服务器消息
    public void getServerMes() {
        // 利用线程池开启一个线程 & 执行
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if(socket != null){
                        // 步骤01: 创建输入流对象InputStream
                        is = socket.getInputStream();
                        // 步骤02: 创建输入流读取器对象 并传入输入流对象
                        // 该对象的作用: 获取服务器返回的数据
                        isr = new InputStreamReader(is);
                        br = new BufferedReader(isr);
                        // 步骤03: 通过输入流读取器对象 接收服务器发送过来的数据
                        response = br.readLine();
                        // 步骤04: 通知主线程 将接受的消息显示到主界面
                        Message msg = Message.obtain();
                        msg.what = 0;
                        mMainHandler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    // 发送消息给服务器
    public void sendMesToServer(String sendmes){
        // 利用线程池开启一个线程 & 执行
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    // 步骤01: 从Socket获得输出流对象
                    // 该对象作用: 发送数据
                    outputStream = socket.getOutputStream();
                    // 步骤02: 写入需要发送的数据到输出流对象中
                    // 注意: 数据的结尾加上换行符才可以让服务端的readline()停止阻塞
                    //outputStream.write((mEdit.getText().toString()+"\n").getBytes("utf-8"));
                    outputStream.write((sendmes+"\n").getBytes("utf-8"));
                    // 步骤03: 发送数据到服务器
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    // 断开客户端 & 服务器连接
    public void disConnect() {
        try {
            // 断开 客户端发送到服务器(发送) 的连接，即关闭输出流对象OutputStream
            if(outputStream != null){
                outputStream.close();
            }
            // 断开 服务器发送到客户端(接收) 的连接，即关闭输入流读取器对象BufferedReader
            if(br != null) {
                br.close();
            }
            // 最终关闭整个Socket连接
            if(socket.isConnected() && socket!=null){
                socket.close();
            }
            // 判断客户端和服务器是否已经断开连接
            Log.i("kaio socket is isClosed", String.valueOf(socket.isClosed()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // 测试用
    public void sendTest(){
        Log.i("Kaio mes:","001");
    }
}
