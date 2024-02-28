package xyz.uaapps.launcher;

import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;

import androidx.annotation.IdRes;
import androidx.annotation.RequiresApi;

public class SwipeOps {
    @RequiresApi(api = ICE_CREAM_SANDWICH)
    static void init(F f) {
        var swipeLayout = f.view(R.id.swipeLayout);
        swipeLayout.setOnRefreshListener(() -> {
            f.onRefresh();
            swipeLayout.setRefreshing(false);
        });
    }

    interface F {
        void onRefresh();
        SwipeLayout view(@IdRes int id);
    }
}
