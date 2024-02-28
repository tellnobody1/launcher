package xyz.uaapps.launcher;

import androidx.annotation.IdRes;

public class SwipeOps {
    public static void init(F f) {}

    interface F {
        void onRefresh();
        SwipeLayout view(@IdRes int id);
    }
}

interface SwipeLayout {}
