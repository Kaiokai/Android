package com.android.opencv;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class socketDemobak {
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
    public String response = "";

    // 发送消息到服务器 变量-----------------------------------
    // 输出流对象
    OutputStream outputStream;

    // 接收Bytes
    ByteArrayInputStream byteArrayInputStream;
    // getServerBytes最终值
    public String byteResp = "";
    public String byteCache = "";
    // 处理getServerBytes获取的byte
    GetPngBytes getPngBytes = new GetPngBytes();


    // 创建客户端 服务器对象
    public void setConnect() {
        // 利用线程池开启一个线程 & 执行
        mThreadPool.execute(new Runnable() {
            @SuppressLint("LongLogTag")
            @Override
            public void run() {
                try {
                    // 创建Socket对象 & 指定服务器端口及IP
                    socket = new Socket("172.16.3.145", 6000);
                    // 判断客户端和服务器是否连接成功
                    Log.i("kaio socket isConnected：", String.valueOf(socket.isConnected()));
                } catch (IOException e) {
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
                    if (socket != null) {
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
    // 接收服务器Bytes消息
    public void getServerBytes() {
        // 利用线程池开启一个线程 & 执行
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // 创建输入流对象InputStream
                    is = socket.getInputStream();
                    // 创建一个byte数组用于存放接受的byte
                    byte[] bArr = new byte[50000];
                    // 定义len存放接收的长度
                    int len;
                    // 是否为第一个接受的数据 是否允许没有|start|头部继续执行
                    boolean caPass = false;
                    // 循环读取byte数组 -1代表无可读取内容
                    while ((len = is.read(bArr)) != -1) {
                        // 读取is对象 保存至bArr数组 偏移量为0 is对象长度
                        is.read(bArr, 0, len);
                        // 创建一个String对象用于存放转String类型的byte数据
                        String bArr_c = new String(bArr);
                        // 判断是否存在 |start| 头部; 存在头部或caPass为true(代表不是第一段)
                        if (!(bArr_c.indexOf("|start|") != -1 || caPass)) {
                            // 不存在头部判断缓存区是否为空 为空代表垃圾数据 直接跳出循环
                            if (byteCache == ""){
                                break;
                            }
                            // 缓存区不为空代表为byteCache未完成的数据 放行
                        }
                        // 判断bArr_c是不是一次最终数组 是否存在|end|
                        if (!getPngBytes.isFinalArr(bArr_c)) {
                            // 否--判断是否存在头部
                            if(bArr_c.indexOf("|start|") != -1){
                                //存在--清空之前的byteCache 全部保存至缓存
                                byteCache = bArr_c;
                            }else {
                                //不存在--证明头部在之前的byteCache里 继续保存
                                byteCache += bArr_c;
                            }
                            caPass = true;
                        }else{
                            // 存在--判断是新内容还是缓存的剩下部分
                            if (bArr_c.indexOf("|start|") != -1) {
                                // 有头有尾 取出除头部尾部外的部分保存新数组 剩下的部分保存至缓存
                                byteResp = getPngBytes.getPicStr(bArr_c);
                                byteCache = getPngBytes.getStrOther(bArr_c);
                                // byteResp完成 打印后清空
                                //Log.i("kaio finish",byteResp);
                                // byteResp = "";
                            }else {
                                // 没有头部只有尾部 代表是byteCache未完成的
                                byteCache += bArr_c;
                                byteResp = getPngBytes.getPicStr(byteCache);
                                //Log.i("kaio finish",byteResp);
                                // byteResp = "";
                                byteCache = "";
                            }
                            // 将caPass设置为true 进入下一次循环
                            caPass = false;
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 发送消息给服务器
    public void sendMesToServer(String sendmes) {
        // 利用线程池开启一个线程 & 执行
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // 步骤01: 从Socket获得输出流对象
                    // 该对象作用: 发送数据
                    outputStream = socket.getOutputStream();
                    // 步骤02: 写入需要发送的数据到输出流对象中
                    // 注意: 数据的结尾加上换行符才可以让服务端的readline()停止阻塞
                    //outputStream.write((mEdit.getText().toString()+"\n").getBytes("utf-8"));
                    //outputStream.write((sendmes+"\n").getBytes("utf-8"));
                    byte a1[] = sendmes.getBytes();
                    byte a[] = sendmes.getBytes("ISO-8859-1");
                    if (sendmes.length() > 100) {
                        byte a1_e[] = sendmes.substring(sendmes.length() - 100).getBytes();
                        byte a_e[] = sendmes.substring(sendmes.length() - 100).getBytes("ISO-8859-1");
                        int i = 1;
                        i++;
                    }
                    outputStream.write(a);
                    // 步骤03: 发送数据到服务器
                    outputStream.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 发送消息给服务器
    public void sendMesToServer(byte[] sendmes) {
        // 利用线程池开启一个线程 & 执行
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // 步骤01: 从Socket获得输出流对象
                    // 该对象作用: 发送数据
                    outputStream = socket.getOutputStream();
                    // 步骤02: 写入需要发送的数据到输出流对象中
                    // 注意: 数据的结尾加上换行符才可以让服务端的readline()停止阻塞
                    //outputStream.write((mEdit.getText().toString()+"\n").getBytes("utf-8"));
                    //outputStream.write((sendmes+"\n").getBytes("utf-8"));
                    outputStream.write(sendmes);
                    // 步骤03: 发送数据到服务器
                    outputStream.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 断开客户端 & 服务器连接
    public void disConnect() {
        try {
            // 断开 客户端发送到服务器(发送) 的连接，即关闭输出流对象OutputStream
            if (outputStream != null) {
                outputStream.close();
            }
            // 断开 服务器发送到客户端(接收) 的连接，即关闭输入流读取器对象BufferedReader
            if (br != null) {
                br.close();
            }
            // 最终关闭整个Socket连接
            if (socket.isConnected() && socket != null) {
                socket.close();
            }
            // 判断客户端和服务器是否已经断开连接
            Log.i("kaio socket is isClosed", String.valueOf(socket.isClosed()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 测试用
    public void sendTest() {
        Log.i("Kaio mes:", "001");
    }
}
