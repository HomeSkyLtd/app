package com.homesky.homesky.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.homesky.homesky.R;
import com.homesky.homesky.fragments.controller.ControllerFragment;
import com.homesky.homesky.fragments.notification.NotificationFragment;
import com.homesky.homesky.fragments.rule.RuleFragment;
import com.homesky.homesky.fragments.settings.SettingsFragment;
import com.homesky.homesky.fragments.state.StateFragment;
import com.homesky.homesky.user.UserFragment;

import java.util.HashMap;
import java.util.Map;

/* AppCompatActivity already extends FragmentActivity */
public class MenuFragmentsActivity extends AppCompatActivity {

    private String[] mModulesTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Map<String, Fragment> mFragments;

    private Fragment createFragment(int position) {
        String f = mModulesTitles[position];
        return mFragments.get(f);
    }

    private void selectFragment(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.menu_fragments_activity_container);

        if (fragment == null) {
            fragment = createFragment(0);
            fragmentManager.beginTransaction()
                    .add(R.id.menu_fragments_activity_container, fragment)
                    .commit();
        } else {
            fragment = createFragment(position);
            fragmentManager.beginTransaction()
                    .replace(R.id.menu_fragments_activity_container, fragment)
                    .commit();
        }

        mDrawerList.setItemChecked(position, true);
        setTitle(mModulesTitles[position]);
        mDrawerLayout.closeDrawer(Gravity.LEFT);
    }

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, MenuFragmentsActivity.class);
        return intent;
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.menu_fragments_activity_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(Gravity.LEFT, true);
            }
        });
    }

    private void initFragments() {
        mFragments = new HashMap<>();

        mFragments.put("State",         new StateFragment());
        mFragments.put("Rules",         new RuleFragment());
        mFragments.put("User",          new UserFragment());
        mFragments.put("Controller",    new ControllerFragment());
        mFragments.put("Notification",  new NotificationFragment());
        mFragments.put("Settings",      new SettingsFragment());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_activity_fragment);

        initFragments();

        mModulesTitles = getResources().getStringArray(R.array.modules_titles);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.menu_fragments_activity_left_drawer);

        mDrawerList.setAdapter(new DrawerLayoutAdapter(getLayoutInflater(), mModulesTitles));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectFragment(i);
            }
        });

        selectFragment(0);
        initToolbar();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_fragment_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.search_button) {
            Snackbar.make(
                            findViewById(R.id.menu_fragments_activity_container),
                            "I WAS CLICKED!",
                            Snackbar.LENGTH_SHORT
            ).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
