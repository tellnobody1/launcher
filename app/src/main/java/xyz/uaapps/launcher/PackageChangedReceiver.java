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

import android.content.*;
import static android.content.Intent.*;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.*;

public class PackageChangedReceiver extends BroadcastReceiver {
    private final F f;

    interface F { void f(); }

    public PackageChangedReceiver(F f) {
        this.f = f;
    }

    public static IntentFilter getFilter() {
        var filter = new IntentFilter();
        filter.addAction(ACTION_PACKAGE_ADDED);
        filter.addAction(ACTION_PACKAGE_CHANGED);
        filter.addAction(ACTION_PACKAGE_REMOVED);
        if (SDK_INT >= CUPCAKE) {
            filter.addAction(ACTION_PACKAGE_REPLACED);
        }
        if (SDK_INT >= FROYO) {
            filter.addAction(ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
            filter.addAction(ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        }
        if (SDK_INT >= N) {
            filter.addAction(ACTION_PACKAGES_SUSPENDED);
            filter.addAction(ACTION_PACKAGES_UNSUSPENDED);
        }
        filter.addDataScheme("package");
        return filter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        var action = intent.getAction();
        if (action != null) switch (action) {
            case ACTION_PACKAGE_ADDED, ACTION_PACKAGE_REMOVED -> {
                var data = intent.getData();
                if (data != null) f.f();
            }
            case ACTION_EXTERNAL_APPLICATIONS_AVAILABLE, ACTION_PACKAGES_UNSUSPENDED, ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE, ACTION_PACKAGES_SUSPENDED, ACTION_PACKAGE_CHANGED, ACTION_PACKAGE_REPLACED -> {
                if (SDK_INT >= FROYO) {
                    var xs = intent.getStringArrayExtra(EXTRA_CHANGED_PACKAGE_LIST);
                    if (xs != null && xs.length != 0) f.f();
                }
            }
            default -> {}
        }
    }
}
