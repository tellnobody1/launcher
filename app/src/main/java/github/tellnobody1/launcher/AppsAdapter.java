/*
 * Copyright 2015-2017 Hayai Software
 * Copyright 2018-2022 The KeikaiLauncher Project
 * Copyright 2024 tellnobody1
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
package github.tellnobody1.launcher;

import android.content.Context;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*;
import java.util.regex.Pattern;
import github.tellnobody1.launcher.AppActivity.RegularAppActivity;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.GINGERBREAD;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

public class AppsAdapter extends BaseAdapter implements Filterable {

    private static final Pattern DIACRITICAL_MARKS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    /**
     * The {@link Filter} used by this list {@code Adapter}.
     */
    private final AppsFilter mFilter = new AppsFilter();

    /**
     * Lock used to modify the content of {@link #mObjects}. Any write operation
     * performed on the array should be synchronized on this lock. This lock is also
     * used by the filter (see {@link #getFilter()} to make a synchronized copy of
     * the original array of data.
     */
    private final Object mLock = new Object();

    /**
     * Contains the list of objects that represent the data of this ArrayAdapter.
     * The content of this list is referred to as "the array" in the documentation.
     */
    private final List<AppActivity> mObjects;

    /**
     * This field contains the database used to store persistent values for
     * {@link AppActivity} objects.
     */
    private final FavoritesDb favoritesDb;

    private final Context context;

    /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter in a drop down widget.
     */
    private final int mDropDownResource;

    // A copy of the original mObjects array, initialized from and then used instead as soon as
    // the mFilter ArrayFilter is used. mObjects will then only contain the filtered values.
    private List<RegularAppActivity> mOriginalValues;

    /**
     * @param resource The resource ID for a layout file containing a TextView to use when instantiating views.
     */
    public AppsAdapter(Context context, int resource) {
        this.context = context;
        mDropDownResource = resource;
        mObjects = Collections.synchronizedList(new LinkedList<>());
        favoritesDb = new FavoritesDb(context);
    }

    public void addAll(List<RegularAppActivity> apps) {
        var favorites = favoritesDb.getAll();
        for (var app : apps) if (favorites.contains(app.getId())) app.setFavorite(true);
        synchronized (mLock) {
            if (mOriginalValues == null) mObjects.addAll(apps);
            else mOriginalValues.addAll(apps);
        }
    }

    public void replaceAll(List<RegularAppActivity> apps) {
        synchronized (mLock) {
            mOriginalValues = null;
            mObjects.clear();
            addAll(apps);
        }
    }

    @Override
    public int getCount() {
        return mObjects.size();
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        final TextView text;

        if (convertView == null) {
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            text = (TextView) inflater.inflate(mDropDownResource, parent, false);
        } else {
            text = (TextView) convertView;
        }

        text.setText(getItem(position).toString());

        return text;
    }

    public Filter getFilter() {
        return mFilter;
    }

    /**
     * This method returns the {@link AppActivity} found at the {@code position} in this
     * adapter.
     * <p>
     * The position is not altered by the current {@link Filter} in use.
     *
     * @param position The index of the {@code LaunchableActivity} to return.
     * @return The {@code LaunchableActivity} in the position, null otherwise.
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    @Override
    public AppActivity getItem(int position) {
        return mObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * The {@link View} used as a grid item for the LaunchableAdapter {@code GridView}.
     *
     * @param position    The position to of the {@link AppActivity} to return a {@code View} for.
     * @param convertView The old {@code View} to reuse, if possible.
     * @param parent      The parent {@code View}.
     * @return The {@code View} to use in the {@code GridView}.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view;

        if (convertView == null) {
            var inflater = LayoutInflater.from(parent.getContext());
            view = inflater.inflate(R.layout.app_grid_item, parent, false);
        } else {
            view = convertView;
        }

        view.setVisibility(View.VISIBLE);
        AppActivity appActivity;
        synchronized (mLock) {
            appActivity = getItem(position);
        }
        var label = appActivity.getActivityLabel();
        var appLabelView = view.<TextView>findViewById(R.id.appLabel);
        var appIconView = view.<AppIconView>findViewById(R.id.appIcon);

        appLabelView.setText(label);

        appIconView.setTag(appActivity);
        appIconView.set(label, appActivity.getIconKey());

        return view;
    }

    /**
     * This method should be called before the parent context is destroyed.
     */
    public void onStop() {
        favoritesDb.close();
    }

    private void sort(Comparator<AppActivity> comparator1, Comparator<RegularAppActivity> comparator2) {
        synchronized (mLock) {
            Collections.sort(mObjects, comparator1);
            if (mOriginalValues != null)
                Collections.sort(mOriginalValues, comparator2);
        }
    }

    public void sortApps() {
        synchronized (mLock) {
            var locale = AppLocales.getDefault(context);
            final Collator collator = Collator.getInstance(locale);
            collator.setStrength(Collator.PRIMARY);

            Comparator<AppActivity> comparator1 = (o1, o2) -> collator.compare(o1.getActivityLabel(), o2.getActivityLabel());
            Comparator<RegularAppActivity> comparator2 = (o1, o2) -> collator.compare(o1.getActivityLabel(), o2.getActivityLabel());
            sort(comparator1, comparator2);

            Comparator<AppActivity> comparator3 = (o1, o2) -> {
                var p1 = o1 instanceof RegularAppActivity a ? (a.isFavorite() ? 1 : 0) : 0;
                var p2 = o2 instanceof RegularAppActivity a ? (a.isFavorite() ? 1 : 0) : 0;
                return p2 - p1;
            };
            Comparator<RegularAppActivity> comparator4 = (o1, o2) -> (o2.isFavorite() ? 1 : 0) - (o1.isFavorite() ? 1 : 0);
            sort(comparator3, comparator4);
        }
    }

    @Override
    public String toString() {
        return mOriginalValues == null ? mObjects.toString() : mOriginalValues.toString();
    }

    /**
     * An array filter constrains the content of the array adapter with
     * a prefix. Each item that does not start with the supplied prefix
     * is removed from the list.
     */
    private final class AppsFilter extends Filter {
        @Override protected FilterResults performFiltering(CharSequence constraint) {
            List<RegularAppActivity> values;
            var results = new FilterResults();

            // Don't act upon a blank constraint if the filter hasn't been used yet.
            if (mOriginalValues == null && constraint.length() == 0) {
                results.values = mObjects;
                results.count = mObjects.size();
            } else {
                if (mOriginalValues == null) {
                    synchronized (mLock) {
                        mOriginalValues = new LinkedList<>();
                        for (var x : mObjects)
                            if (x instanceof RegularAppActivity regular)
                                mOriginalValues.add(regular);
                    }
                }

                synchronized (mLock) {
                    values = new ArrayList<>(mOriginalValues);
                }
                final int count = values.size();
                if (constraint == null || constraint.length() == 0) {
                    results.values = values;
                    results.count = count;
                } else {
                    var query = stripAccents(constraint).toLowerCase();
                    var newValues = new LinkedList<AppActivity>();

                    var phoneMatcher = Pattern.compile("^\\+?[\\d\\s\\-()]+$").matcher(query);
                    if (phoneMatcher.find() && SDK_INT >= LOLLIPOP) {
                        newValues.add(new DialIntentAppActivity(phoneMatcher.group(), context.getResources().getString(R.string.dial)));
                    }

                    var allTargets = new LinkedHashMap<AppActivity, Set<String>>();
                    for (var value : values)
                        allTargets.put(value, value.getLabels());
                    newValues.addAll(QueryVariants.checkAll(query, allTargets));

                    results.values = newValues;
                    results.count = newValues.size();
                }
            }

            return results;
        }

        @Override protected void publishResults(CharSequence constraint, FilterResults results) {
            if (mObjects != results.values) {
                mObjects.clear();
                mObjects.addAll((List<AppActivity>) results.values);

                if (results.count > 0) notifyDataSetChanged();
                else notifyDataSetInvalidated();
            }
        }

        private String stripAccents(CharSequence cs) {
            return SDK_INT >= GINGERBREAD ? DIACRITICAL_MARKS.matcher(Normalizer.normalize(cs, Normalizer.Form.NFKD)).replaceAll("") : cs.toString();
        }
    }
}
