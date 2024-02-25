/*
 * Copyright 2015-2017 Hayai Software
 * Copyright 2018-2022 The KeikaiLauncher Project
 * Copyright 2024 uaapps
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.uaapps.launcher.activities;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.CUPCAKE;
import static android.os.Build.VERSION_CODES.DONUT;
import static android.os.Build.VERSION_CODES.FROYO;
import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N;
import static android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS;
import static android.provider.Settings.System.ACCELEROMETER_ROTATION;
import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_ENTER;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_GO;
import static java.util.Collections.emptyMap;
import static xyz.uaapps.launcher.BuildConfig.DEBUG;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DeprecatedSinceApi;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import xyz.uaapps.launcher.LaunchableActivity;
import xyz.uaapps.launcher.LaunchableActivityPrefs;
import xyz.uaapps.launcher.LaunchableAdapter;
import xyz.uaapps.launcher.LauncherActivityInfoOps;
import xyz.uaapps.launcher.LocaleConfig;
import xyz.uaapps.launcher.R;
import xyz.uaapps.launcher.RegularLaunchableActivity;
import xyz.uaapps.launcher.ResolveInfoOps;
import xyz.uaapps.launcher.SharedLauncherPrefs;
import xyz.uaapps.launcher.monitor.PackageChangeCallback;
import xyz.uaapps.launcher.monitor.PackageChangedReceiver;
import xyz.uaapps.launcher.swipe.SwipeLayout;

public class MainActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener, PackageChangeCallback {

    /**
     * Synchronize to this lock when the Adapter is visible and might be called by multiple threads.
     */
    private final Object mLock = new Object();

    /**
     * This {@link BroadcastReceiver} implements an updater for package changes.
     */
    private final BroadcastReceiver mPackageChangeReceiver = new PackageChangedReceiver();

    /**
     * This implements a listener for orientation change, see {@link DisplayChangeListener} for
     * more information.
     */
    @RequiresApi(api = JELLY_BEAN_MR1)
    private DisplayManager.DisplayListener mDisplayListener = null;

    /**
     * This ContentObserver is used by the ContentResolver to register a callback to set rotation in case it changes in the system settings.
     */
    private final ContentObserver mAccSettingObserver = new ContentObserver(new Handler()) {
        @Override public void onChange(boolean selfChange) {
            setRotation(new SharedLauncherPrefs(MainActivity.this));
        }
    };

    /**
     * An adapter, based off {@link android.widget.ArrayAdapter}, to handle
     * {@link LaunchableActivity} items.
     */
    private LaunchableAdapter<LaunchableActivity> mAdapter;

    private EditText mSearchEditText;

    private static int getDimensionSize(Resources resources, String name) {
        var resourceId = resources.getIdentifier(name, "dimen", "android");
        return resourceId > 0 ? resources.getDimensionPixelSize(resourceId) : 0;
    }

    private static LaunchableActivity getLaunchableActivity(View view) {
        return (LaunchableActivity) view.findViewById(R.id.appIcon).getTag();
    }

    private static LaunchableActivity getLaunchableActivity(ContextMenuInfo menuInfo) {
        return getLaunchableActivity(((AdapterContextMenuInfo) menuInfo).targetView);
    }

    private static LaunchableActivity getLaunchableActivity(MenuItem item) {
        return getLaunchableActivity(item.getMenuInfo());
    }

    private static Collection<ResolveInfo> getLaunchableResolveInfos(PackageManager pm, @Nullable String activityName) {
        var intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        if (SDK_INT >= DONUT)
            intent.setPackage(activityName);
        return pm.queryIntentActivities(intent, 0);
    }

    @DeprecatedSinceApi(api = VERSION_CODES.R, message =
            "Later APIs use get getNavigationBarHeight30()")
    private static int getNavigationBarHeight15(Resources resources) {
        var configuration = resources.getConfiguration();
        //Only phone between 0-599 has navigationbar can move
        var isSmartphone = configuration.smallestScreenWidthDp < 600;
        var isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT;

        int navBarHeight;
        if (isSmartphone && !isPortrait)
            navBarHeight = 0;
        else if (isPortrait)
            navBarHeight = getDimensionSize(resources, "navigation_bar_height");
        else
            navBarHeight = getDimensionSize(resources, "navigation_bar_height_landscape");
        return navBarHeight;
    }

    public static int getAppUsableScreenSizeWidth(Display defaultDisplay) {
        Point size = new Point();
        defaultDisplay.getSize(size);
        return size.x;
    }

    public static int getRealScreenWidth(Display defaultDisplay) {
        var size = new Point();
        if (SDK_INT >= JELLY_BEAN_MR1) {
            defaultDisplay.getRealSize(size);
        } else if (SDK_INT >= ICE_CREAM_SANDWICH) {
            try {
                size.x = (Integer) Display.class.getMethod("getRawWidth").invoke(defaultDisplay);
            } catch (IllegalAccessException ignored) {
            } catch (InvocationTargetException ignored) {
            } catch (NoSuchMethodException ignored) {
            }
        }
        return size.x;
    }

    /**
     * This is a workaround. Unfortunately, Android does not have a way in it's API to
     * accurately detect the position of the navigation bar when it's translucent.
     * This becomes highly problematic in landscape where we really don't want Launchables
     * underneath the navigation bar when in landscape mode. This forces the master layout outside
     * of the status bar and the navigation bar. Ideally, we would be forced outside of the
     * navigation bar alone and be able to travel underneath the status bar. The inconsistency of
     * this layout which affects landscape orientation and non-gesture mode is outweighed by the
     * really crappy hacks that used to try to work around this.
     *
     * @param context The current context.
     * @return True if the navigation bar is not in gesture mode, the device is in landscape, and
     * the application usable space is less than the real space.
     */
    public static boolean isNavBarProblematic(Context context) {
        var resources = context.getResources();
        var isLandscape = resources.getConfiguration().orientation == ORIENTATION_LANDSCAPE;
        return !isInGestureMode(resources) && isLandscape && isRealSizeDifferentThanUsable(context);
    }

    public static boolean isInGestureMode(Resources resources) {
        var resourceId = resources.getIdentifier("config_navBarInteractionMode", "integer", "android");
        return resourceId != 0 && resources.getInteger(resourceId) == 2;
    }

    private static boolean isRealSizeDifferentThanUsable(Context context) {
        var display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        return getAppUsableScreenSizeWidth(display) < getRealScreenWidth(display);
    }

    /**
     * This method adds LauncherActivityInfo objects to an adapter in API 24+.
     *
     * @param adapter  The adapter to add to.
     * @param infoList The objects to add to the adapter.
     */
    @TargetApi(N)
    private void addToAdapter(
            @NonNull LaunchableAdapter<LaunchableActivity> adapter,
            @NonNull Iterable<LauncherActivityInfo> infoList,
            Map<LauncherActivityInfo, Map<Locale, String>> labels) {
        var thisCanonicalName = getClass().getCanonicalName();
        var manager = (UserManager) getSystemService(USER_SERVICE);
        for (var info : infoList)
            if (thisCanonicalName == null || !thisCanonicalName.startsWith(info.getName())) {
                Map<Locale, String> activityLabels = labels.getOrDefault(info, emptyMap());
                adapter.add(new RegularLaunchableActivity(info, manager, valuesSet(activityLabels), activityLabels.getOrDefault(Locale.US, null)));
            }
    }

    /**
     * This method adds ResolveInfo objects to an adapter in SDK 15-24, optionally using a
     * readCache.
     *
     * @param adapter      The adapter to add ResolveInfo object to.
     * @param infoList     The ResolveInfo object to add to the adapter.
     * @param useReadCache Whether to use a read cache.
     */
    private void addToAdapter1(
            @NonNull LaunchableAdapter<LaunchableActivity> adapter,
            @NonNull Iterable<ResolveInfo> infoList,
            boolean useReadCache,
            Map<ResolveInfo, Map<Locale, String>> labels) {
        var prefs = getPreferences(Context.MODE_PRIVATE);
        var thisCanonicalName = getClass().getCanonicalName();
        var manager = useReadCache ? getPackageManager() : null;

        for (var info : infoList) {
            if (thisCanonicalName == null || !thisCanonicalName.startsWith(info.activityInfo.packageName)) {
                @Nullable var activityLabels = labels.get(info);
                @NonNull Map<Locale, String> activityLabels2 = activityLabels == null ? Collections.emptyMap() : activityLabels;
                String labelEn = activityLabels2.containsKey(Locale.US) ? activityLabels2.get(Locale.US) : null;
                adapter.add(new RegularLaunchableActivity(info, prefs, manager, valuesSet(activityLabels2), labelEn));
            }
        }
    }

    private static Set<String> valuesSet(Map<Locale, String> xs) {
        return new HashSet<>(xs.values());
    }

    private void showKeyboard() {
        mSearchEditText.requestFocus();
        if (SDK_INT >= CUPCAKE) {
            var imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            getWindow().setSoftInputMode(SOFT_INPUT_STATE_VISIBLE);
            imm.showSoftInput(mSearchEditText, 0);
        }
    }

    private void hideKeyboard() {
        if (SDK_INT >= CUPCAKE) {
            var focus = getCurrentFocus();
            if (focus != null) {
                var imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
            }
            getWindow().setSoftInputMode(SOFT_INPUT_STATE_HIDDEN);
        }
        findViewById(R.id.appsContainer).requestFocus();
    }

    private boolean isCurrentLauncher() {
        var pm = getPackageManager();
        var intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        var resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo != null && getPackageName().equals(resolveInfo.activityInfo.packageName);

    }

    private void launchActivity(LaunchableActivity launchableActivity) {
        hideKeyboard();
        // Second conditional is always true, but this shuts up warnings.
        if (launchableActivity.isUserKnown() && SDK_INT >= N) {
            var userManager = (UserManager) getSystemService(USER_SERVICE);
            var launcher = (LauncherApps) getSystemService(LAUNCHER_APPS_SERVICE);
            var userSerial = launchableActivity.getUserSerial();
            var userHandle = userManager.getUserForSerialNumber(userSerial);
            launcher.startMainActivity(launchableActivity.getComponent(), userHandle, null, Bundle.EMPTY);
        } else {
            try {
                startActivity(launchableActivity.getLaunchIntent());
                mSearchEditText.setText(null);
                var prefs = new LaunchableActivityPrefs(this);
                try {
                    prefs.writePreference(launchableActivity);
                } finally {
                    prefs.close();
                }
                mAdapter.sortApps();
            } catch (ActivityNotFoundException e) {
                if (DEBUG) throw e;
                else Toast.makeText(this, getString(R.string.activity_not_found), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void launchActivity(View view) {
        launchActivity(getLaunchableActivity(view));
    }

    public void launchApplicationDetails(MenuItem item) {
        var activity = getLaunchableActivity(item);
        var intent = new Intent(ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + activity.getComponent().getPackageName()));
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private LaunchableAdapter<LaunchableActivity> loadLaunchableAdapter() {
        LaunchableAdapter<LaunchableActivity> adapter;
        var pm = getPackageManager();
        if (SDK_INT >= N) {
            var manager = (UserManager) getSystemService(USER_SERVICE);
            var launcherApps = (LauncherApps) getSystemService(LAUNCHER_APPS_SERVICE);
            var iter = manager.getUserProfiles().listIterator();
            int count = 0;

            while (iter.hasNext()) {
                count += launcherApps.getActivityList(null, iter.next()).size();
            }

            adapter = new LaunchableAdapter<>(this, R.layout.app_grid_item, count);

            while (iter.hasPrevious()) {
                var activityList = launcherApps.getActivityList(null, iter.previous());
                var labels = getLabels(activityList, pm);
                addToAdapter(adapter, activityList, labels);
            }
        } else {
            var infoList = getLaunchableResolveInfos(pm, null);
            adapter = new LaunchableAdapter<>(this, R.layout.app_grid_item, infoList.size());
            var labels = getLabels_1(infoList, pm);
            addToAdapter1(adapter, infoList, true, labels);
        }
        adapter.sortApps();
        adapter.notifyDataSetChanged();
        return adapter;
    }

    @RequiresApi(api = N)
    private Map<LauncherActivityInfo, Map<Locale, String>> getLabels(List<LauncherActivityInfo> activityList, PackageManager pm) {
        var labels = new HashMap<LauncherActivityInfo, Map<Locale, String>>();
        var locales = getLabelLocales();
        for (var activityInfo : activityList) {
            var ops = new LauncherActivityInfoOps(activityInfo);
            labels.put(activityInfo, ops.getLabels(locales, pm));
        }
        return labels;
    }

    private Map<ResolveInfo, Map<Locale, String>> getLabels_1(Collection<ResolveInfo> infoList, PackageManager pm) {
        var labels = new HashMap<ResolveInfo, Map<Locale, String>>();
        var locales = getLabelLocales();
        for (var resolveInfo : infoList) {
            var ops = new ResolveInfoOps(resolveInfo, pm);
            labels.put(resolveInfo, ops.getLabels(locales));
        }
        return labels;
    }

    /** @return defaults + assets + english */
    private Set<Locale> getLabelLocales() {
        var locales = new HashSet<>(List.of(Locale.US, Locale.UK));
        // add default locales
        if (SDK_INT >= N) {
            var defaults = getResources().getConfiguration().getLocales();
            for (var i = 0; i < defaults.size(); i++) locales.add(defaults.get(i));
        } else {
            locales.add(Locale.getDefault());
        }
        // add assets locales
        for (var asset : LocaleConfig.LOCALES)
            if (SDK_INT >= LOLLIPOP) {
                locales.add(Locale.forLanguageTag(asset));
            } else {
                var parts = asset.split("-");
                locales.add(parts.length >= 2 ? new Locale(parts[0], parts[1]) : new Locale(parts[0]));
            }
        return locales;
    }

    @Override
    public void onBackPressed() {
        if (isCurrentLauncher()) hideKeyboard();
        else moveTaskToBack(false);
    }

    public void onClickClearButton(View view) {
        mSearchEditText.setText("");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        if (SDK_INT >= ICE_CREAM_SANDWICH) {
            var prefs = new SharedLauncherPrefs(this);
            SwipeLayout swipeLayout = (SwipeLayout) findViewById(R.id.swipeLayout);
            swipeLayout.setOnRefreshListener(() -> {
                if (prefs.isActionBarEnabled() && prefs.isSwipeEnabled()) {
                    showKeyboard();
                }
                swipeLayout.setRefreshing(false);
            });
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        var inflater = getMenuInflater();
        inflater.inflate(R.menu.app, menu);

        var activity = getLaunchableActivity(menuInfo);
        var item = menu.findItem(R.id.appmenu_pin_to_top);

        item.setTitle(activity.getPriority() == 0 ? R.string.appmenu_pin_to_top : R.string.appmenu_remove_pin);
    }

    /**
     * This method is called when the user is already in this activity and presses the {@code home}
     * button. Use this opportunity to return this activity back to a default state.
     *
     * @param intent The incoming {@link Intent} sent by this activity
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // If search has been typed, and home is hit, clear it.
        if (mSearchEditText.length() > 0) {
            mSearchEditText.setText(null);
        }

        closeContextMenu();
        closeOptionsMenu();

        // If the y coordinate is not at 0, let's reset it.
        var view = this.<GridView>findViewById(R.id.appsContainer);
        var loc = new int[]{0, 0};
        view.getLocationInWindow(loc);
        if (loc[1] != 0) {
            if (SDK_INT >= FROYO) view.smoothScrollToPosition(0);
            else view.setSelection(0);
        }
    }

    @Override
    public void onPackageAppeared(String activityName, int[] uids) {
        var pm = getPackageManager();
        synchronized (mLock) {
            if (mAdapter.getClassNamePosition(activityName) == -1) {
                if (SDK_INT >= N) {
                    var launcherApps = (LauncherApps) getSystemService(LAUNCHER_APPS_SERVICE);
                    for (int uid : uids) {
                        var activityList = launcherApps.getActivityList(activityName, UserHandle.getUserHandleForUid(uid));
                        var labels = getLabels(activityList, pm);
                        addToAdapter(mAdapter, activityList, labels);
                    }
                } else {
                    Collection<ResolveInfo> resolveInfos = getLaunchableResolveInfos(pm, activityName);
                    var labels = getLabels_1(resolveInfos, pm);
                    addToAdapter1(mAdapter, resolveInfos, false, labels);
                }
                mAdapter.sortApps();
                var cs = mSearchEditText.getText();
                mAdapter.getFilter().filter(cs);
            }
        }
    }

    @Override
    public void onPackageDisappeared(String activityName, int[] uids) {
        synchronized (mLock) {
            mAdapter.removeAllByName(activityName);
            var cs = mSearchEditText.getText();
            mAdapter.getFilter().filter(cs);
        }
    }

    @Override
    public void onPackageModified(String activityName, int uid) {
        synchronized (mLock) {
            onPackageDisappeared(activityName, new int[]{uid});
            onPackageAppeared(activityName, new int[]{uid});
        }
    }

    @Override
    protected void onPause() {
        if (SDK_INT >= JELLY_BEAN_MR1) {
            var manager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
            manager.unregisterDisplayListener(mDisplayListener);
        }

        getContentResolver().unregisterContentObserver(mAccSettingObserver);

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        var searchText = mSearchEditText.getText();
        if (searchText.length() > 0) showKeyboard();
        else hideKeyboard();

        if (SDK_INT >= CUPCAKE) {
            Uri accUri = Settings.System.getUriFor(ACCELEROMETER_ROTATION);
            getContentResolver().registerContentObserver(accUri, false, mAccSettingObserver);
        }
    }

    /**
     * This method checks whether rotation should be allowed and sets the launcher to
     * <p>
     * The current rules:
     * <p><ul>
     * <li> Rotate if allowed by both system and local settings.
     * <li> If rotation is not allowed by system settings disable rotation.
     * <li> If rotation is not allowed by local settings set orientation as portrait.
     * </ul><p>
     */
    private void setRotation(SharedLauncherPrefs prefs) {
        var systemRotationAllowed = Settings.System.getInt(getContentResolver(), ACCELEROMETER_ROTATION, 0) == 1;
        if (systemRotationAllowed) {
            if (prefs.isRotationAllowed()) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                if (SDK_INT >= KITKAT)
                    registerDisplayListener();
            } else {
                setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            setRequestedOrientation(SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //does this need to run in uiThread?
        if (getString(R.string.pref_key_allow_rotation).equals(key))
            setRotation(new SharedLauncherPrefs(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        // In a perfect world, this all could happen in onCreate(), but there are problems
        // with BroadcastReceiver registration and unregistration with that scenario.
        mSearchEditText = findViewById(R.id.user_search_input);
        mAdapter = loadLaunchableAdapter();

        if (SDK_INT >= JELLY_BEAN_MR1) {
            mDisplayListener = new DisplayChangeListener();
        }

        registerReceiver(mPackageChangeReceiver, PackageChangedReceiver.getFilter());
        PackageChangedReceiver.setCallback(this);

        setupPadding();
        setupPreferences();
        setupViews();
        setRotation(new SharedLauncherPrefs(this));

        setupPadding();
    }

    @Override
    protected void onStop() {
        mAdapter.onStop();
        unregisterReceiver(mPackageChangeReceiver);
        super.onStop();
    }

    public void pinToTop(MenuItem item) {
        var activity = getLaunchableActivity(item);
        activity.setPriority(activity.getPriority() == 0 ? 1 : 0);

        var prefs = new LaunchableActivityPrefs(this);
        try {
            prefs.writePreference(activity);
        } finally {
            prefs.close();
        }

        mAdapter.sortApps();
    }

    /**
     * This method registers a display listener for JB MR1 and higher to workaround a Android
     * deficiency with regard to 180 degree landscape rotation. See {@link DisplayChangeListener}
     * documentation for more information.
     */
    @RequiresApi(api = JELLY_BEAN_MR1)
    private void registerDisplayListener() {
        var displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        var handler = new Handler(Looper.getMainLooper());
        displayManager.registerDisplayListener(mDisplayListener, handler);
    }

    @RequiresApi(api = VERSION_CODES.R)
    private int getNavigationBarHeight30() {
        var navBars = WindowInsets.Type.navigationBars();
        var insets = getWindowManager().getCurrentWindowMetrics().getWindowInsets().getInsets(navBars);
        return insets.bottom;
    }

    private static void setupMasterLayoutPadding(View masterLayout, int padding) {
        masterLayout.setFitsSystemWindows(isNavBarProblematic(masterLayout.getContext()));
        var masterParams = (FrameLayout.LayoutParams) masterLayout.getLayoutParams();
        masterParams.setMargins(padding, 0, padding, 0);
    }

    private static int setupActionBarLayout(View customActionBar, int padding) {
        var context = customActionBar.getContext();
        var searchParams = (FrameLayout.LayoutParams) customActionBar.getLayoutParams();
        int searchTop = SDK_INT >= KITKAT && !isNavBarProblematic(context) ? getDimensionSize(context.getResources(), "status_bar_height") + padding : padding;

        searchParams.setMargins(0, searchTop, 0, 0);

        return searchTop + context.getResources().getDimensionPixelSize(R.dimen.app_icon_size) + padding;
    }

    private void setupPadding() {
        var appContainer = findViewById(R.id.appsContainer);
        var dp16 = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        setupMasterLayoutPadding(findViewById(R.id.masterLayout), dp16);

        final int appContainerTop;
        if (new SharedLauncherPrefs(this).isActionBarEnabled())
            appContainerTop = setupActionBarLayout(findViewById(R.id.customActionBar), dp16);
        else
            if (SDK_INT >= KITKAT && !isNavBarProblematic(this))
                appContainerTop = getDimensionSize(getResources(), "status_bar_height") + dp16;
            else
                appContainerTop = dp16;

        final int appContainerBottom;
        if (SDK_INT >= VERSION_CODES.R)
            appContainerBottom = getNavigationBarHeight30() + dp16;
        else if (SDK_INT >= KITKAT)
            appContainerBottom = getNavigationBarHeight15(getResources()) + dp16;
        else
            appContainerBottom = dp16;

        appContainer.setPadding(0, appContainerTop, 0, appContainerBottom);
    }

    private void setupPreferences() {
        var prefs = new SharedLauncherPrefs(this);
        var preferences = prefs.getPreferences();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    private EditText setupSearchEditText() {
        var listeners = new SearchEditTextListeners();
        var searchEditText = this.<EditText>findViewById(R.id.user_search_input);

        searchEditText.addTextChangedListener(listeners);
        searchEditText.setOnEditorActionListener(listeners);

        return searchEditText;
    }

    private void setupActionBar() {
        var view = findViewById(R.id.customActionBar);
        var prefs = new SharedLauncherPrefs(this);
        if (prefs.isActionBarEnabled()) view.setVisibility(VISIBLE);
        else view.setVisibility(GONE);
    }

    private void setupViews() {
        var appContainer = this.<GridView>findViewById(R.id.appsContainer);
        var listener = new AppContainerListener();
        mSearchEditText = setupSearchEditText();

        registerForContextMenu(appContainer);

        appContainer.setOnScrollListener(listener);
        appContainer.setAdapter(mAdapter);
        appContainer.setOnItemClickListener(listener);
        setupActionBar();
    }

    private class AppContainerListener implements AbsListView.OnScrollListener, OnItemClickListener {
        @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            launchActivity(view);
        }
        @Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
        @Override public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState != SCROLL_STATE_IDLE) hideKeyboard();
        }
    }

    /**
     * This class is a workaround for cases where {@link Activity} does not call any lifecycle
     * methods after 180 degree landscape orientation change.
     * <p>
     * In this case, OrientationEventListener would not be suitable due to magnitude restrictions
     * in the SensorEventListener implementation.
     */
    @RequiresApi(api = JELLY_BEAN_MR1)
    private class DisplayChangeListener implements DisplayManager.DisplayListener {
        @Override public void onDisplayAdded(int displayId) {}
        @Override public void onDisplayChanged(int displayId) {
            setupPadding();
        }
        @Override public void onDisplayRemoved(int displayId) {}
    }

    private class SearchEditTextListeners implements TextView.OnEditorActionListener, TextWatcher {
        @Override public void afterTextChanged(Editable s) {}
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            boolean actionConsumed;
            boolean enterPressed = event != null &&
                    event.getAction() == ACTION_DOWN &&
                    event.getKeyCode() == KEYCODE_ENTER;
            if (actionId == IME_ACTION_GO || (enterPressed && !mAdapter.isEmpty())) {
                if (mAdapter.getCount() > 0) {
                    launchActivity(mAdapter.getItem(0));
                    actionConsumed = true;
                } else
                    actionConsumed = false;
            } else
                actionConsumed = false;
            return actionConsumed;
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mAdapter.getFilter().filter(s);
            findViewById(R.id.clear_button).setVisibility(s.length() > 0 ? VISIBLE : GONE);
        }
    }
}
