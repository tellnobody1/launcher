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
 * This is a convenience class write persistent information to save to restore
 * {@link LaunchableActivity} objects.
 */
public class LaunchableActivityPrefs extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String KEY_CLASSNAME = "ClassName";

    private static final String KEY_FAVORITE = "Favorite";

    private static final String KEY_ID = "Id";

    private static final String TABLE_NAME = "ActivityLaunchNumbers";

    public LaunchableActivityPrefs(Context context) {
        super(context, TABLE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This method deletes a column based on the classname.
     *
     * @param db        The database.
     * @param className The classname of the column to delete.
     */
    private static void deletePreference(SQLiteDatabase db, String className) {
        db.delete(TABLE_NAME, KEY_CLASSNAME + "=?", new String[]{className});
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        var tableCreate = String.format(
                "CREATE TABLE %s (%S INTEGER PRIMARY KEY, %s TEXT UNIQUE, %s INTEGER);",
                TABLE_NAME, KEY_ID, KEY_CLASSNAME, KEY_FAVORITE);
        db.execSQL(tableCreate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < DATABASE_VERSION && newVersion == DATABASE_VERSION) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    /**
     * This method updates a {@link LaunchableActivity} with persistent information.
     *
     * @param launchableActivity The {@link LaunchableActivity} to update.
     */
    public void setPreferences(final LaunchableActivity launchableActivity) {
        var db = getReadableDatabase();
        var name = launchableActivity.getComponent();
        var columns = new String[]{KEY_FAVORITE};

        var whereArgs = name == null ? new String[]{launchableActivity.toString()} : new String[]{name.getClassName()};

        var cursor = db.query(TABLE_NAME, columns, KEY_CLASSNAME + "=?", whereArgs, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                var column = cursor.getColumnIndex(KEY_FAVORITE);
                if (column != -1)
                    launchableActivity.setPriority(cursor.getInt(column));
            }
        } finally {
            cursor.close();
        }
    }

    /**
     * Write the preferences from the {@link LaunchableActivity} to persistent storage.
     *
     * @param launchableActivity The {@link LaunchableActivity} to write to persistent storage.
     */
    public void writePreference(final LaunchableActivity launchableActivity) {
        var values = new ContentValues();
        var priority = launchableActivity.getPriority();
        var name = launchableActivity.getComponent();

        if (priority > 0) {
            values.put(KEY_FAVORITE, priority);
        }

        var className = name == null ? launchableActivity.toString() : name.getClassName();

        var db = getWritableDatabase();
        try {
            if (values.size() == 0) {
                deletePreference(db, className);
            } else {
                values.put(KEY_CLASSNAME, className);
                db.replace(TABLE_NAME, null, values);
            }
        } finally {
            db.close();
        }
    }
}
