package com.weteoes.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.RandomAccessFile;

public class FilesClass {
    public String read(String file){ //读取
        try{
            File z = new File(file);
            if(!z.exists()){ return null; }
            FileReader a = new FileReader(file);
            BufferedReader b = new BufferedReader(a);
            String all = "",c;
            while((c = b.readLine()) != null){
                all += c;
            }
            return all;
        }
        catch (Exception error){
            error.printStackTrace();
            return "读取文件 " + file +" 出错";
        }
    }
    public boolean write(String file,String data){ //写出
        try{
            File a = new File(file);
            if(!a.exists()){ //文件不存在
                createDir(file); //创建文件夹
                a.createNewFile();
                FileWriter b = new FileWriter(a);
                b.write(data);
                b.close();
                return true;
            }else{ //存在
                RandomAccessFile b = new RandomAccessFile(a,"rw");
                b.seek(b.length());
                b.writeChars(data);
                b.close();
                return true;
            }
        }
        catch (Exception error){
            error.printStackTrace();
            return false;
        }
    }
    public boolean createDir(String file){ //递归创建文件夹
        try{
            file = file.substring(0,file.lastIndexOf("/"));
            File a = new File(file);
            if(a.exists()) { return true; }
            return a.mkdirs();
        }
        catch (Exception error){
            error.printStackTrace();
            return false;
        }
    }
}
