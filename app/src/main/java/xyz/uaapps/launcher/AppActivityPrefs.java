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

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This is a convenience class write persistent information to save to restore {@link RegularAppActivity} objects.
 */
public class AppActivityPrefs extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String KEY_CLASSNAME = "ClassName";
    private static final String KEY_FAVORITE = "Favorite";
    private static final String KEY_ID = "Id";
    private static final String TABLE_NAME = "Favorites";

    public AppActivityPrefs(Context context) {
        super(context, TABLE_NAME, null, DATABASE_VERSION);
    }

    private static void deletePreference(SQLiteDatabase db, String className) {
        db.delete(TABLE_NAME, String.format("%s=?", KEY_CLASSNAME), new String[]{className});
    }

    public void onCreate(SQLiteDatabase db) {
        var tableCreate = String.format(
            "CREATE TABLE %s (%S INTEGER PRIMARY KEY, %s TEXT UNIQUE, %s INTEGER);",
            TABLE_NAME, KEY_ID, KEY_CLASSNAME, KEY_FAVORITE);
        db.execSQL(tableCreate);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < DATABASE_VERSION && newVersion == DATABASE_VERSION) {
            db.execSQL(String.format("DROP TABLE IF EXISTS %s", TABLE_NAME));
            onCreate(db);
        }
    }

    public void restoreFavorite(RegularAppActivity activity) {
        var db = getReadableDatabase();
        var columns = new String[]{KEY_FAVORITE};

        var whereArgs = new String[]{activity.getName()};

        var cursor = db.query(TABLE_NAME, columns, KEY_CLASSNAME + "=?", whereArgs, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                var column = cursor.getColumnIndex(KEY_FAVORITE);
                if (column != -1)
                    activity.setFavorite(cursor.getInt(column) == 1);
            }
        } finally {
            cursor.close();
        }
    }

    public void saveFavorite(RegularAppActivity activity) {
        var favorite = activity.isFavorite();
        var className = activity.getName();

        var db = getWritableDatabase();
        try {
            if (favorite) {
                var values = new ContentValues();
                values.put(KEY_FAVORITE, 1);
                values.put(KEY_CLASSNAME, className);
                db.replace(TABLE_NAME, null, values);
            } else {
                deletePreference(db, className);
            }
        } finally {
            db.close();
        }
    }
}
