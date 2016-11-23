package com.zz.henry.hbeijingweather;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    public final static int LOAD_PROGRESS = 0;
    public final static int LOAD_COMPLETE = 1;
    public final static int LOAD_ERROR = 2;
    public final static int LOAD_PIC_COMPLETE = 3;

    Button mButton = null;
    TextView mTextView = null;
    ImageView imageView = null;
    List<TextView> views = new ArrayList<>();

    String AK_BAIDU_API = "BF7bdc283d89f4692247027dd40c186d";

    //接受传过来得消息
    Handler handler = new Handler() {

        public void fillTomorrows(JSONArray weather_data) throws JSONException {
            int i;

            String date;
            String weather;
            String wind;
            String temperature;


            for (i = 0; i < 3; i++) {

                TextView view = views.get(i);
                String body = "";

                JSONObject day = weather_data.getJSONObject(i + 1);
                date = day.getString("date");
                weather = day.getString("weather");
                wind = day.getString("wind");
                temperature = day.getString("temperature");

                body += "日期：" + date + "\n";
                body += "天气情况：" + weather + "\n";
                body += "风速：" + wind + "\n";
                body += "温度：" + temperature + "\n";

                view.setText(body);
            }

        }

        @Override
        public void handleMessage(Message msg) {

            JSONObject json;
            String content;

            switch (msg.what) {

                case LOAD_PROGRESS:
                    mTextView.setText("数据获取中，请稍候......");
                    break;

                case LOAD_COMPLETE:

                    String textMain = "";
                    String picUrl = null;

                    content = (String) msg.obj;
                    try {
                        json = new JSONObject(content);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        mTextView.setText("解析返回Json错误！！");
                        break;
                    }

                    try {

                        String date = json.getString("date");
                        JSONArray results = json.getJSONArray("results");
                        JSONObject data = results.getJSONObject(0);

                        String location = data.getString("currentCity");
                        String pm25 = data.getString("pm25");

                        JSONArray weather_data = data.getJSONArray("weather_data");

                        JSONObject today = weather_data.getJSONObject(0);

                        picUrl = today.getString("dayPictureUrl");
                        String realtime = today.getString("date");
                        String weather = today.getString("weather");
                        String wind = today.getString("wind");
                        String temperature = today.getString("temperature");

                        textMain += "日期：" + date + "\n";
                        textMain += "实时：" + realtime + "\n";
                        textMain += "位置：" + "北京市" + "\n";
                        textMain += "PM2.5：" + pm25 + "\n";
                        textMain += "天气情况：" + weather + "\n";
                        textMain += "风速：" + wind + "\n";
                        textMain += "温度" + temperature + "\n";

                        fillTomorrows(weather_data);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    createPictureThread(picUrl);
                    mTextView.setText(textMain);

                    break;

                case LOAD_PIC_COMPLETE:

                    Bitmap bitmap = (Bitmap) msg.obj;
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
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

        setContentView(R.layout.activity_main);

        mButton = (Button) findViewById(R.id.button1);
        mTextView = (TextView) findViewById(R.id.mainText);
        imageView = (ImageView) findViewById(R.id.imageView);

        views.add((TextView) findViewById(R.id.day1));
        views.add((TextView) findViewById(R.id.day2));
        views.add((TextView) findViewById(R.id.day3));

        createWeatherThread();

        mButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                createWeatherThread();
            }
        });
    }

    public void createWeatherThread() {
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


    public void createPictureThread(final String picUrl) {
        new Thread() {
            public void run() {

                Message msg = new Message();
                Bitmap bitmap = null;

                msg.what = LOAD_PIC_COMPLETE;

                try {
                    bitmap = ImageService.getImageBitmap(picUrl);
                    msg.obj = bitmap;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (bitmap != null) {
                    handler.sendMessage(msg);
                }

            }
        }.start();
    }


    public String getWeatherInfo() {

        String out = null;

        try {

            URL subwayUrl = new URL("http://api.map.baidu.com/telematics/v3/weather?location=beijing&output=json&ak=" + AK_BAIDU_API);

            HttpURLConnection conn = (HttpURLConnection) subwayUrl.openConnection();
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