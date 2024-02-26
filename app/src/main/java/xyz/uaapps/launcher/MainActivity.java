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
package xyz.uaapps.launcher;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.CUPCAKE;
import static android.os.Build.VERSION_CODES.DONUT;
import static android.os.Build.VERSION_CODES.FROYO;
import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.TIRAMISU;
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
import static java.util.Locale.ENGLISH;
import static xyz.uaapps.launcher.BuildConfig.DEBUG;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final BroadcastReceiver packageChangeReceiver = new PackageChangedReceiver();

    /**
     * This ContentObserver is used by the ContentResolver to register a callback to set rotation in case it changes in the system settings.
     */
    private final ContentObserver mAccSettingObserver = new ContentObserver(new Handler()) {
        @Override public void onChange(boolean selfChange) {
            setRotation(new SharedLauncherPrefs(MainActivity.this));
        }
    };

    /**
     * An adapter, based off {@link android.widget.ArrayAdapter}, to handle {@link LaunchableActivity} items.
     */
    private LaunchableAdapter mAdapter;

    private EditText mSearchEditText;

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

    /**
     * This method adds LauncherActivityInfo objects to an adapter in API 24+.
     *
     * @param adapter  The adapter to add to.
     * @param infoList The objects to add to the adapter.
     */
    @TargetApi(N)
    private void addToAdapter(
            @NonNull LaunchableAdapter adapter,
            @NonNull Iterable<LauncherActivityInfo> infoList,
            Map<LauncherActivityInfo, Map<Locale, String>> labels) {
        var thisCanonicalName = getClass().getCanonicalName();
        var manager = (UserManager) getSystemService(USER_SERVICE);
        for (var info : infoList)
            if (thisCanonicalName == null || !thisCanonicalName.startsWith(info.getName())) {
                Map<Locale, String> activityLabels = labels.getOrDefault(info, emptyMap());
                adapter.add(new RegularUserLaunchableActivityImpl(info, manager, valuesSet(activityLabels), activityLabels.getOrDefault(ENGLISH, null)));
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
            @NonNull LaunchableAdapter adapter,
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
                String labelEn = activityLabels2.containsKey(ENGLISH) ? activityLabels2.get(ENGLISH) : null;
                adapter.add(new RegularIntentLaunchableActivityImpl(info, prefs, manager, valuesSet(activityLabels2), labelEn));
            }
        }
    }

    private static Set<String> valuesSet(Map<Locale, String> xs) {
        return new HashSet<>(xs.values());
    }

    private void showKeyboard() {
        findViewById(R.id.customActionBar).setVisibility(VISIBLE);
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
            findViewById(R.id.customActionBar).setVisibility(GONE);
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
        if (launchableActivity instanceof RegularUserLaunchableActivity activity) {
            var userManager = (UserManager) getSystemService(USER_SERVICE);
            var launcher = (LauncherApps) getSystemService(LAUNCHER_APPS_SERVICE);
            var userSerial = activity.getUserSerial();
            var userHandle = userManager.getUserForSerialNumber(userSerial);
            launcher.startMainActivity(activity.getComponent(), userHandle, null, Bundle.EMPTY);
        } else if (launchableActivity instanceof IntentLaunchableActivity activity) {
            try {
                startActivity(activity.getLaunchIntent());
                mSearchEditText.setText(null);
                mAdapter.sortApps();
            } catch (ActivityNotFoundException e) {
                if (DEBUG) throw e;
                else Toast.makeText(this, getString(R.string.activity_not_found), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.activity_not_found), Toast.LENGTH_SHORT).show();
        }
    }

    private void launchActivity(View view) {
        launchActivity(getLaunchableActivity(view));
    }

    private LaunchableAdapter loadLaunchableAdapter() {
        LaunchableAdapter adapter;
        var pm = getPackageManager();
        if (SDK_INT >= N) {
            var manager = (UserManager) getSystemService(USER_SERVICE);
            var launcherApps = (LauncherApps) getSystemService(LAUNCHER_APPS_SERVICE);
            var iter = manager.getUserProfiles().listIterator();
            int count = 0;

            while (iter.hasNext()) {
                count += launcherApps.getActivityList(null, iter.next()).size();
            }

            adapter = new LaunchableAdapter(this, R.layout.app_grid_item, count);

            while (iter.hasPrevious()) {
                var activityList = launcherApps.getActivityList(null, iter.previous());
                var labels = getLabels(activityList, pm);
                addToAdapter(adapter, activityList, labels);
            }
        } else {
            var infoList = getLaunchableResolveInfos(pm, null);
            adapter = new LaunchableAdapter(this, R.layout.app_grid_item, infoList.size());
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
        var locales = AppLocales.getLabelLocales(getResources().getConfiguration());
        for (var activityInfo : activityList) {
            var ops = new LauncherActivityInfoOps(activityInfo);
            labels.put(activityInfo, ops.getLabels(locales, pm));
        }
        return labels;
    }

    private Map<ResolveInfo, Map<Locale, String>> getLabels_1(Collection<ResolveInfo> infoList, PackageManager pm) {
        var labels = new HashMap<ResolveInfo, Map<Locale, String>>();
        var locales = AppLocales.getLabelLocales(getResources().getConfiguration());
        for (var resolveInfo : infoList) {
            var ops = new ResolveInfoOps(resolveInfo, pm);
            labels.put(resolveInfo, ops.getLabels(locales));
        }
        return labels;
    }

    @Override
    public void onBackPressed() {
        if (isCurrentLauncher()) hideKeyboard();
        else moveTaskToBack(false);
    }

    public void onClickClearButton(View view) {
        mSearchEditText.setText(null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        if (SDK_INT >= ICE_CREAM_SANDWICH) {
            var prefs = new SharedLauncherPrefs(this);
            SwipeLayout swipeLayout = (SwipeLayout) findViewById(R.id.swipeLayout);
            swipeLayout.setOnRefreshListener(() -> {
                if (prefs.isSwipeEnabled()) {
                    showKeyboard();
                }
                swipeLayout.setRefreshing(false);
            });
        }

        if (SDK_INT >= TIRAMISU) {
            registerReceiver(packageChangeReceiver, PackageChangedReceiver.getFilter(), Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(packageChangeReceiver, PackageChangedReceiver.getFilter());
        }

        if (SDK_INT < DONUT) {
            this.<ImageButton>findViewById(R.id.clear_button).setOnClickListener(v -> {
                onClickClearButton(null);
            });
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(packageChangeReceiver);
        super.onDestroy();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        var inflater = getMenuInflater();
        inflater.inflate(R.menu.app, menu);

        var activity = getLaunchableActivity(menuInfo);

        var pinItem = menu.findItem(R.id.appmenu_pin);
        if (activity instanceof RegularLaunchableActivity a)
            pinItem.setTitle(a.isFavorite() ? R.string.appmenu_remove_pin : R.string.appmenu_pin_to_top);
        pinItem.setEnabled(activity instanceof RegularLaunchableActivity);

        var appInfoItem = menu.findItem(R.id.appmenu_app_info);
        appInfoItem.setEnabled(activity instanceof RegularLaunchableActivity);
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
        mSearchEditText.setText(null);

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
    protected void onPause() {
        getContentResolver().unregisterContentObserver(mAccSettingObserver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSearchEditText.setText(null);

        if (SDK_INT >= CUPCAKE) {
            var accUri = Settings.System.getUriFor(ACCELEROMETER_ROTATION);
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
        if (systemRotationAllowed)
            if (prefs.isRotationAllowed())
                setRequestedOrientation(SCREEN_ORIENTATION_SENSOR);
            else
                setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
        else
            setRequestedOrientation(SCREEN_ORIENTATION_UNSPECIFIED);
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

        setupPreferences();
        setupViews();
        setRotation(new SharedLauncherPrefs(this));
    }

    @Override
    protected void onStop() {
        mAdapter.onStop();
        super.onStop();
    }

    public void pinToTop(MenuItem item) {
        var activity = getLaunchableActivity(item);
        if (activity instanceof RegularLaunchableActivity a) {
            a.setFavorite(!a.isFavorite());

            var prefs = new RegularLaunchableActivityPrefs(this);
            try {
                prefs.saveFavorite(a);
            } finally {
                prefs.close();
            }

            mAdapter.sortApps();
        }
    }

    public void launchApplicationDetails(MenuItem item) {
        var activity = getLaunchableActivity(item);
        if (activity instanceof RegularLaunchableActivity regular) {
            var intent = new Intent(ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + regular.getComponent().getPackageName()));
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private void setupPreferences() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        var prefs = new SharedLauncherPrefs(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    private EditText setupSearchEditText() {
        var listeners = new SearchEditTextListeners();
        var searchEditText = this.<EditText>findViewById(R.id.user_search_input);

        searchEditText.addTextChangedListener(listeners);
        searchEditText.setOnEditorActionListener(listeners);

        return searchEditText;
    }

    private void setupViews() {
        var appContainer = this.<GridView>findViewById(R.id.appsContainer);
        var listener = new AppContainerListener();
        mSearchEditText = setupSearchEditText();

        registerForContextMenu(appContainer);

        appContainer.setOnScrollListener(listener);
        appContainer.setAdapter(mAdapter);
        appContainer.setOnItemClickListener(listener);
    }

    private class AppContainerListener implements AbsListView.OnScrollListener, OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            launchActivity(view);
        }
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState != SCROLL_STATE_IDLE)
                hideKeyboard();
        }
    }

    private class SearchEditTextListeners implements TextView.OnEditorActionListener, TextWatcher {
        public void afterTextChanged(Editable s) {}
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
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
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mAdapter.getFilter().filter(s);
            findViewById(R.id.clear_button).setVisibility(s.length() > 0 ? VISIBLE : GONE);
        }
    }
}
