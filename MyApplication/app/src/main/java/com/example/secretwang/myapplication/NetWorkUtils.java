package com.example.secretwang.myapplication;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Created by AaronLee on 2015/12/23.
 */
public class NetWorkUtils {
    /**
     * 检查网络是否可用
     */
    public static boolean checkEnable(Context context) {
        boolean i = false;
        NetworkInfo localNetworkInfo = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if ((localNetworkInfo != null) && (localNetworkInfo.isAvailable()))
            return true;
        return false;
    }
    //获取本机WIFI
    public static String getLocalIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        // 获取32位整型IP地址
        int ipAddress = wifiInfo.getIpAddress();
        //返回整型地址转换成“*.*.*.*”地址
        return String.format("%d.%d.%d.%d",
                (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }
    //3G网络IP
    public static String getIpAddress( ) {
        try {
            for(Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();// 得到每一个网络接口绑定的所有ip
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取当前的网络状态 -1：没有网络 1：WIFI网络2：wap网络3：net网络
     * @param context
     * @return
     */
    public static int getAPNType(Context context){
        int netType = -1;
        int CMNET = 0;
        int CMWAP = 0;
        int WIFI = 1;
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if(networkInfo==null){
            return netType;
        }
        int nType = networkInfo.getType();
        if(nType==ConnectivityManager.TYPE_MOBILE) {
            String extraInfo = networkInfo.getExtraInfo();
            if (!extraInfo.equals("")) {
                if (networkInfo.getExtraInfo().toLowerCase().equals("cmnet")) {
                    netType = CMNET;
                } else {
                    netType = CMWAP;
                }
            }
        }
        else if(nType==ConnectivityManager.TYPE_WIFI){
            netType = WIFI;
        }
        return netType;
    }
}
