package com.homesky.homesky.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.homesky.homesky.R;
import com.homesky.homesky.controller.ControllerFragment;
import com.homesky.homesky.notification.NotificationFragment;
import com.homesky.homesky.rule.RuleFragment;
import com.homesky.homesky.state.StateFragment;
import com.homesky.homesky.user.UserFragment;

/* AppCompatActivity already extends FragmentActivity */
public class MenuSingleFragmentActivity extends AppCompatActivity {

    private String[] mModulesTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private class DrawerItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            selectItem(i);
        }

        private void selectItem(int position) {
            Fragment fragment = createFragment(position);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.menu_activity_fragment_container, fragment)
                    .commit();
            mDrawerList.setItemChecked(position, true);
            setTitle(mModulesTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }


    public Fragment createFragment(int position) {
        switch (mModulesTitles[position]) {
            case "Controller":
                return new ControllerFragment();
            case "Notification":
                return new NotificationFragment();
            case "Rules":
                return new RuleFragment();
            case "State":
                return new StateFragment();
            case "User":
                return new UserFragment();
            default:
                return new StateFragment();
        }
    }

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, MenuSingleFragmentActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_activity_fragment);

        mModulesTitles = getResources().getStringArray(R.array.modules_titles);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<String>(
                this,
                R.layout.drawer_list_item,
                mModulesTitles
        ));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.menu_activity_fragment_container);

        if (fragment == null) {
            fragment = createFragment(0);
            fm.beginTransaction()
                    .add(R.id.menu_activity_fragment_container, fragment)
                    .commit();
            setTitle(mModulesTitles[0]);
        }
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

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
