package com.bayescom.adtest;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
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
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

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
//        final JSONObject jsonObject = getDeviceInfo();
//        System.out.println(jsonObject.toString());
        //mock request Json
        String jsonStr = "{\n" +
                "    \"androidid\":\"a3e5c5f9c1e370cc\",\n" +
                "    \"sh\":1794,\n" +
                "    \"reqid\":\"d67411e0315d11e787e5e57138710211\",\n" +
                "    \"token\":\"f6eee0e8a70b4dc7a4db35802d137f70\",\n" +
                "    \"ua\":\"Mozilla/5.0 (Linux; Android 7.0; EVA-AL00 Build/HUAWEIEVA-AL00; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/55.0.2883.91 Mobile Safari/537.36\",\n" +
                "    \"mac\":\"58:02:03:04:05:06:\",\n" +
                "    \"impsize\":1,\n" +
                "    \"time\":\"1493966475774\",\n" +
                "    \"ppi\":480,\n" +
                "    \"version\":\"2.0\",\n" +
                "    \"sw\":1080,\n" +
                "    \"appid\":\"100144\",\n" +
                "    \"carrier\":\"46000\",\n" +
                "    \"adspotid\":\"10000334\",\n" +
                "    \"model\":\"EVA-AL00\",\n" +
                "    \"appver\":\"3.5.1.7\",\n" +
                "    \"make\":\"HUAWEI\",\n" +
                "    \"os\":2,\n" +
                "    \"lat\":36.106565554497,\n" +
                "    \"lon\":120.35140907084,\n" +
                "    \"imei\":\"869158023529130\",\n" +
                "    \"network\":4,\n" +
                "    \"devicetype\":0,\n" +
                "    \"ip\":\"117.136.77.99\",\n" +
                "    \"osv\":\"7.0\"\n" +
                "}";
        final JSONObject jsonObject = new JSONObject(jsonStr);
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
                            msg.what = CLICK_REPORTED;
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

    public JSONObject getDeviceInfo() {
        JSONObject jsonObject = new JSONObject();
        try {
            //set interface version
            jsonObject.put("version", "2.0");
            //set timestamp
            String timestamp = System.currentTimeMillis() + "";
            jsonObject.put("time", timestamp);
            //set token
            jsonObject.put("token", "f6eee0e8a70b4dc7a4db35802d137f70");
            //set reqid
            jsonObject.put("reqid", "d67411e0315d11e787e5e57138710211");
            //set appid
            jsonObject.put("appid", "100144");
            //set adspotid
            jsonObject.put("adspotid", "10000334");
            //set appver
            String appver = "1.0";
            jsonObject.put("appver", appver);
            Integer impsize = 1;
            jsonObject.put("impsize", impsize);
            //model
            String model = Build.MODEL;
            jsonObject.put("model", model);
            String make = Build.MANUFACTURER;
            jsonObject.put("make", make);
            String osv = Build.VERSION.RELEASE;
            jsonObject.put("osv", osv);
            Integer os = 2;
            jsonObject.put("os", os);
            String ip = getIP();
            jsonObject.put("ip", ip);
            String ua = getCurrentUserAgent();
            jsonObject.put("ua", ua);
            String imei = getPhoneIMEI();
            jsonObject.put("imei", imei);
            String mac = getMacAddress();
            jsonObject.put("mac", mac);
            Location location = getLocation();
            if (location != null) {
                Double lat = location.getLatitude();
                Double lon = location.getLongitude();
                jsonObject.put("lat", lat);
                jsonObject.put("lon", lon);
            }
            String androidid = getAndroidid();
            jsonObject.put("androidid", androidid);
            Integer sw = this.getResources()
                    .getDisplayMetrics().widthPixels;
            // 屏幕高度(px)
            Integer sh = this.getResources()
                    .getDisplayMetrics().heightPixels;
            Integer ppi = this.getResources()
                    .getDisplayMetrics().densityDpi;
            jsonObject.put("sw", sw);
            jsonObject.put("sh", sh);
            jsonObject.put("ppi", ppi);

            Integer devicetype = 1;
            jsonObject.put("devicetype", devicetype);

            String carrier = getCarrier();
            jsonObject.put("carrier", carrier);
            Integer network = getNetwork();
            jsonObject.put("network", network);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private String getIP() {
        Context context = getApplicationContext();
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                //调用方法将int转换为地址字符串
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            return "";
            //当前无网络连接,请在设置中打开网络
        }
        return "";
    }

    private String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

    private String getCurrentUserAgent() {
        String userAgent = System.getProperty("http.agent");
        return userAgent;

    }

    private String getPhoneIMEI() {
        TelephonyManager mTm = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        String imei = mTm.getDeviceId();
        return imei;
    }


    private String getMacAddress() {
 /*获取mac地址有一点需要注意的就是android 6.0版本后，以下注释方法不再适用，不管任何手机都会返回"02:00:00:00:00:00"这个默认的mac地址，这是googel官方为了加强权限管理而禁用了getSYstemService(Context.WIFI_SERVICE)方法来获得mac地址。*/
        //        String macAddress= "";
//        WifiManager wifiManager = (WifiManager) MyApp.getContext().getSystemService(Context.WIFI_SERVICE);
//        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//        macAddress = wifiInfo.getMacAddress();
//        return macAddress;
        String macAddress = null;
        StringBuffer buf = new StringBuffer();
        NetworkInterface networkInterface = null;
        try {
            networkInterface = NetworkInterface.getByName("eth1");
            if (networkInterface == null) {
                networkInterface = NetworkInterface.getByName("wlan0");
            }
            if (networkInterface == null) {
                return "02:00:00:00:00:02";
            }
            byte[] addr = networkInterface.getHardwareAddress();
            for (byte b : addr) {
                buf.append(String.format("%02X:", b));
            }
            if (buf.length() > 0) {
                buf.deleteCharAt(buf.length() - 1);
            }
            macAddress = buf.toString();
        } catch (SocketException e) {
            e.printStackTrace();
            return "02:00:00:00:00:02";
        }
        return macAddress;
    }

    private Location getLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String locationProvider;

        //获取所有可用的位置提供器
        List<String> providers = locationManager.getProviders(true);
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            //如果是GPS
            locationProvider = LocationManager.GPS_PROVIDER;
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            //如果是Network
            locationProvider = LocationManager.NETWORK_PROVIDER;
        } else {
            return null;
        }
        //获取Location
        Location location = locationManager.getLastKnownLocation(locationProvider);
        if (location != null) {
            //不为空,显示地理位置经纬度
            return location;
        } else {
            return null;
        }
    }

    private String getAndroidid() {
        return Settings.System.getString(getContentResolver(), Settings.System.ANDROID_ID);
    }

    private String getCarrier() {
        TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String operator = telManager.getSimOperator();
        return operator;

    }

    private Integer getNetwork() {
        int network = 0;
        ConnectivityManager connectivity = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return network;
        }

        NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                network = 1;
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                String _strSubTypeName = networkInfo.getSubtypeName();

                // TD-SCDMA   networkType is 17
                int networkType = networkInfo.getSubtype();
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                        network = 2;
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                    case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                    case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                        network = 3;
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                        network = 4;
                        break;
                    default:
                        //中国移动 联通 电信 三种3G制式
                        if (_strSubTypeName.equalsIgnoreCase("TD-SCDMA") || _strSubTypeName.equalsIgnoreCase("WCDMA") || _strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                            network = 3;
                        } else {
                            network = 0;
                        }
                        break;
                }
            }
        }

        return network;
    }

}
