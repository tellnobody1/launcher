package github.tellnobody1.launcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import java.text.Collator;
import java.util.*;

public class MainActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        var listView = this.<ListView>findViewById(R.id.listView);

        var appList = getInstalledApps();

        var appNames = new ArrayList<String>();
        for (var appInfo : appList) appNames.add(appInfo.appName);

        var adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, appNames);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            var appInfo = appList.get(position);
            var launchIntent = launch(appInfo.packageName);
            if (launchIntent != null) startActivity(launchIntent);
            else Toast.makeText(MainActivity.this, "Cannot launch " + appInfo.appName, Toast.LENGTH_SHORT).show();
        });
    }

    List<AppInfo> getInstalledApps() {
        var res = new ArrayList<AppInfo>();
        var pm = getPackageManager();
        var packs = pm.getInstalledPackages(0);
        for (var pack : packs)
            if (launch(pack.packageName) != null) {
                var appName = pack.applicationInfo.loadLabel(pm).toString();
                var packageName = pack.packageName;
                res.add(new AppInfo(appName, packageName));
            }
        { /* sort with default locale */
            var collator = Collator.getInstance();
            collator.setStrength(Collator.PRIMARY);
            Collections.sort(res, (o1, o2) -> collator.compare(o1.appName(), o2.appName()));
        }
        return res;
    }

    Intent launch(String packageName) {
        try {
            var pm = getPackageManager();
            var method = pm.getClass().getMethod("getLaunchIntentForPackage", String.class);
            return (Intent) method.invoke(pm, packageName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    record AppInfo(String appName, String packageName) {}
}
