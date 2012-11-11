
package de.wangchao.musicplayer.widget;

/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class MyArrayAdapter<T> extends BaseAdapter {
    /**
     * Contains the list of objects that represent the data of this
     * ArrayAdapter. The content of this list is referred to as "the array" in
     * the documentation.
     */
    private List<T> mObjects;

    /**
     * Lock used to modify the content of {@link #mObjects}. Any write operation
     * performed on the array should be synchronized on this lock. This lock is
     * also used by the filter (see {@link #getFilter()} to make a synchronized
     * copy of the original array of data.
     */
    private final Object mLock = new Object();

    /**
     * Indicates whether or not {@link #notifyDataSetChanged()} must be called
     * whenever {@link #mObjects} is modified.
     */
    private boolean mNotifyOnChange = true;

    protected Context mContext;

    protected LayoutInflater mInflater;

    /**
     * Constructor
     * 
     * @param context The current context.
     */
    public MyArrayAdapter(Context context) {

        init(context, new ArrayList<T>());
    }

    /**
     * Constructor
     * 
     * @param context The current context.
     * @param objects The objects to represent in the ListView.
     */
    public MyArrayAdapter(Context context, T[] objects) {

        init(context, Arrays.asList(objects));
    }

    /**
     * Adds the specified object at the end of the array.
     * 
     * @param object The object to add at the end of the array.
     */
    public void add(T object) {

        synchronized (mLock) {
            mObjects.add(object);
            if (mNotifyOnChange)
                notifyDataSetChanged();
        }
    }

    public void add(ArrayList<T> objects) {

        synchronized (mLock) {
            if (objects == null) {
                return;
            }
            mObjects.addAll(objects);
            if (mNotifyOnChange)
                notifyDataSetChanged();
        }
    }

    public void set(ArrayList<T> objects) {

        synchronized (mLock) {
            if (objects == null) {
                mObjects.clear();
            } else {
                mObjects = objects;
            }
            if (mNotifyOnChange)
                notifyDataSetChanged();
        }
    }

    /**
     * Inserts the specified object at the specified index in the array.
     * 
     * @param object The object to insert into the array.
     * @param index The index at which the object must be inserted.
     */
    public void insert(T object, int index) {

        synchronized (mLock) {
            mObjects.add(index, object);
            if (mNotifyOnChange)
                notifyDataSetChanged();
        }
    }

    /**
     * Removes the specified object from the array.
     * 
     * @param object The object to remove.
     */
    public void remove(T object) {

        synchronized (mLock) {
            mObjects.remove(object);
            if (mNotifyOnChange)
                notifyDataSetChanged();
        }
    }

    /**
     * Remove all elements from the list.
     */
    public void clear() {

        synchronized (mLock) {
            mObjects.clear();
            if (mNotifyOnChange)
                notifyDataSetChanged();
        }
    }

    /**
     * Sorts the content of this adapter using the specified comparator.
     * 
     * @param comparator The comparator used to sort the objects contained in
     *            this adapter.
     */
    public void sort(Comparator<? super T> comparator) {

        Collections.sort(mObjects, comparator);
        if (mNotifyOnChange)
            notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyDataSetChanged() {

        super.notifyDataSetChanged();
        mNotifyOnChange = true;
    }

    /**
     * Control whether methods that change the list ({@link #add},
     * {@link #insert}, {@link #remove}, {@link #clear}) automatically call
     * {@link #notifyDataSetChanged}. If set to false, caller must manually call
     * notifyDataSetChanged() to have the changes reflected in the attached
     * view. The default is true, and calling notifyDataSetChanged() resets the
     * flag to true.
     * 
     * @param notifyOnChange if true, modifications to the list will
     *            automatically call {@link #notifyDataSetChanged}
     */
    public void setNotifyOnChange(boolean notifyOnChange) {

        mNotifyOnChange = notifyOnChange;
    }

    private void init(Context context, List<T> objects) {

        mContext = context;
        mObjects = objects;
        mInflater = LayoutInflater.from(context);
    }

    /**
     * Returns the context associated with this array adapter. The context is
     * used to create views from the resource passed to the constructor.
     * 
     * @return The Context associated with this adapter.
     */
    public Context getContext() {

        return mContext;
    }

    /**
     * {@inheritDoc}
     */
    public int getCount() {

        return mObjects.size();
    }

    /**
     * {@inheritDoc}
     */
    public T getItem(int position) {

        return mObjects.get(position);
    }

    /**
     * Returns the position of the specified item in the array.
     * 
     * @param item The item to retrieve the position of.
     * @return The position of the specified item.
     */
    public int getPosition(T item) {

        return mObjects.indexOf(item);
    }

    /**
     * {@inheritDoc}
     */
    public long getItemId(int position) {

        return position;
    }

}
