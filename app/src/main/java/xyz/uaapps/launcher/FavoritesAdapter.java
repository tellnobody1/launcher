package xyz.uaapps.launcher;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class FavoritesAdapter extends BaseAdapter {
    private final ArrayList<RegularLaunchableActivity> favorites;

    public FavoritesAdapter(ArrayList<RegularLaunchableActivity> favorites) {
        this.favorites = favorites;
    }

    public int getCount() {
        return favorites.size();
    }

    public RegularLaunchableActivity getItem(int position) {
        return favorites.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final View view;
        if (convertView == null) {
            var inflater = LayoutInflater.from(parent.getContext());
            view = inflater.inflate(R.layout.app_grid_item, parent, false);
        } else {
            view = convertView;
        }
        view.setVisibility(VISIBLE);

        var appLabelView = view.<TextView>findViewById(R.id.appLabel);
        appLabelView.setVisibility(GONE);

        var appIconView = view.<AppIconView>findViewById(R.id.appIcon);

        var launchableActivity = getItem(position);
        appIconView.setTag(launchableActivity);

        var label = launchableActivity.getActivityLabel();
        appIconView.set(label, launchableActivity.getIconKey());

        return view;
    }
}
