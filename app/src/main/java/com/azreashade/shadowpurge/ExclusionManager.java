package com.azreashade.shadowpurge;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class ExclusionManager {
    private static final String PREF = "shadow_prefs";
    private static final String KEY = "excluded_pkgs";
    private final SharedPreferences prefs;

    public ExclusionManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public Set<String> getExcluded() {
        return new HashSet<>(prefs.getStringSet(KEY, new HashSet<String>()));
    }

    public void setExcluded(Set<String> pkgs) {
        prefs.edit().putStringSet(KEY, pkgs).apply();
    }

    public boolean isExcluded(String pkg) {
        return getExcluded().contains(pkg);
    }

    public void toggle(String pkg) {
        Set<String> s = getExcluded();
        if (s.contains(pkg)) s.remove(pkg); else s.add(pkg);
        setExcluded(s);
    }
}
