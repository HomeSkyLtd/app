package com.homesky.homesky.activities;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.homesky.homesky.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by henrique on 9/23/16.
 */


/**
 * This class helps creating the items in drawer list
 * It can be extended to hold ImageViews, TextViews, etc.
 */
class DrawerLayoutAdapter extends BaseAdapter {

    private static final String TAG = "DrawerLayoutAdapter";

    private LayoutInflater mInflater = null;
    private String[] mModules;
    final private Map<String, Integer> mapToIcon;

    DrawerLayoutAdapter(LayoutInflater inflater, String[] modules) {
        mInflater = inflater;
        mModules = modules;

        mapToIcon = new HashMap<>();
        mapToIcon.put("State", R.drawable.ic_poll_white_24dp);
        mapToIcon.put("Rules", R.drawable.ic_library_books_white_24dp);
        mapToIcon.put("User", R.drawable.ic_person_white_24dp);
        mapToIcon.put("Controller", R.drawable.ic_router_white_24dp);
        mapToIcon.put("Notification", R.drawable.ic_message_white_24dp);
        mapToIcon.put("Settings", R.drawable.ic_settings_white_24dp);
    }

    @Override
    public int getCount() {
        return mModules.length;
    }

    @Override
    public Object getItem(int i) {
        if (i > getCount()) return null;

        return mModules[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null)
            view = mInflater.inflate(R.layout.drawer_list_item, null);

        ImageView image = (ImageView) view.findViewById(R.id.drawer_list_icon);
        image.setImageResource(mapToIcon.get(mModules[i]));

        TextView text = (TextView) view.findViewById(R.id.drawer_list_title);
        text.setText(mModules[i]);

        return view;
    }
}
