package com.weteoes.model;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

public class InternetClass {
    public String Get(String Type, String Url, String param){ //服务器不识别汉字 URLEncoder.encode
        try{
            HttpURLConnection conn;
            InputStream is = null;
            String resultData = "";
            URL url = new URL(Url); //URL对象
            conn = (HttpURLConnection)url.openConnection(); //使用URL打开一个链接,下面设置这个连接
            conn.setRequestMethod(Type); //使用POST请求
            conn.connect();
            //用输出流向服务器发出参数，要求字符，所以不能直接用getOutputStream
            DataOutputStream dos=new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(param);
            dos.flush();
            dos.close();
            int result = conn.getResponseCode();
            if(result==200) {//返回200表示相应成功
                is = conn.getInputStream();   //获取输入流
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader bufferReader = new BufferedReader(isr);
                String inputLine;
                while ((inputLine = bufferReader.readLine()) != null) {
                    resultData += inputLine + "\n";
                }
                return resultData;
            }
            return null;
        }
        catch (Exception error){
            error.printStackTrace();
            return null;
        }
    }
    public long Download(String Url,String Name,String Dir,boolean cover,String Title, Context _this){ //cover true 为覆盖下载
        try{
            DownloadManager downloadManager = (DownloadManager) _this.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(Url));//设置下载的文件存储的地址，我们这里将下载的apk文件存在/Download目录下面
            //request.setDestinationInExternalPublicDir(Dir, Name);
            File saveFile = new File(Dir, Name);
            request.setDestinationUri(Uri.fromFile(saveFile));
            request.allowScanningByMediaScanner(); //设置现在的文件可以被MediaScanner扫描到。
            request.setTitle(Title); //设置通知的标题
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //设置下载的时候Notification的可见性
            request.setMimeType("application/vnd.android.package-archive"); //设置下载文件类型
            if(cover == true){
                File old = new File(Dir + "/" + Name);
                if(old.exists()){
                    old.delete();
                }
            }
            long id = downloadManager.enqueue(request);
            return id;
        }
        catch (Exception error){
            error.printStackTrace();
            return -1;
        }
    }
    public DownloadStatic DownloadQuery(long id, Context _this){
        try{
            DownloadManager manager = (DownloadManager) _this.getSystemService(Context.DOWNLOAD_SERVICE); // 获取下载管理器服务的实例
            DownloadManager.Query query = new DownloadManager.Query(); // 创建一个查询对象
            query.setFilterById(id);
            Cursor cursor = manager.query(query);
            DownloadStatic now = new DownloadStatic();
            if (cursor.moveToFirst() == false) {
                cursor.close();
                return null;
            }
            now.Id = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
            now.Status =  cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            now.DownloadedSize = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            now.DownloadedAllSize = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            now.SaveDir = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
            if(now.SaveDir == null || now.SaveDir.equals("")){ now.SaveDir = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)); }
            return now;
        }
        catch (Exception error){
            error.printStackTrace();
            return null;
        }
    }
    public static class DownloadStatic{
        public long Id; //id
        public int Status; //状态
        public String SaveDir; //保存位置
        public long DownloadedSize; //已下载
        public long DownloadedAllSize; //总大小
    }
}
