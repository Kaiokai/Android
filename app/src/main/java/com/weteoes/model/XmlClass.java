package com.weteoes.model;


import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class XmlClass {
    static File file;
    static Document xml;
    public Document GetNewDocument(){
        DocumentBuilder b = getDocumentBuilder();
        Document c = b.newDocument();
        this.xml = c;
        return c;
    }
    /* 文件读取 */
    public Document read(String file,boolean create) {
        try{
            DocumentBuilder b = getDocumentBuilder();
            Document c;
            File files = new File(file);
            if(!files.exists()){ if(create) c = GetNewDocument(); else return null; } //不存在创建或退出
            else{ c = b.parse(files); } //文件存在读取
            this.file = files; //保存到静态变量
            this.xml = c; //保存到静态变量
            return  c;
        }
        catch (Exception error){
            error.printStackTrace();
            return null;
        }
    }
    /* 字符串读取 */
    public Document read(String data) {
        try{
            DocumentBuilder b = getDocumentBuilder();
            StringReader sr = new StringReader(data);
            InputSource is = new InputSource(sr);
            Document c = b.parse(is);
            return  c;
        }
        catch (Exception error){
            error.printStackTrace();
            return null;
        }
    }
    private DocumentBuilder getDocumentBuilder(){
        try{
            DocumentBuilderFactory a = DocumentBuilderFactory.newInstance();
            return a.newDocumentBuilder();
        }
        catch (Exception error){
            error.printStackTrace();
            return null;
        }
    }
    public boolean save(){
        try{
            if(this.xml == null){ return false; }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer(); //获取到Transformer
            transformer.setOutputProperty("encoding","UTF-8"); //设置输出格式
            if(!new FilesClass().createDir(file.getPath())){ return false; } //创建文件夹
            OutputStream os = new FileOutputStream(file); //设置输出流
            transformer.transform(new DOMSource(xml),new StreamResult(os));//将文件写出
            return true;
        }
        catch (Exception error){
            error.printStackTrace();
            return false;
        }
    }
}

