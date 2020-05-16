package com.android.opencv;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;

//import org.opencv.core.Core;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.opencv.imgcodecs.Imgcodecs.imwrite;

public class MainActivity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "OpenCameraActivity";

    static {
        OpenCVLoader.initDebug();
    }

    // Mat是OpenCV中用来存储图像信息的内存对象
    private Mat mRgba;
    private Mat mFlipRgba;
    private Bitmap a;
    private CameraBridgeViewBase mOpenCvCameraView;

    // Socket
    socketDemo mysocket = new socketDemo();
    Button btnConnect, btnDisConnect, btnSend, btnGetMes;
    boolean isSend = false;

    /**
     * Called when the activity is first created.
     */
    @SuppressLint({"SourceLockedOrientationActivity", "HandlerLeak"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置窗体始终点亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // 设置全屏 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 锁定为横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        // 获取用来显示画面的JavaCameraView
        mOpenCvCameraView = findViewById(R.id.fd_activity_surface_view);
        // 加载视图 不加黑屏
        mOpenCvCameraView.enableView();
        // 设置预览部件 this: com.android.opencv.MainActivity@24c75fdd
        mOpenCvCameraView.setCvCameraViewListener(this);
        // 设置摄像头 前置摄像头: CameraBridgeViewBase.CAMERA_ID_FRONT 后置摄像头: CameraBridgeViewBase.CAMERA_ID_BACK
        mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);

        // Socket
        // 初始化按钮
        btnConnect = findViewById(R.id.connect);
        btnDisConnect = findViewById(R.id.disconnect);
        btnSend = findViewById(R.id.send);
        btnGetMes = findViewById(R.id.getmes);
        // 初始化线程池
        mysocket.mThreadPool = Executors.newCachedThreadPool();
        // 实例化主线程，用于更新接受过来的消息
        mysocket.mMainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        Toast.makeText(getApplicationContext(), String.valueOf(mysocket.response), Toast.LENGTH_SHORT).show();
                        Log.i("kaio msg", String.valueOf(mysocket.response));
                        break;
                }
            }
        };
        // 创建客户端 服务器对象
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mysocket.setConnect();
            }
        });
        // 接收 服务器消息
        btnGetMes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mysocket.getServerMes();
            }
        });
        // 发送消息给服务器
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mysocket.sendMesToServer("123");
                isSend = !isSend;
            }
        });
        // 断开客户端 & 服务器连接
        btnDisConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mysocket.disConnect();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat();
        mFlipRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mRgba.release();
        mFlipRgba.release();
    }

    //NDK对每一帧数据进行操作
//    public static native void nativeRgba(long jrgba);

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        // 单帧从相机回调 gray()返回带有帧的单通道灰度级消光片 rgba()返回带有帧的RGBA Mat
        mRgba = inputFrame.rgba();
        // 初始: 正面向左转
        // 0:  左转镜像翻转
        // -1: 正面向右转
        // 1：右转镜像翻转
        Core.flip(mRgba, mRgba, -1);
        Core.flip(mRgba, mRgba, -1);

        // 得到当前一帧图像的内存地址
        // long addres = mRgba.getNativeObjAddr();

        // NDK对一帧图像进行处理
        // nativeRgba(addr);
        // Log.i("log addr", String.valueOf(addres));
         String pstart = "|start|";
         String pend = "|end|";

        // 调用socket的发送函数 把图片传过去
        if (isSend) {
            // 将mat类型的图片转成bytearray
            //byte[] pData = new byte[(int) mRgba.total()];
            //mRgba.get(0, 0, pData);
            //String pMes = pstart + Arrays.toString(pData) + pend;
            //saveImageToGallery(matToBitmap(mRgba));


            // 生成字符串 加上头尾
            // String pMes = pstart + Arrays.toString(bytes) + pend;
            // 调用发送函数
             mysocket.sendMesToServer(pstart+toByte()+pend);
        }

        return mRgba;
    }
    // bitmap转img转byte
    public String toByte(){
        // 把Map类型的mRgba转成Bitmap类型
        Bitmap bitmapa = matToBitmap(mRgba);
        // 创建一个ByteArrayOutputStream类型的变量来存放转为jpeg的Bitmap
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 转为JPEG 存至baos
        bitmapa.compress(Bitmap.CompressFormat.JPEG,0,baos);
        // 转为byte[]型
        byte[] bytes = baos.toByteArray();
        // 生成字符串 加上头尾
        return Arrays.toString(bytes);
    }

    // mat 转 Bitmap
    public static Bitmap matToBitmap(Mat mat) {
        Bitmap resultBitmap = null;
        if (mat != null) {
            resultBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
            if (resultBitmap != null)
                Utils.matToBitmap(mat, resultBitmap);
        }
        return resultBitmap;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }
}