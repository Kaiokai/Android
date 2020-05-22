package com.weteoes.model;

import android.content.Context;
import android.widget.Toast;

public class MessageClass {
    static Toast Toast_Temp;
    public static void Toast(Context context, String text){
        if(Toast_Temp != null){
            Toast_Temp.cancel();
        }
        Toast_Temp = Toast.makeText(context, text,Toast.LENGTH_SHORT);
        Toast_Temp.show();
    }
}
