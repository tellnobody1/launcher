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

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.CUPCAKE;
import static android.os.Build.VERSION_CODES.FROYO;
import static android.os.Build.VERSION_CODES.N;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class PackageChangedReceiver extends BroadcastReceiver {
    private final F f;

    interface F { void f(); }

    public PackageChangedReceiver(F f) {
        this.f = f;
    }

    public static IntentFilter getFilter() {
        var filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        if (SDK_INT >= CUPCAKE) {
            filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        }
        if (SDK_INT >= FROYO) {
            filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
            filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        }
        if (SDK_INT >= N) {
            filter.addAction(Intent.ACTION_PACKAGES_SUSPENDED);
            filter.addAction(Intent.ACTION_PACKAGES_UNSUSPENDED);
        }
        filter.addDataScheme("package");
        return filter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null)
            f.f();
    }
}
