package com.bayescom.adtest;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Base64;
import android.view.Menu;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.UUID;

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
    private static final int ADSPOT_INFO_RECEIVED=11;
    private static final int ADSPOT_INFO_ERROR=12;

    private static final String POST_URL = "http://shuttle.bayescom.com/shuttle";
    private static final String JUPITER_URL = "http://jupiter.bayescom.com/dspapi/v1/mediaInfo/";
    private  ArrayList showReportArrayList;
    private ArrayList clickReportArrayList;
    private String link = "";
    private String htmlString="";
    private ArrayList imageUrlArrayList;
    private ArrayList wordArrayList;
    private String adspotId ="10000334";
    private String mediaId ="100144";
    private String mediaKey  ="1c75b8bbe58398d2930fa4afb85d87db";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        SharedPreferences sp = getSharedPreferences("adspotInfo", getApplicationContext().MODE_PRIVATE);
        String storeMediaId = sp.getString("mediaId",null);
        String storeMediaKey = sp.getString("mediaKey",null);
        String storeAdspotId= sp.getString("adspotId",null);
        if(storeMediaId!=null&&storeAdspotId!=null&&storeMediaKey!=null)
        {
            mediaId = storeMediaId;
            mediaKey =storeMediaKey;
            adspotId =storeAdspotId;
        }
        toolbar.setTitle(adspotId);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_set_adspot:
                final EditText et = new EditText(this);
                et.setInputType(InputType.TYPE_CLASS_NUMBER);
                new AlertDialog.Builder(this)
                        .setTitle("请输入")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(et)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                              final  String adsId=et.getText().toString();
                              new Thread(){
                                  @Override
                                  public void run()
                                  {
                                      requestAdspotInfo(adsId);
                                  }

                              }.start();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
                break;
       }

        return super.onOptionsItemSelected(item);
    }


    //1.在主线程里面声明消息处理器 handler
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            LinearLayout ad_group = (LinearLayout) findViewById(R.id.ad_v_layout);
            switch (msg.what) {
                case PIC_RECEIVED:
                    Bitmap bitmap2 = (Bitmap) msg.obj;
                    System.out.println("图片正常显示");
                    ImageView iv = new ImageView(getApplicationContext());
                    iv.setImageBitmap(bitmap2);
                    ad_group.addView(iv);

                   break;
                case AD_RECEIVED:
                    String rspJson =(String) msg.obj;

                    showReportArrayList = new ArrayList();
                    clickReportArrayList = new ArrayList();
                    imageUrlArrayList = new ArrayList();
                    wordArrayList = new ArrayList();
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
                           JSONArray image = impObj.optJSONArray("image") ;
                           JSONArray word =  impObj.optJSONArray("word");
                           JSONArray imptk = impObj.optJSONArray("imptk");
                           JSONArray clicktk = impObj.optJSONArray("clicktk");
                           htmlString  = impObj.optString("htmlstring");
                           link = impObj.getString("link");
                           if(word !=null)
                           {
                               for(int a =0;a<word.length();a++)
                               {
                                   JSONObject wordObject = word.getJSONObject(a);
                                   wordArrayList.add( wordObject.getString("text"));
                               }
                           }
                           if(image !=null) {

                               for (int b = 0; b < image.length(); b++) {
                                   JSONObject imageObj = image.getJSONObject(b);
                                   imageUrlArrayList.add(imageObj.getString("iurl"));
                               }
                           }
                           if(imptk !=null) {
                               for (int c = 0; c < imptk.length(); c++)
                               {
                                   showReportArrayList.add(imptk.getString(c));

                               }
                           }
                           if(clicktk !=null) {
                               for (int d = 0; d < clicktk.length(); d++) {
                                   clickReportArrayList.add(clicktk.getString(d));
                               }
                           }

                        }
                        //set the ad text if any
                        for (int e =0;e<wordArrayList.size();e++) {
                            String text = (String)wordArrayList.get(e);
                            TextView tv = new TextView(getApplicationContext());
                            tv.setTextColor(Color.RED);
                            tv.setText(text);
                            ad_group.addView(tv);
                        }
                        //request the ad images
                        for(int f=0;f<imageUrlArrayList.size();f++)
                        {
                            final String imgUrl = (String)imageUrlArrayList.get(f);
                            new Thread(){
                                public void run() {
                                    getImage(imgUrl);
                                }

                            }.start();

                        }
                        //set the html webView if any

                        if(htmlString!=""&&htmlString!=null)
                        {
                            WebView webView = new WebView(getApplicationContext());
                            webView.loadDataWithBaseURL(null, htmlString, "text/html", "utf-8", null);
                            webView.getSettings().setJavaScriptEnabled(true);
                            webView.setWebChromeClient(new WebChromeClient());
                            ad_group.addView(webView);

                        }
                       //添加点击回调
                        ad_group.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View v)
                            {
                                adDidClick(v);
                            }
                        });
                      //上报展示
                        reportAdShow();


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
                case ADSPOT_INFO_RECEIVED:
                    try {
                        JSONObject jsonObject = (JSONObject) msg.obj;
                        mediaKey = jsonObject.getString("media_key");
                        mediaId = jsonObject.getString("media_id");
                        adspotId = jsonObject.getString("adspot_id");
                        SharedPreferences sp = getSharedPreferences("adspotInfo", getApplicationContext().MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("adspotId", adspotId);
                        editor.putString("mediaId", mediaId);
                        editor.putString("mediaKey",mediaKey);
                        editor.commit();
                        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                        toolbar.setTitle(adspotId);
                    }catch (Exception e)
                    {
                        Toast.makeText(MainActivity.this, "广告位信息解析出错", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case ADSPOT_INFO_ERROR:
                    Toast.makeText(MainActivity.this, "广告位信息请求出错", Toast.LENGTH_SHORT).show();
                    break;
            }

            super.handleMessage(msg);
        }
    };


    public void loadAd(View view) throws JSONException {

        LinearLayout ad_group = (LinearLayout) findViewById(R.id.ad_v_layout);
        ad_group.removeAllViews();

        DeviceInfoUtil devu = new DeviceInfoUtil(this);
        final JSONObject jsonObject = devu.getDeviceInfo(adspotId,mediaId,mediaKey);
//        final JSONObject jsonObject = devu.getMockDeviceInfo();
        System.out.println(jsonObject.toString());


        new Thread() {
            public void run() {
                requestAd(jsonObject);
            }

        }.start();

    }
    public void requestAdspotInfo(String adspotId)
    {
        System.out.println(adspotId);
        try {
            String requestUrl = JUPITER_URL+adspotId;
            URL url = new URL(requestUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            if (200 == urlConnection.getResponseCode()) {
                // 请求返回的数据
                BufferedReader in = null;
                String result = "";
                in = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    result +=  line;
                }
                Message msg = new Message();
                msg.what = ADSPOT_INFO_RECEIVED;
                JSONObject jsonObject = new JSONObject(result);
                jsonObject.put("adspot_id",adspotId);
                msg.obj = jsonObject;
                handler.sendMessage(msg);
            } else {
                Message msg = new Message();
                msg.what = ADSPOT_INFO_ERROR;
                handler.sendMessage(msg);
            }
        }catch (Exception e)
        {
            Message msg = new Message();
            msg.what = ADSPOT_INFO_ERROR;
            handler.sendMessage(msg);

        }


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
        System.out.println(link);
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
                            handler.sendMessage(msg);
                        }
                    }catch (Exception e)
                    {
                        Message msg = new Message();
                        msg.what = CLICK_REPORT_ERROR;
                        handler.sendMessage(msg);

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
                            handler.sendMessage(msg);
                        }
                    }catch (Exception e)
                    {
                        Message msg = new Message();
                        msg.what = SHOW_REPORT_ERROR;
                        handler.sendMessage(msg);

                    }

                }

            }

        }.start();
    }

    public void getImage(String path) {
        try {
            File file = new File(getCacheDir(), UUID.randomUUID().toString());
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url
                    .openConnection();
            int code = conn.getResponseCode();
            if (code == 200) {
                InputStream is = conn.getInputStream();
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
