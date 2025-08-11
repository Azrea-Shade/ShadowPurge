package com.azreashade.shadowpurge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.VH> {
    public interface OnToggle { void onToggle(String pkg); }

    private final List<AppInfo> items;
    private final ExclusionManager excl;
    private final OnToggle toggleCb;

    public AppListAdapter(List<AppInfo> items, ExclusionManager excl, OnToggle toggleCb) {
        this.items = items;
        this.excl = excl;
        this.toggleCb = toggleCb;
    }

    static class VH extends RecyclerView.ViewHolder {
        CheckBox chk;
        TextView name, pkg;
        VH(View v) {
            super(v);
            chk = v.findViewById(R.id.checkExclude);
            name = v.findViewById(R.id.txtAppName);
            pkg = v.findViewById(R.id.txtPkg);
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        AppInfo a = items.get(pos);
        h.name.setText(a.appName);
        h.pkg.setText(a.packageName);
        h.chk.setChecked(excl.isExcluded(a.packageName));

        View.OnClickListener click = v -> toggleCb.onToggle(a.packageName);
        h.itemView.setOnClickListener(click);
        h.chk.setOnClickListener(click);
    }

    @Override
    public int getItemCount() { return items.size(); }

    public int findPositionByPackage(String packageName) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).packageName.equals(packageName)) return i;
        }
        return -1;
    }
}
