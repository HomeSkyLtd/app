package com.homesky.homesky.abstracts;

import android.view.View;
import android.widget.AdapterView;

/**
 * Created by henrique on 9/22/16.
 */
public class DrawerItemClickListener implements AdapterView.OnItemClickListener {
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        selectItem(i);
    }

    private void selectItem(int position) {

    }
}
