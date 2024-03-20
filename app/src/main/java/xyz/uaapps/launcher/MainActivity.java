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

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.*;
import android.content.pm.*;
import android.net.Uri;
import android.os.*;
import android.preference.PreferenceManager;
import android.text.*;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.AdapterView.*;
import android.widget.TextView.OnEditorActionListener;
import java.util.*;
import java.util.concurrent.*;
import static android.content.Intent.*;
import static android.content.pm.PackageManager.MATCH_DEFAULT_ONLY;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.CUPCAKE;
import static android.os.Build.VERSION_CODES.DONUT;
import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS;
import static android.view.KeyEvent.*;
import static android.view.View.*;
import static android.view.WindowManager.LayoutParams.*;
import static android.view.inputmethod.EditorInfo.IME_ACTION_GO;
import static java.util.Collections.emptyMap;
import static java.util.Locale.ENGLISH;

public class MainActivity extends Activity {
    private AppsAdapter mAdapter;
    private int packagesHash;
    private boolean packagesChanged = false;
    private boolean visible = false;

    private final ExecutorService exec = Executors.newSingleThreadExecutor();

    private final BroadcastReceiver packageChangeReceiver = new PackageChangedReceiver(() ->
        exec.execute(() -> {
            if (visible) if (isPackagesReallyChanged()) replaceApps();
            else packagesChanged = true;
        })
    );

    private boolean isPackagesReallyChanged() {
        return packagesChanged && calculateHash() != packagesHash;
    }

    private int calculateHash() {
        var hash = 1;
        if (SDK_INT >= N) {
            var manager = (UserManager) getSystemService(USER_SERVICE);
            var launcherApps = (LauncherApps) getSystemService(LAUNCHER_APPS_SERVICE);
            for (UserHandle userHandle : manager.getUserProfiles())
                for (var a : launcherApps.getActivityList(null, userHandle))
                    hash = 31 * hash + Objects.hash(a.getName(), manager.getSerialNumberForUser(userHandle));
        } else {
            var infoList = getLaunchableResolveInfos(getPackageManager(), null);
            for (var i : infoList)
                hash = 31 * hash + i.activityInfo.name.hashCode();
        }
        return hash;
    }

    private static AppActivity getLaunchableActivity(View view) {
        return (AppActivity) view.findViewById(R.id.appIcon).getTag();
    }

    private static AppActivity getLaunchableActivity(ContextMenuInfo menuInfo) {
        return getLaunchableActivity(((AdapterContextMenuInfo) menuInfo).targetView);
    }

    private static AppActivity getLaunchableActivity(MenuItem item) {
        return getLaunchableActivity(item.getMenuInfo());
    }

    private static Collection<ResolveInfo> getLaunchableResolveInfos(PackageManager pm, String activityName) {
        var intent = new Intent();
        intent.setAction(ACTION_MAIN);
        intent.addCategory(CATEGORY_LAUNCHER);
        if (SDK_INT >= DONUT)
            intent.setPackage(activityName);
        return pm.queryIntentActivities(intent, 0);
    }

    private static Set<String> valuesSet(Map<Locale, String> xs) {
        return new HashSet<>(xs.values());
    }

    private void showKeyboard() {
        var actionBar = findViewById(R.id.customActionBar);
        if (actionBar != null)
            if (actionBar.getVisibility() != VISIBLE)
                actionBar.setVisibility(VISIBLE);

        var searchEditText = findViewById(R.id.user_search_input);
        searchEditText.requestFocus();
        if (SDK_INT >= CUPCAKE) {
            var imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            getWindow().setSoftInputMode(SOFT_INPUT_STATE_VISIBLE);
            imm.showSoftInput(searchEditText, 0);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (ACTION_ASSIST.equals(intent.getAction()))
            showKeyboard();
    }

    private void hideKeyboard() {
        var focus = getCurrentFocus();
        if (focus != null)
            if (focus.getId() != R.id.appsContainer) {
                if (SDK_INT >= CUPCAKE) {
                    var imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
                    getWindow().setSoftInputMode(SOFT_INPUT_STATE_HIDDEN);
                }
                findViewById(R.id.appsContainer).requestFocus();
            }

        clearSearchEditText();
        var actionBar = findViewById(R.id.customActionBar);
        if (actionBar != null)
            if (actionBar.getVisibility() != GONE)
                actionBar.setVisibility(GONE);
    }

    private boolean isCurrentLauncher() {
        var pm = getPackageManager();
        var intent = new Intent(ACTION_MAIN);
        intent.addCategory(CATEGORY_HOME);
        var resolveInfo = pm.resolveActivity(intent, MATCH_DEFAULT_ONLY);
        return resolveInfo != null && getPackageName().equals(resolveInfo.activityInfo.packageName);
    }

    private void launchActivity(AppActivity appActivity) {
        hideKeyboard();
        try {
            if (appActivity instanceof RegularUserAppActivity activity) {
                if (SDK_INT >= LOLLIPOP) {
                    var userManager = (UserManager) getSystemService(USER_SERVICE);
                    var launcher = (LauncherApps) getSystemService(LAUNCHER_APPS_SERVICE);
                    var userSerial = activity.getUserSerial();
                    var userHandle = userManager.getUserForSerialNumber(userSerial);
                    launcher.startMainActivity(activity.getComponent(), userHandle, null, Bundle.EMPTY);
                }
            } else if (appActivity instanceof IntentAppActivity activity) {
                startActivity(activity.getLaunchIntent());
            }
        } catch (Exception ignored) {
            Toast.makeText(this, getString(R.string.activity_not_found), Toast.LENGTH_SHORT).show();
        }
    }

    private void launchActivity(View view) {
        launchActivity(getLaunchableActivity(view));
    }

    private AppsAdapter loadLaunchableAdapter() {
        var adapter = new AppsAdapter(this, R.layout.app_grid_item);
        adapter.addAll(apps());
        adapter.sortApps();
        adapter.notifyDataSetChanged();
        return adapter;
    }

    private List<RegularAppActivity> apps() {
        var acc = new LinkedList<RegularAppActivity>();
        var pm = getPackageManager();
        var thisCanonicalName = getClass().getCanonicalName();
        if (SDK_INT >= N) {
            var manager = (UserManager) getSystemService(USER_SERVICE);
            var launcherApps = (LauncherApps) getSystemService(LAUNCHER_APPS_SERVICE);
            for (var userHandle : manager.getUserProfiles()) {
                var activityList = launcherApps.getActivityList(null, userHandle);
                var labels = getLabels(activityList, pm);
                for (var info : activityList)
                    if (thisCanonicalName == null || !thisCanonicalName.startsWith(info.getName())) {
                        var activityLabels = labels.getOrDefault(info, emptyMap());
                        acc.add(new RegularUserAppActivityImpl(info, manager, valuesSet(activityLabels), activityLabels.getOrDefault(ENGLISH, null)));
                    }
            }
        } else {
            var infoList = getLaunchableResolveInfos(pm, null);
            var labels = getLabels_1(infoList, pm);
            var prefs = getPreferences(MODE_PRIVATE);
            for (var info : infoList)
                if (thisCanonicalName == null || !thisCanonicalName.startsWith(info.activityInfo.packageName)) {
                    var activityLabels = labels.get(info);
                    var activityLabels2 = activityLabels == null ? Collections.<Locale, String>emptyMap() : activityLabels;
                    var labelEn = activityLabels2.containsKey(ENGLISH) ? activityLabels2.get(ENGLISH) : null;
                    acc.add(new RegularIntentAppActivityImpl(info, prefs, getPackageManager(), valuesSet(activityLabels2), labelEn));
                }
        }
        return acc;
    }

    //todo inline
    @TargetApi(N)
    private Map<LauncherActivityInfo, Map<Locale, String>> getLabels(List<LauncherActivityInfo> activityList, PackageManager pm) {
        var labels = new HashMap<LauncherActivityInfo, Map<Locale, String>>();
        var locales = AppLocales.getLabelLocales(getResources().getConfiguration());
        for (var activityInfo : activityList) {
            var ops = new LauncherActivityInfoOps(activityInfo, pm);
            labels.put(activityInfo, ops.getLabels(locales));
        }
        return labels;
    }

    //todo inline
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
        clearSearchEditText();
    }

    public void onClickSearch(View view) {
        showKeyboard();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        var prefs = new SharedAppPrefs(this);

        packagesHash = calculateHash();

        if (SDK_INT >= ICE_CREAM_SANDWICH)
            SwipeOps.init(new SwipeOps.F() {
                public void onRefresh() { showKeyboard(); }
                public SwipeLayout view(int id) { return findViewById(id); }
            });

        if (SDK_INT >= TIRAMISU)
            registerReceiver(packageChangeReceiver, PackageChangedReceiver.getFilter(), Context.RECEIVER_NOT_EXPORTED);
        else
            registerReceiver(packageChangeReceiver, PackageChangedReceiver.getFilter());

        if (prefs.isShowSearchButton())
            findViewById(R.id.search_button).setVisibility(VISIBLE);

        findViewById(R.id.clear_button).setOnClickListener(v -> onClickClearButton(null));
        findViewById(R.id.search_button).setOnClickListener(v -> onClickSearch(null));

        mAdapter = loadLaunchableAdapter();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setupAppContainer();
        setupSearchEditText();
    }

    @Override
    protected void onPause() {
        visible = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        visible = true;
        exec.execute(() -> {
            if (packagesChanged && isPackagesReallyChanged()) replaceApps();
            else runOnUiThread(this::hideKeyboard);
        });
    }

    private void replaceApps() {
        mAdapter.replaceAll(apps());
        mAdapter.sortApps();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        exec.shutdownNow();
        mAdapter.onStop();
        unregisterReceiver(packageChangeReceiver);
        super.onDestroy();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getMenuInflater().inflate(R.menu.app, menu);

        var activity = getLaunchableActivity(menuInfo);

        var pinItem = menu.findItem(R.id.appmenu_pin);
        if (activity instanceof RegularAppActivity a)
            pinItem.setTitle(a.isFavorite() ? R.string.appmenu_remove_pin : R.string.appmenu_pin_to_top);
        pinItem.setEnabled(activity instanceof RegularAppActivity);

        var appInfoItem = menu.findItem(R.id.appmenu_app_info);
        appInfoItem.setEnabled(activity instanceof RegularAppActivity);
    }

    private void clearSearchEditText() {
        var input = this.<EditText>findViewById(R.id.user_search_input);
        if (input != null)
            if (!input.getText().toString().equals(""))
                input.setText("");
    }

    public void pinToTop(MenuItem item) {
        var activity = getLaunchableActivity(item);
        if (activity instanceof RegularAppActivity a) {
            a.setFavorite(!a.isFavorite());

            var prefs = new AppActivityPrefs(this);
            try {
                prefs.saveFavorite(a);
            } finally {
                prefs.close();
            }

            mAdapter.sortApps();
            mAdapter.notifyDataSetChanged();
        }
    }

    public void launchApplicationDetails(MenuItem item) {
        var activity = getLaunchableActivity(item);
        if (activity instanceof RegularAppActivity regular) {
            var intent = new Intent(ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + regular.getComponent().getPackageName()));
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private void setupSearchEditText() {
        var searchEditText = this.<EditText>findViewById(R.id.user_search_input);
        searchEditText.addTextChangedListener(new SearchEditTextWatcher());
        if (SDK_INT >= CUPCAKE)
            searchEditText.setOnEditorActionListener(new SearchEditTextEditorActionListener());
    }

    private void setupAppContainer() {
        var appContainer = this.<GridView>findViewById(R.id.appsContainer);
        appContainer.setAdapter(mAdapter);

        registerForContextMenu(appContainer);

        var listener = new AppContainerListener();
        appContainer.setOnScrollListener(listener);
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

    private class SearchEditTextWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {}
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mAdapter.getFilter().filter(s);
            findViewById(R.id.clear_button).setVisibility(s.length() > 0 ? VISIBLE : GONE);
        }
    }

    @TargetApi(CUPCAKE)
    private class SearchEditTextEditorActionListener implements OnEditorActionListener {
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            var enterPressed = event != null && event.getAction() == ACTION_DOWN && event.getKeyCode() == KEYCODE_ENTER;
            if (actionId == IME_ACTION_GO || enterPressed)
                if (mAdapter.getCount() > 0) {
                    launchActivity(mAdapter.getItem(0));
                    return true;
                }
            return false;
        }
    }
}
