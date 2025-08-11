package com.azreashade.shadowpurge;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppRepository {
    private final PackageManager pm;

    public AppRepository(Context ctx) {
        pm = ctx.getPackageManager();
    }

    public List<AppInfo> getUserApps() {
        List<ApplicationInfo> list = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<AppInfo> out = new ArrayList<>();
        for (ApplicationInfo ai : list) {
            boolean isSystem = (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            if (!isSystem) {
                out.add(new AppInfo(pm.getApplicationLabel(ai).toString(), ai.packageName, false));
            }
        }
        sort(out);
        return out;
    }

    public List<AppInfo> getSystemApps() {
        List<ApplicationInfo> list = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<AppInfo> out = new ArrayList<>();
        for (ApplicationInfo ai : list) {
            boolean isSystem = (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            if (isSystem) {
                out.add(new AppInfo(pm.getApplicationLabel(ai).toString(), ai.packageName, true));
            }
        }
        sort(out);
        return out;
    }

    private void sort(List<AppInfo> apps) {
        Collections.sort(apps, new Comparator<AppInfo>() {
            @Override public int compare(AppInfo a, AppInfo b) {
                return a.appName.toLowerCase().compareTo(b.appName.toLowerCase());
            }
        });
    }
}
