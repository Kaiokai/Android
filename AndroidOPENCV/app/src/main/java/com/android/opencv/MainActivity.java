package com.android.opencv;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;

//import org.opencv.core.Core;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import com.weteoes.variableClass;
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

    // 时间
    public String nowTime = "0";
    public String getTimes = "0";

    // 存放byteResp 判断是否为重复
    public String byteResp_do = "";

    // ImageView(显示Server端的PNG)
    public ImageView iv;

    // SurfaceView
    public MySurfaceView mysv;
    public SurfaceHolder mholder;
    public boolean isRunning;
    public Bitmap bmpStatus;
    public Canvas canvas;
    Bitmap bitmap;

    /**
     * Called when the activity is first created.
     */
    @SuppressLint({"SourceLockedOrientationActivity", "HandlerLeak"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initw();
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
        iv = findViewById(R.id.imageView);
        mysv = findViewById(R.id.mysurfaceview);
        mholder = mysv.getHolder();
        bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.girl);

        // SurfaceView
        mholder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // mysv.setZOrderOnTop(true);
                mholder.setFormat(PixelFormat.TRANSLUCENT);
                mysv.setZOrderOnTop(true);
                isRunning = true;
                SvRun(bitmap);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                isRunning = false;
            }
        });

        // 初始化线程池
        mysocket.mThreadPool = Executors.newCachedThreadPool();
        // 实例化主线程，用于更新接受过来的消息
        mysocket.mMainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        Toast.makeText(getApplicationContext(), String.valueOf(mysocket.response), Toast.LENGTH_SHORT).show();
                        Log.i("kaio msg create", String.valueOf(mysocket.response));
                        break;
                }
            }
        };
        // 创建客户端 服务器对象
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mysocket.setConnect();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        variableClass.socketClientClass.a_connect();
                    }
                }).start();
            }
        });
        // 接收 服务器消息
        btnGetMes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mysocket.getServerMes();
//                mysocket.getServerBytes();
            }
        });
        // 发送消息给服务器
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mysocket.sendMesToServer("|start|hello world!!!|end|");
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

    // SurfaceView
    public void SvRun(Bitmap bmp) {
        while (isRunning) {
            if (bmpStatus != bmp) {
//                try {
//                    Thread.sleep(50);
//                }catch (Exception e) {
//                    e.printStackTrace();
//                }
                Bitmap a = Bitmap.createScaledBitmap(bmp,400,400,true);
                doDraw(a);
            }
            isRunning = false;
        }
    }
    public void doDraw(Bitmap bmp) {
        canvas = mholder.lockCanvas();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        // canvas.drawColor(Color.TRANSPARENT);
        canvas.drawBitmap(bmp,0,0,null);
        mholder.unlockCanvasAndPost(canvas);
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

    @RequiresApi(api = Build.VERSION_CODES.N)
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
        //String pstart = "|start|";
        //String pend = "|end|";

        // 调用socket的发送函数 把图片传过去
        //if (isSend) {
            // 将mat类型的图片转成bytearray
            //byte[] pData = new byte[(int) mRgba.total()];
            //mRgba.get(0, 0, pData);
            //String pMes = pstart + Arrays.toString(pData) + pend;
            //saveImageToGallery(matToBitmap(mRgba));


            // 生成字符串 加上头尾
            // String pMes = pstart + Arrays.toString(bytes) + pend;
            // 调用发送函数
            // mysocket.sendMesToServer(pstart+toByte()+pend);



        // 如果当前获取的时间和上一秒保存的时间不同 则执行 每次间隔为1s
//            if(!(getTime()).equals(nowTime)){
//                // Log.i("kaio mes",getTime()+" "+nowTime);
//                // mysocket.sendMesToServer(getTime()+ " " +nowTime+ " " +getTime().equals(nowTime));
//
//                if(isSend) {
//                    // 发送图像
//                    byte[] data = toByte();
//                    // 往socketSendDataList内存放Bytes图像
//                    variableClass.socketClientClass.socketSendDataList.add(data);
//                }
//
//                //data = byteMerger(data, pstart.getBytes());
//                //data = byteMerger(data, toByte());
//                //data = byteMerger(data, pend.getBytes());
//                //mysocket.sendMesToServer(data);
//                // 保存时间 用以判断间隔
//                nowTime = getTime();
//            }
            // 延时发送画面
            try {
                Thread.sleep(500);
                if(isSend) {
                    // 发送图像
                    byte[] data = toByte();
                    // 往socketSendDataList内存放Bytes图像
                    variableClass.socketClientClass.socketSendDataList.add(data);
                }
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 显示图片--只能在创建View的线程中修改View
            if (variableClass.socketClientClass.response != null) {
                Bitmap bitmap = variableClass.socketClientClass.response;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(bitmap!=null) {
                            // iv.setImageBitmap(bitmap);
                            isRunning = true;
                            SvRun(bitmap);
                        }
                    }
                });
            }

//            // 获取服务器Png的Bytes
//            mysocket.getServerBytes();
//            //判断是否为空 是否与上一次接收内容重复 重复则不操作
//            if (mysocket.byteResp!="" && mysocket.byteResp != byteResp_do) {
//                // 判断drawImage返回的Bitmap是否为空
//                if (drawImage(mysocket.byteResp)!=null) {
//                    // 将获取的bitmap对象 转为Mat
//                    // Bitmap bitmap = drawImage(mysocket.byteResp);
//                    // Utils.bitmapToMat(bitmap,mRgba);
//                    //Imgproc.circle(mRgba, new Point(mRgba.cols()/2, mRgba.rows()/2), 50,
//                    //       new Scalar(255, 0, 0, 255), 2, 8, 0);
//
//                    // 保存Bitmap变量
//                    Bitmap bitmap = drawImage(mysocket.byteResp);
//                    // 直接用Bitmap显示
//                    // iv.setImageBitmap(bitmap);
//                    // 只能在创建View的线程中修改View
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if(bitmap!=null) {
//                                iv.setImageBitmap(bitmap);
//                                isRunning = true;
//                                SvRun(bitmap);
//                            }
//                        }
//                    });
//                }
//                // drawImage(mysocket.byteResp);
//                Log.i("zdk ms",mysocket.byteResp);
//                byteResp_do = mysocket.byteResp;
//            }
        //}
        return mRgba;
    }

    // 将获取的PNG String转Byte 再由byteToBimap转为Bitmap返回
    public Bitmap drawImage(String str) {
        //Bitmap bitmap =  Bitmap.createBitmap(480, 640, Bitmap.Config.ARGB_8888);
        Bitmap bitmap = null;
        try {
            // 转为Bytes
            byte[] byteArr = str.getBytes("ISO-8859-1");
            if (byteArr.length != 0) {
                // 转为Bitmap
                bitmap = byteToBimap(byteArr);
            }
            if (bitmap != null) {
                return bitmap;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    // 接收drawImage的byte[] 转为Bitmap返回
    public Bitmap byteToBimap(byte[] byteArr) {
        byte[] bs = byteArr;
        YuvImage yuvimage=new YuvImage(bs, ImageFormat.NV21, 20,20, null);//20、20分别是图的宽度与高度
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0,20, 20), 80, baos);//80--JPG图片的质量[0-100],100最高
        byte[] jdata = baos.toByteArray();
        Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);
        return bmp;
    }

    // 获取当前时间
    public String getTime() {
        long timeStamp = System.currentTimeMillis();
        String time = stampToDate(timeStamp);
        return time;
    }
    // 时间戳转为时间
    public String stampToDate(long timeMillis){
        // 设置格式 原始为"SimpleDateFormat("yyyy-MM-dd HH:mm:ss")"
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ss");
        Date date = new Date(timeMillis);
        return simpleDateFormat.format(date);
    }

    // bitmap转img转byte
    public byte[] toByte(){
        // 把Map类型的mRgba转成Bitmap类型
        Bitmap bitmapa = matToBitmap(mRgba);
        // 创建一个ByteArrayOutputStream类型的变量来存放转为jpeg的Bitmap
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 转为JPEG 存至baos
        bitmapa.compress(Bitmap.CompressFormat.JPEG,10, baos);
        // 转为byte[]型
        byte[] bytes = baos.toByteArray();
        // 生成字符串 加上头尾
        //saveByte(bytes);
        //String result = new String(bytes);
        return bytes;
        //return Arrays.toString(bytes);
    }

    // 合并byte
    public byte[] byteMerger(byte[] byte_1, byte[] byte_2){
        byte[] byte_3 = new byte[byte_1.length+byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    // 保存
    public void saveByte(byte[] a) {
        try {
            String sdCardDir = Environment.getExternalStorageDirectory() + "/Weteoes/opencv/";
            File dirFile = new File(sdCardDir);
            if (!dirFile.exists()) {              //如果不存在，那就建立这个文件夹
                dirFile.mkdirs();
            }
            File file = new File(sdCardDir, "test.jpg");
            FileOutputStream out = new FileOutputStream(file);
            out.write(a,0, a.length);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public void initw() {
        variableClass.activity = this;
        variableClass.mainActivity = this;
    }
}