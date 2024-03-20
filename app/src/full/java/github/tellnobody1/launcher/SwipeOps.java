package github.tellnobody1.launcher;

import androidx.annotation.*;
import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;

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
