package com.homesky.homesky.activities;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.homesky.homesky.R;

/**
 * Created by henrique on 9/23/16.
 */


/**
 * This class helps creating the items in drawer list
 * It can be extended to hold ImageViews, TextViews, etc.
 */
class DrawerLayoutAdapter extends BaseAdapter {

    private static LayoutInflater sInflater = null;
    private String[] mModules;

    DrawerLayoutAdapter(Context context, String[] modules) {
        sInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mModules = modules;
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
            view = sInflater.inflate(R.layout.drawer_list_item, null);

        /*ImageView image = (ImageView) view.findViewById(R.id.drawer_list_icon);
        image.setImageResource(R.drawable.ic_poll_white_24dp);*/

        TextView text = (TextView) view.findViewById(R.id.drawer_list_title);
        text.setText(mModules[i]);

        return view;
    }
}
