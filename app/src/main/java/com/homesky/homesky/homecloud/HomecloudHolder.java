package com.homesky.homesky.homecloud;

import android.content.Context;

import com.homesky.homecloud_lib.Homecloud;
import com.homesky.homecloud_lib.HomecloudWrapper;

public class HomecloudHolder {
    private static HomecloudWrapper instance = null;

    private static String url;
    private static String username;
    private static String password;
    private static String token;

    public static void setUrl(String url) {
        HomecloudHolder.url = url;
    }

    public static void setUsername(String username) {
        HomecloudHolder.username = username;
        if(instance != null)
            instance.setUsername(username);
    }

    public static void setPassword(String password) {
        HomecloudHolder.password = password;
        if(instance != null)
            instance.setPassword(password);
    }

    public static void setToken(String token) {
        HomecloudHolder.token = token;
        if(instance != null)
            instance.setToken(token);
    }

    public static HomecloudWrapper getInstance(){
        if(instance == null) {
            instance = new HomecloudWrapper(url);
        }
        return instance;
    }
}
