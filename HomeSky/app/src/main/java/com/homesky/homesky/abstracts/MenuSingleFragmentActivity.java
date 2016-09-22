package com.homesky.homesky.abstracts;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.homesky.homesky.R;

/* AppCompatActivity already extends FragmentActivity */
public abstract class MenuSingleFragmentActivity extends AppCompatActivity {

    private String[] mModulesTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    public abstract Fragment createFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_activity_fragment);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.menu_activity_fragment_container);

        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.menu_activity_fragment_container, fragment)
                    .commit();
        }

        mModulesTitles = getResources().getStringArray(R.array.modules_titles);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerList.setAdapter(new ArrayAdapter<>(
                this,
                R.layout.menu_activity_fragment,
                mModulesTitles
        ));
    }

    @Override
    public void onBackPressed() {
        //TODO: if drawer_list_item is opened, close drawer_list_item. Otherwise go to previous activity.

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_fragment_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
