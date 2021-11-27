package com.huseyn.myapplock.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class SharedPrefUtil {
    private static final String SHARED_APP_PREFERENCE_NAME = "SharedPref";
    Context cxt;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public SharedPrefUtil(Context con){
        this.pref = con.getSharedPreferences(SHARED_APP_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public static SharedPrefUtil getInstance(Context context){
        return new SharedPrefUtil(context);
    }

    public void putString(String key, String value) {
        pref.edit().putString(key, value).apply();
    }

    public void putInteger(String key, int value) {
        pref.edit().putInt(key, value).apply();
    }

    public void putBoolean(String key, boolean value) {
        pref.edit().putBoolean(key, value).apply();
    }

    public String getString(String key)
    {
        return pref.getString(key,"");
    }

    public int getInteger(String key)
    {
        return pref.getInt(key,0);
    }

    public boolean getBoolean(String key)
    {
        return pref.getBoolean(key,false);
    }

    public void putStringList(List<String> list){
        for (int i = 0; i< list.size(); i++){
            putString("app_"+i, list.get(i));
        }
        putInteger("listSize", list.size());
    }

    public List<String> getStringList(){
        int size = getInteger("listSize");
        List<String> temp = new ArrayList<>();
        for(int i = 0; i < size; i++){
            temp.add(getString("app_"+i));
        }

        return temp;
    }


}
