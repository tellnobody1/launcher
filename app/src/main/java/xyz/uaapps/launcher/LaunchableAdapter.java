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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.Collator;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * This class is an adapter for LaunchableActivities, originally inspired by the ArrayAdapter
 * class.
 */
public class LaunchableAdapter<T extends LaunchableActivity> extends BaseAdapter
        implements Filterable {

    private static final Pattern DIACRITICAL_MARKS =
            Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private static final String TAG = "LaunchableAdapter";

    /**
     * The {@link Filter} used by this list {@code Adapter}.
     */
    private final LaunchableFilter mFilter = new LaunchableFilter();

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
    private final List<T> mObjects;

    /**
     * This field contains the database used to store persistent values for
     * {@link LaunchableActivity} objects.
     */
    private final LaunchableActivityPrefs mPrefs;

    /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter in a drop down widget.
     */
    private int mDropDownResource;

    /**
     * Indicates whether or not {@link #notifyDataSetChanged()} must be called whenever
     * {@link #mObjects} is modified.
     */
    private boolean mNotifyOnChange = false;

    // A copy of the original mObjects array, initialized from and then used instead as soon as
    // the mFilter ArrayFilter is used. mObjects will then only contain the filtered values.
    private List<T> mOriginalValues;

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     */
    public LaunchableAdapter(@NonNull final Context context, @LayoutRes final int resource,
                             final int initialSize) {
        mDropDownResource = resource;
        mObjects = Collections.synchronizedList(new ArrayList<>(initialSize));
        mPrefs = new LaunchableActivityPrefs(context);
    }

    /**
     * This constructor is for reloading this Adapter using the {@link #export()} method.
     *
     * @param object   The Object from the {@code export()} method.
     * @param context  The current activity.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     */
    @SuppressWarnings("unchecked")
    public LaunchableAdapter(final Object object, @NonNull final Context context,
                             @LayoutRes final int resource) {
        this(context, resource, ((List<? extends T>[]) object)[0].size());

        final List<? extends T>[] lists = (List<? extends T>[]) object;
        mObjects.addAll(lists[0]);

        if (lists[1] != null) {
            mOriginalValues = (List<T>) lists[1];
        }
    }

    /**
     * Adds the specified object at the end of the array.
     *
     * @param object The object to add at the end of the array.
     */
    public void add(@Nullable final T object) {
        mPrefs.setPreferences(object);

        synchronized (mLock) {
            if (mOriginalValues == null) {
                mObjects.add(object);
            } else {
                mOriginalValues.add(object);
            }
        }
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
    }

    /**
     * The Object from this method is for use with
     * {@link Activity#onRetainNonConfigurationInstance()} and
     * {@link LaunchableAdapter#LaunchableAdapter(Object, Context, int)}.
     *
     * @return An object used to restore the state of this Adapter.
     */
    public Object export() {
        return new List<?>[]{mObjects, mOriginalValues};
    }

    /**
     * Returns the position of a {@link LaunchableActivity} where the
     * {@link LaunchableActivity#getComponent()}.{@link ComponentName#getClassName()} is equal to
     * the {@code className} parameter.
     *
     * @param className The classname to find.
     * @return The LaunchableActivity matching the classname parameter, {@code -1} if not found.
     */
    public int getClassNamePosition(@NonNull final String className) {
        final List<T> current;
        int position = -1;

        if (mOriginalValues == null) {
            current = mObjects;
        } else {
            current = mOriginalValues;
        }

        final int currentSize = current.size();
        for (int i = 0; i < currentSize && position == -1; i++) {
            final String componentName = current.get(i).getComponent().getClassName();
            final int firstIndex = componentName.indexOf('.');
            final int lastIndex = componentName.lastIndexOf('.');

            // The component classname is actually the ActivityName which will likely be longer
            // than the classname. Make sure the activity name is somewhat valid before attempting
            // to match with the beginning.
            if (firstIndex == -1 || lastIndex == -1 || firstIndex == lastIndex) {
                if (componentName.equals(className)) {
                    position = i;
                }
            } else {
                if (componentName.startsWith(className)) {
                    position = i;
                }
            }
        }

        return position;
    }

    @Override
    public int getCount() {
        return mObjects.size();
    }

    @Override
    public View getDropDownView(final int position, @Nullable final View convertView,
                                @NonNull final ViewGroup parent) {
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

    /**
     * <p>Returns a filter that can be used to constrain data with a filtering
     * pattern.</p>
     * <p>
     * <p>This method is usually implemented by {@link android.widget.Adapter}
     * classes.</p>
     *
     * @return a filter used to constrain data
     */
    @NonNull
    @Override
    public Filter getFilter() {
        return mFilter;
    }

    /**
     * This method returns the {@link LaunchableActivity} found at the {@code position} in this
     * adapter.
     * <p>
     * The position is not altered by the current {@link Filter} in use.
     *
     * @param position The index of the {@code LaunchableActivity} to return.
     * @return The {@code LaunchableActivity} in the position, null otherwise.
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    @Nullable
    @Override
    public T getItem(final int position) {
        return mObjects.get(position);
    }

    /**
     * This returns the position given.
     *
     * @param position The position given.
     * @return The position from the {@code position} argument.
     */
    @Override
    public long getItemId(final int position) {
        return position;
    }

    /**
     * The {@link View} used as a grid item for the LaunchableAdapter {@code GridView}.
     *
     * @param position    The position to of the {@link LaunchableActivity} to return a {@code
     *                    View} for.
     * @param convertView The old {@code View} to reuse, if possible.
     * @param parent      The parent {@code View}.
     * @return The {@code View} to use in the {@code GridView}.
     */
    @NonNull
    @Override
    public View getView(final int position, final View convertView,
                        @NonNull final ViewGroup parent) {
        final View view;

        if (convertView == null) {
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            view = inflater.inflate(R.layout.app_grid_item, parent, false);
        } else {
            view = convertView;
        }

        view.setVisibility(View.VISIBLE);
        final LaunchableActivity launchableActivity;
        synchronized (mLock) {
            launchableActivity = getItem(position);
        }
        final CharSequence label = launchableActivity.toString();
        final TextView appLabelView = view.findViewById(R.id.appLabel);
        final AppIconView appIconView = view.findViewById(R.id.appIcon);

        appLabelView.setText(label);

        appIconView.setTag(launchableActivity);
        appIconView.set(label, launchableActivity.getLabelEn());

        return view;
    }

    /**
     * Notifies the attached observers that the underlying data has been changed
     * and any View reflecting the data set should refresh itself.
     */
    @Override
    public void notifyDataSetChanged() {
        Log.v(TAG, "Notifying about adapter change");
        super.notifyDataSetChanged();
        mNotifyOnChange = true;
    }

    /**
     * This method should be called before the parent context is destroyed.
     */
    public void onStop() {
        mPrefs.close();
    }

    /**
     * This method removes all items by name.
     * <p>
     * This method removes all items by name. This method has been made very specifically to meet
     * the need of this Adapter.
     * <p>
     * This method will remove a package name if it has an equal {@code name} in this Adapter.
     * <p>
     * This method will remove a class name if it has a classname that starts with {@code name} in
     * this adapter.
     * <p>
     * This complexity is required because some packages install multiple activities (see "A Photo
     * Manager"
     * <p>
     * Some packages install activities with duplicate names (see Google Drive/Google Sheets).
     * <p>
     * This method should not be part of this class, but we rely on locking the collections during
     * this critical method.
     *
     * @param name The name. See the description for more information.
     * @return The number of packages removed by this method.
     */
    public int removeAllByName(@NonNull final String name) {
        ComponentName component;
        final List<T> current;
        int removedCount = 0;

        synchronized (mLock) {
            if (mOriginalValues == null) {
                current = mObjects;
            } else {
                current = mOriginalValues;
            }

            for (int i = current.size() - 1; i >= 0; i--) {
                component = current.get(i).getComponent();

                if (component.getClassName().startsWith(name)) {
                    Log.d(TAG, "Removing " + name +
                            " by starting with classname: " + component.getClassName());
                    current.remove(i);
                    removedCount++;
                } else if (component.getPackageName().equals(name)) {
                    Log.d(TAG, "Found position of " + name);
                    current.remove(i);
                    removedCount++;
                }
            }
        }

        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }

        return removedCount;
    }

    /**
     * <p>Sets the layout resource to create the drop down views.</p>
     *
     * @param resource the layout resource defining the drop down views
     * @see #getDropDownView(int, View, ViewGroup)
     */
    public void setDropDownViewResource(@LayoutRes final int resource) {
        mDropDownResource = resource;
    }

    /**
     * Control whether methods that change the list ({@link #add}, {@link #addAll(Collection)},
     * {@link #addAll(LaunchableActivity[])} (Object[])}, {@link #insert}, {@link #remove},
     * {@link #clear}, {@link #sort(Comparator)}) automatically call {@link #notifyDataSetChanged}.
     * If set to false, caller must manually call notifyDataSetChanged() to have the changes
     * reflected in the attached view.
     * <p>
     * The default is true, and calling notifyDataSetChanged()
     * resets the flag to true.
     *
     * @param notifyOnChange if true, modifications to the list will
     *                       automatically call {@link
     *                       #notifyDataSetChanged}
     */
    public void setNotifyOnChange(final boolean notifyOnChange) {
        mNotifyOnChange = notifyOnChange;
    }

    /**
     * Sorts the content of this adapter using the specified comparator.
     *
     * @param comparator The comparator used to sort the objects contained
     *                   in this adapter.
     */
    public void sort(@NonNull final Comparator<? super T> comparator) {
        synchronized (mLock) {
            Collections.sort(mObjects, comparator);
            if (mOriginalValues != null) {
                Collections.sort(mOriginalValues, comparator);
            }
        }

        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
    }

    public void sortApps() {
        synchronized (mLock) {
            final boolean notify = mNotifyOnChange;
            mNotifyOnChange = false;

            final Collator collator = Collator.getInstance(Locale.getDefault());
            collator.setStrength(Collator.PRIMARY);
            sort((o1, o2) -> collator.compare(o1.toString(), o2.toString()));

            sort((o1, o2) -> o2.getPriority() - o1.getPriority());

            if (notify) {
                notifyDataSetChanged();
            }
        }
    }

    /**
     * Returns a string representation of the current LaunchActivity collection.
     *
     * @return A string representation of the current LaunchActivity collection.
     */
    @Override
    public String toString() {
        final String toString;

        if (mOriginalValues == null) {
            toString = mObjects.toString();
        } else {
            toString = mOriginalValues.toString();
        }

        return toString;
    }

    /**
     * <p>An array filter constrains the content of the array adapter with
     * a prefix. Each item that does not start with the supplied prefix
     * is removed from the list.</p>
     */
    private final class LaunchableFilter extends Filter {

        @Override
        protected FilterResults performFiltering(final CharSequence constraint) {
            final List<T> values;
            final FilterResults results = new FilterResults();

            // Don't act upon a blank constraint if the filter hasn't been used yet.
            if (mOriginalValues == null && constraint.length() == 0) {
                results.values = mObjects;
                results.count = mObjects.size();
            } else {
                if (mOriginalValues == null) {
                    synchronized (mLock) {
                        mOriginalValues = new ArrayList<>(mObjects);
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
                    final String prefixString = stripAccents(constraint).toLowerCase();
                    final Collection<T> newValues = new ArrayList<>();

                    for (int i = 0; i < count; i++) {
                        final T value = values.get(i);

                        var targets = value.getLabels();
                        if (QueryVariants.check(prefixString, targets)) {
                            newValues.add(value);
                        }
                    }

                    results.values = newValues;
                    results.count = newValues.size();
                }
            }

            return results;
        }

        @Override
        protected void publishResults(final CharSequence constraint, final FilterResults results) {
            //noinspection ObjectEquality
            if (mObjects != results.values) {
                mObjects.clear();
                //noinspection unchecked
                mObjects.addAll((Collection<T>) results.values);

                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        }

        private String stripAccents(final CharSequence cs) {
            return DIACRITICAL_MARKS.matcher(
                    Normalizer.normalize(cs, Normalizer.Form.NFKD)).replaceAll("");
        }
    }
}
