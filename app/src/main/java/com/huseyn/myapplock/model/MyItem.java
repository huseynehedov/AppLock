package com.huseyn.myapplock.model;

import android.graphics.drawable.Drawable;

import java.util.Objects;

public class MyItem {
    private Drawable icon;
    private String packageName;
    private String name;
    private boolean appStatus;

    public MyItem(Drawable icon, String name, String packageName, boolean appStatus) {
        this.icon = icon;
        this.packageName = packageName;
        this.name = name;
        this.appStatus = appStatus;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAppStatus() {
        return appStatus;
    }

    public void setAppStatus(boolean appStatus) {
        this.appStatus = appStatus;
    }
}

