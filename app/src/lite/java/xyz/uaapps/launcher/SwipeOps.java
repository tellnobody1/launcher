package xyz.uaapps.launcher;

public class SwipeOps {
    public static void init(F f) {}

    public interface F {
        void onRefresh();
        SwipeLayout view(int id);
    }
}
