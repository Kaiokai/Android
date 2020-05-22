package com.weteoes.model;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;

public class SocketClass {
    private String readByteCache = ""; //缓存
    private String flac_Start = "|start|";
    private String flac_End = "|end|";
    private boolean w = true;
    private boolean socketRun = false; // 是否正在运行socket
    private int socketCache = 100000;

    // 需要重写的函数
    public void SocketRunShell(String data) { }
    public void SendSocketWhile(Socket socket) { }

    public socketRusult connectLongSocket(String ip,int port, String data) {
        try {
            if (socketRun) { return null; }
            socketRun = true;

            final Socket socket = new Socket(); //获得对应socket的输入/输出流
            socket.connect(new InetSocketAddress(ip,port),2000);
            // 读取线程
            Thread readThread = new Thread(){
                public void run(){
                    RecvSocketWhile(socket);
                }
            };
            readThread.start();
            // 发送线程
            Thread sendThread = new Thread(){
                public void run(){
                    SendSocketWhile(socket);
                }
            };
            sendThread.start();

            if (w) { data = flac_Start + data + flac_End; }
            socketSend(socket,data.getBytes(),false);

            socketRusult socketresult = new socketRusult();
            socketresult.socket = socket;
            socketresult.rusult = "";
            return socketresult;
        }
        catch(Exception e) {
            e.printStackTrace();
            socketRun = false;
            return null;
        }
    }
    public void RecvSocketWhile(Socket socket) {
        try {
            InputStream is = socket.getInputStream();
            //BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while(socketRun) {
                try {
                    // 读取缓存
                    String result = readByteCache;
                    byte[] data_one = new byte[socketCache];
                    readByteCache = "";
                    int len = 0;
                    recv:
                    while ((len = is.read(data_one)) != -1) {
                        result += new String(data_one, 0, len,"ISO-8859-1"); // "ISO-8859-1"
                        if (w) {
                            int i_s = 0, i_e = 0;
                            while ((i_s = result.indexOf(flac_Start, i_s)) != -1) {
                                if ((i_e = result.indexOf(flac_End, i_s)) != -1) {
                                    //符合条件
                                    String result_a = result.substring(i_s + flac_Start.length(), i_e);
                                    result = result.substring(i_e + flac_End.length());
                                    readByteCache = result;
                                    final String result_t = result_a;
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SocketRunShell(result_t);
                                        }
                                    }).start();
                                    continue;
                                } else {
                                    if (result == "" || i_e == -1) {
                                        continue recv;
                                    }
                                    result = result.substring(i_e); // 丢弃前面没用数据
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String socketSend_s(java.net.Socket socket, String data,boolean closeing) {
        if (w) { data = flac_Start + data + flac_End; }
        return socketSend(socket, data.getBytes(), closeing);
    }

    public String socketSend_b(java.net.Socket socket, byte[] data,boolean closeing) {
        if (w) {
            data = byteMerger(flac_Start.getBytes(), data);
            data = byteMerger(data, flac_End.getBytes());
        }
        return socketSend(socket, data, closeing);
    }

    // 合并byte
    public byte[] byteMerger(byte[] byte_1, byte[] byte_2){
        byte[] byte_3 = new byte[byte_1.length+byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    public String socketSend(java.net.Socket socket, byte[] data,boolean closeing) {
        try{
            //Weteoes.security.RSA rsa = new Weteoes.security.RSA();
            OutputStream dos = socket.getOutputStream();
            //data = rsa.byPublicAsEncode(data);
            dos.write(data);
            dos.flush(); //发送
            if(closeing){
                dos.close(); // 关闭数据输出流
            }
            return "";
        }
        catch (Exception error){
            error.printStackTrace();
            return null;
        }
    }
    public class socketRusult{
        public java.net.Socket socket;
        public String rusult;
    }
}
