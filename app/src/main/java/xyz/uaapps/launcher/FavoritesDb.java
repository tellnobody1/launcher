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
import android.database.sqlite.*;
import java.util.*;

public class FavoritesDb extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String ID_COLUMN = "id";
    private static final String TABLE_NAME = "favorites";

    public FavoritesDb(Context context) {
        super(context, TABLE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        var tableCreate = String.format("CREATE TABLE %s (%s TEXT PRIMARY KEY);", TABLE_NAME, ID_COLUMN);
        db.execSQL(tableCreate);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < DATABASE_VERSION && newVersion == DATABASE_VERSION) {
            db.execSQL(String.format("DROP TABLE IF EXISTS %s", TABLE_NAME));
            onCreate(db);
        }
    }

    public Set<String> getAll() {
        var res = new HashSet<String>();
        var db = getReadableDatabase();
        var columns = new String[]{ID_COLUMN};
        var cursor = db.query(TABLE_NAME, columns, null, null, null, null, null);
        try {
            var idIndex = cursor.getColumnIndex(ID_COLUMN);
            while (cursor.moveToNext()) {
                var id = cursor.getString(idIndex);
                res.add(id);
            }
        } finally {
            cursor.close();
        }
        return res;
    }

    public void save(String id, boolean favorite) {
        var db = getWritableDatabase();
        try {
            if (favorite) {
                var values = new ContentValues();
                values.put(ID_COLUMN, id);
                db.replace(TABLE_NAME, null, values);
            } else {
                db.delete(TABLE_NAME, String.format("%s = ?", ID_COLUMN), new String[]{id});
            }
        } finally {
            db.close();
        }
    }
}
