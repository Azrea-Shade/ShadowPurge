package com.azreashade.shadowpurge;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;

public class AppsFragment extends Fragment {
    private static final String ARG_IS_SYSTEM = "is_system";

    public static AppsFragment newInstance(boolean isSystem) {
        AppsFragment f = new AppsFragment();
        Bundle b = new Bundle();
        b.putBoolean(ARG_IS_SYSTEM, isSystem);
        f.setArguments(b);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle s) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        Context ctx = requireContext();
        RecyclerView rv = v.findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(ctx));

        boolean isSystem = getArguments() != null && getArguments().getBoolean(ARG_IS_SYSTEM, false);

        AppRepository repo = new AppRepository(ctx);
        List<AppInfo> data = isSystem ? repo.getSystemApps() : repo.getUserApps();

        ExclusionManager excl = new ExclusionManager(ctx);
        AppListAdapter adapter = new AppListAdapter(data, excl, pkg -> {
            excl.toggle(pkg);
            // notify item changed by finding its position
            int pos = adapter.findPositionByPackage(pkg);
            if (pos >= 0) adapter.notifyItemChanged(pos);
        });
        rv.setAdapter(adapter);
    }
}
