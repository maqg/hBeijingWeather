package com.zz.henry.httpresource;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends Activity {

    public final static int LOAD_PROGRESS = 0;
    public final static int LOAD_COMPLETE = 1;
    public final static int LOAD_ERROR = 2;

    Button mButton = null;
    TextView mTextView = null;
    Context mContext = null;
    String content = null;

    String AK_BAIDU_API = "BF7bdc283d89f4692247027dd40c186d";

    //接受传过来得消息
    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            JSONObject json;

            switch (msg.what) {
                case LOAD_PROGRESS:
                    content = (String) msg.obj;
                    mTextView.setText("数据获取中，请稍候......");
                    break;
                case LOAD_COMPLETE:
                    String textMain = "";
                    content = (String) msg.obj;
                    try {
                        json = new JSONObject(content);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        mTextView.setText("解析返回Json错误！！");
                        break;
                    }

                    try {
                        textMain += json.getString("date");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    mTextView.setText("Finished: " + textMain);

                    break;
                case LOAD_ERROR:
                    content = (String) msg.obj;
                    mTextView.setText(content);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        setContentView(R.layout.activity_main);

        mButton = (Button) findViewById(R.id.button1);
        mTextView = (TextView) findViewById(R.id.mainText);

        mButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                GetResourceInfo();
            }
        });
    }

    public void GetResourceInfo() {
        new Thread() {
            public void run() {

                Message msg = new Message();
                msg.what = LOAD_PROGRESS;
                msg.obj = "{}";
                handler.sendMessage(msg);

                msg = new Message();

                try {
                    msg.obj = getWeatherInfo();
                    if (msg.obj != null) {
                        msg.what = LOAD_COMPLETE;
                    } else {
                        msg.obj = "获取数据错误，确认连上网啦！！";
                        msg.what = LOAD_ERROR;
                    }
                } catch (Exception e) {
                    msg.what = LOAD_ERROR;
                    msg.obj = "获取数据错误，确认连上网啦！！";
                }
                handler.sendMessage(msg);

            }
        }.start();
    }


    public String getWeatherInfo() {

        String out = null;

        try {

            URL subwayUrl = new URL("http://api.map.baidu.com/telematics/v3/weather?location=beijing&output=json&ak=" + AK_BAIDU_API);

            HttpURLConnection conn = (HttpURLConnection)subwayUrl.openConnection();
            conn.setDoInput(true);
            conn.setConnectTimeout(10000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("accept", "*/*");

            int retCode = conn.getResponseCode();
            if (retCode != 200) {
                out = "bad http reply code " + retCode;
                conn.disconnect();
                return out;
            }

            InputStream stream = conn.getInputStream();

            out = StreamTools.readStream(stream);

            conn.disconnect();
            stream.close();

            return out;

        } catch (Exception e) {
            return out;
        }
    }
}