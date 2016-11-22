package com.zz.henry.hbeijingweather;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by henry on 16/11/22.
 */
public class ImageService {

    public static byte[] getImage(String path) throws IOException {
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");   //设置请求方法为GET
        conn.setReadTimeout(5 * 1000);    //设置请求过时时间为5秒
        conn.setConnectTimeout(5 * 1000);
        InputStream inputStream = conn.getInputStream();   //通过输入流获得图片数据
        return StreamTools.readInputStreamBytes(inputStream);     //获得图片的二进制数据

    }

    public static Bitmap getImageBitmap(String path) throws IOException {
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");   //设置请求方法为GET
        conn.setReadTimeout(5 * 1000);    //设置请求过时时间为5秒
        conn.setConnectTimeout(5 * 1000);
        InputStream inputStream = conn.getInputStream();   //通过输入流获得图片数据
        byte[] data = StreamTools.readInputStreamBytes(inputStream);

        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }
}
