package com.azreashade.shadowpurge;

public class AppInfo {
    public final String appName;
    public final String packageName;
    public final boolean isSystem;

    public AppInfo(String appName, String packageName, boolean isSystem) {
        this.appName = appName;
        this.packageName = packageName;
        this.isSystem = isSystem;
    }
}
