package com.iteration.relaxio.network;

import android.content.Context;
import android.content.SharedPreferences;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class Config {

    public static final String SHARED_PREF = "ah_firebase";
    public static final String REG_ID = "regId";
    public static final String isToken = "isToken";
    public static final int NOTIFICATION_ID = 100;

    public static String getToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = pref.getString(REG_ID, null);
        return regId;
    }

    public static boolean uploadToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences(Config.SHARED_PREF, 0);
        return pref.getBoolean(isToken, false);
    }

    public static void setUploadToken(Context context, boolean b) {
        SharedPreferences pref = context.getSharedPreferences(Config.SHARED_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(isToken, b);
        editor.commit();
    }

    public static String getWifiMacAddress() {
        try {
            String interfaceName = "wlan0";
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (!intf.getName().equalsIgnoreCase(interfaceName)) {
                    continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac == null) {
                    return "";
                }
                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) {
                    buf.append(String.format("%02X:", aMac));
                }
                if (buf.length() > 0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                return buf.toString();
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return "";
    }

}
