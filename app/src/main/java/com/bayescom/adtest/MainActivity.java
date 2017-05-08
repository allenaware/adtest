package com.bayescom.adtest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int PIC_RECEIVED = 2;
    private static final int PIC_REQUEST_ERROR = 3;
    private static final int PIC_REQUEST_EXCEPTION = 4;
    private static final int AD_RECEIVED = 5;
    private static final int AD_REQUEST_ERROR = 6;
    private static final int SHOW_REPORTED = 7;
    private static final int CLICK_REPORTED = 8;
    private static final int SHOW_REPORT_ERROR = 9;
    private static final int CLICK_REPORT_ERROR = 10;
    private ImageView adImageView;
    private static final String POST_URL = "http://shuttle.bayescom.com/shuttle";
    private  ArrayList showReportArrayList;
    private ArrayList clickReportArrayList;
    private String link = "";
    private String imageURL ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("广告位展示");
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        adImageView = (ImageView) findViewById(R.id.adImageView);


    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

    //1.在主线程里面声明消息处理器 handler
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PIC_RECEIVED:
                    Bitmap bitmap2 = (Bitmap) msg.obj;
                    adImageView.setImageBitmap(bitmap2);
                    System.out.println("图片正常显示");
                    //上报展示
                    reportAdShow();
                    break;
                case AD_RECEIVED:
                    String rspJson =(String) msg.obj;

                    showReportArrayList = new ArrayList();
                    clickReportArrayList = new ArrayList();

                    try {
                        System.out.println(rspJson);
                        JSONObject jsonObject = new JSONObject(rspJson);
                        Integer code = jsonObject.getInt("code");
                        if(code!=200)
                        {

                            Toast.makeText(MainActivity.this,rspJson, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        JSONArray impArray= jsonObject.getJSONArray("imp");
                        for(int i=0;i<impArray.length();i++)
                        {
                           JSONObject impObj= impArray.getJSONObject(i);
                           JSONArray image = impObj.getJSONArray("image") ;
                           JSONArray imptk = impObj.getJSONArray("imptk");
                           JSONArray clicktk = impObj.getJSONArray("clicktk");
                           link = impObj.getString("link");
                           for(int j=0;j<image.length();j++  )
                           {
                               JSONObject imageObj = image.getJSONObject(j);
                               imageURL =imageObj.getString("iurl");
                           }
                           for(int m =0;m<imptk.length();m++)
                           {
                               showReportArrayList.add(imptk.getString(m));

                           }
                           for(int n =0;n<clicktk.length();n++)
                           {
                               clickReportArrayList.add(clicktk.getString(0));

                           }

                        }
                        System.out.println(imageURL);
                        new Thread(){
                            public void run() {
                                getImage(imageURL);
                            }

                        }.start();

                    } catch (JSONException e) {

                        Toast.makeText(MainActivity.this,"返回格式广告Json格式解析错误", Toast.LENGTH_SHORT).show(); }
                    break;
                case AD_REQUEST_ERROR:
                    Toast.makeText(MainActivity.this,(String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case PIC_REQUEST_ERROR:
                    Toast.makeText(MainActivity.this, "广告图片请求失败", Toast.LENGTH_SHORT).show();
                    break;
                case SHOW_REPORTED:
                    Toast.makeText(MainActivity.this, "展示上报成功", Toast.LENGTH_SHORT).show();
                    break;
                case SHOW_REPORT_ERROR:
                    Toast.makeText(MainActivity.this, "展示上报失败", Toast.LENGTH_SHORT).show();
                    break;
                case CLICK_REPORTED:
                    Toast.makeText(MainActivity.this, "点击上报成功", Toast.LENGTH_SHORT).show();
                    break;
                case CLICK_REPORT_ERROR:
                    Toast.makeText(MainActivity.this, "点击上报失败", Toast.LENGTH_SHORT).show();
                    break;
                case PIC_REQUEST_EXCEPTION:
                    Toast.makeText(MainActivity.this, "广告图片请求发生异常", Toast.LENGTH_SHORT).show();
                    break;
            }

            super.handleMessage(msg);
        }
    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void loadAd(View view) throws JSONException {
        DeviceInfoUtil devu = new DeviceInfoUtil(this);

        final JSONObject jsonObject = devu.getDeviceInfo();
        System.out.println(jsonObject.toString());


        new Thread() {
            public void run() {
                requestAd(jsonObject);
            }

        }.start();

    }

    public void requestAd(JSONObject jsonObject) {
        try {

            URL url = new URL(POST_URL);
            // 建立http连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 设置允许输出
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 设置不用缓存
            conn.setUseCaches(false);
            // 设置传递方式
            conn.setRequestMethod("POST");
            // 设置文件字符集:
            conn.setRequestProperty("Charset", "UTF-8");
            //转换为字节数组
            byte[] data = (jsonObject.toString()).getBytes();
            // 设置文件长度
            conn.setRequestProperty("Content-Length", String.valueOf(data.length));
            // 设置文件类型:
            conn.setRequestProperty("contentType", "application/json");
            // 开始连接请求
            conn.connect();
            OutputStream out = conn.getOutputStream();
            // 写入请求的字符串
            out.write((jsonObject.toString()).getBytes());
            out.flush();
            out.close();

            System.out.println(conn.getResponseCode());
            // 请求返回的状态
            if (conn.getResponseCode() == 200) {
                System.out.println("有广告返回");
                // 请求返回的数据
                BufferedReader in = null;
                String result = "";
                in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    result +=  line;
                }
                System.out.println(result);
                Message msg = new Message();
                msg.what = AD_RECEIVED;
                msg.obj= result;
                handler.sendMessage(msg);
            } else {
                System.out.println("无广告返回");
                BufferedReader in = null;
                String result = "";
                in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    result +=  line;
                }
                System.out.println(result);
                Message msg = new Message();
                msg.what = AD_REQUEST_ERROR;
                msg.obj= result;
                handler.sendMessage(msg);
            }

        } catch (Exception e) {
            Message msg = new Message();
            msg.what = AD_REQUEST_ERROR;
            msg.obj="ad request exception";
            handler.sendMessage(msg);
        }

    }

    public void adDidClick(View view) {
        System.out.println("report ad clicked");
        Intent intent = new Intent();
        intent.setData(Uri.parse(link));
        intent.setAction(Intent.ACTION_VIEW);
        this.startActivity(intent);
        new Thread(){
            public void run()
            {
                for(int i =0;i<clickReportArrayList.size();i++)
                {
                    try {
                        URL url = new URL((String) showReportArrayList.get(i));
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                        if (200 == urlConnection.getResponseCode()) {
                            Message msg = new Message();
                            msg.what = CLICK_REPORTED;
                            handler.sendMessage(msg);
                        } else {
                            Message msg = new Message();
                            msg.what = CLICK_REPORT_ERROR;
                        }
                    }catch (Exception e)
                    {
                        Message msg = new Message();
                        msg.what = CLICK_REPORT_ERROR;

                    }

                }

            }


        }.start();

    }

    public void reportAdShow() {
        System.out.println("report ad showed");
        new Thread(){
            public void run(){
                for(int i =0;i<showReportArrayList.size();i++)
                {
                    try {
                        URL url = new URL((String) showReportArrayList.get(i));
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                        if (200 == urlConnection.getResponseCode()) {
                            Message msg = new Message();
                            msg.what = SHOW_REPORTED;
                            handler.sendMessage(msg);
                        } else {
                            Message msg = new Message();
                            msg.what = SHOW_REPORT_ERROR;
                        }
                    }catch (Exception e)
                    {
                        Message msg = new Message();
                        msg.what = SHOW_REPORT_ERROR;

                    }

                }

            }

        }.start();
    }

    public void getImage(String path) {
        try {

            File file = new File(getCacheDir(), Base64.encodeToString(
                    path.getBytes(), Base64.DEFAULT));
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url
                    .openConnection();
            int code = conn.getResponseCode();
            if (code == 200) {
                InputStream is = conn.getInputStream();// png的图片
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int len = -1;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                is.close();
                fos.close();
                Bitmap bitmap = BitmapFactory.decodeFile(file
                        .getAbsolutePath());
                //更新ui ，不能写在子线程
                Message msg = new Message();
                msg.obj = bitmap;
                msg.what = PIC_RECEIVED;
                handler.sendMessage(msg);
            } else {
                // 请求失败
                //土司更新ui，不能写在子线程
                //Toast.makeText(this, "请求失败", 0).show();
                Message msg = new Message();
                msg.what = PIC_REQUEST_ERROR;
                handler.sendMessage(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //土司不能写在子线程
            //Toast.makeText(this, "发生异常，请求失败", 0).show();
            Message msg = new Message();
            msg.what = PIC_REQUEST_EXCEPTION;
            handler.sendMessage(msg);
        }
    }


}
