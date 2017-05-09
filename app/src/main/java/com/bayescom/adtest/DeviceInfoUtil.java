package com.bayescom.adtest;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by allen on 2017/5/8.
 */

public class DeviceInfoUtil {
    private Activity activity;
    public DeviceInfoUtil(Activity activity)
    {
        this.activity = activity;

    }
    public JSONObject getMockDeviceInfo()
    {
        //mock request Json
        JSONObject jsonObject = new JSONObject();
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
        try {
               jsonObject= new JSONObject(jsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;

    }
    public JSONObject getDeviceInfo() {
        JSONObject jsonObject = new JSONObject();
        try {
            //set interface version
            jsonObject.put("version", "2.0");
            //set timestamp
            String timestamp = System.currentTimeMillis() + "";
            jsonObject.put("time", "1493966475774");
            timestamp = "1493966475774";
            //set token
            String tokenRaw = "100144"+"1c75b8bbe58398d2930fa4afb85d87db"+timestamp;
            String token = getMD5(tokenRaw);
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
            Integer sw = activity.getResources()
                    .getDisplayMetrics().widthPixels;
            // 屏幕高度(px)
            Integer sh = activity.getResources()
                    .getDisplayMetrics().heightPixels;
            Integer ppi = activity.getResources()
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
        Context context = activity.getApplicationContext();
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
        TelephonyManager mTm = (TelephonyManager) activity.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
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
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
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
        return Settings.System.getString(activity.getContentResolver(), Settings.System.ANDROID_ID);
    }

    private String getCarrier() {
        TelephonyManager telManager = (TelephonyManager)activity.getSystemService(Context.TELEPHONY_SERVICE);
        String operator = telManager.getSimOperator();
        return operator;

    }

    private Integer getNetwork() {
        int network = 0;
        ConnectivityManager connectivity = (ConnectivityManager)activity.getApplicationContext()
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
    private  String getMD5(String s) {
        char hexDigits[]={ '0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
