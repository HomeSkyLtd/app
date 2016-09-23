package com.homesky.homesky.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
public class MenuFragmentsActivity extends AppCompatActivity {

    private String[] mModulesTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    //TODO: Is this the best way to retrieve the right fragment?
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

    private void selectFragment(int position, boolean add) {
        Fragment fragment = createFragment(position);
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (add) {
            fragmentManager.beginTransaction()
                    .add(R.id.menu_fragments_activity_container, fragment)
                    .commit();
        } else {
            fragmentManager.beginTransaction()
                    .replace(R.id.menu_fragments_activity_container, fragment)
                    .commit();
        }

        mDrawerList.setItemChecked(position, true);
        setTitle(mModulesTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, MenuFragmentsActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_activity_fragment);

        mModulesTitles = getResources().getStringArray(R.array.modules_titles);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.menu_fragments_activity_left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mModulesTitles));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectFragment(i, false);
            }
        });

        Fragment fragment = getSupportFragmentManager()
                .findFragmentById(R.id.menu_fragments_activity_container);

        if (fragment == null) {
            selectFragment(0, true);
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
