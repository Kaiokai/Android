package com.weteoes.model;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class PermissionsClass {
    public static int obtainNum; //一共获取到的权限数量
    public static String[][] permissionsList = new String[][]{
            { Manifest.permission.WRITE_EXTERNAL_STORAGE, "需要内存读取权限" } //读写权限
    };

    public boolean getPermissions(Activity thisActivity,String permissions){ //检查权限
        if(android.os.Build.VERSION.SDK_INT >= 23){
            int a = ContextCompat.checkSelfPermission(thisActivity, permissions);
            if(a == PackageManager.PERMISSION_GRANTED){ //已授权
                return true;
            }
        }else{ //sdk过低直接进入
            return true;
        }
        return false;
    }
    public void obtainPermissions(Activity thisActivity,String permissions,String message){ //请求权限
        ActivityCompat.requestPermissions(thisActivity, new String[]{permissions},1);
    }
    public boolean loading(Activity thisActivity){ //一键初始化
        int i = 0; //有权限的数量
        for (String[] only : permissionsList) {
            getPermissions_:
            if(!getPermissions(thisActivity,only[0])){ //没有权限
                obtainPermissions(thisActivity,only[0],only[1]);
                break getPermissions_;
            }else{
                i++;
                if(i == permissionsList.length){ return true; }
            }
        }
        return false;
    }
    public boolean loading_get(Activity thisActivity){ //一键判断是否有权限
        for (String[] only : permissionsList) {
            if(!getPermissions(thisActivity,only[0])){
                return false;
            }
        }
        return true;
    }
}
