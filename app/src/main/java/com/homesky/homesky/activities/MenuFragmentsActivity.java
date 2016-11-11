package com.homesky.homesky.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homesky.R;
import com.homesky.homesky.command.LogoutCommand;
import com.homesky.homesky.fragments.controller.ControllerFragment;
import com.homesky.homesky.fragments.notification.FirebaseBackgroundService;
import com.homesky.homesky.fragments.notification.NotificationFragment;
import com.homesky.homesky.fragments.rule.RuleFragment;
import com.homesky.homesky.fragments.settings.SettingsFragment;
import com.homesky.homesky.fragments.state.StateFragment;
import com.homesky.homesky.login.LoginActivity;
import com.homesky.homesky.request.AsyncRequest;
import com.homesky.homesky.request.ModelStorage;
import com.homesky.homesky.request.RequestCallback;
import com.homesky.homesky.user.NewUserDialog;
import com.homesky.homesky.user.UserFragment;

import java.util.HashMap;
import java.util.Map;

/* AppCompatActivity already extends FragmentActivity */
public class MenuFragmentsActivity extends AppCompatActivity implements NewUserDialog.NewUserDialogCallback{

    private static final String TAG = "MenuFragmentsActivity";

    public static final String EXTRA_STARTING_FRAGMENT = "com.homesky.homesky.activities.starting_fragment";
    public static final String EXTRA_NOTIFICATION_MAP = "com.homesky.homesky.activities.notification_map";

    private String[] mModulesTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Map<String, Fragment> mFragments;
    private ActionBarDrawerToggle mDrawerToggle;

    private Fragment createFragment(int position) {
        String f = mModulesTitles[position];
        return mFragments.get(f);
    }

    private void selectFragment(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.menu_fragments_activity_container);

        if (fragment == null) {
            fragment = createFragment(position);
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

        Log.d(TAG, "onCreate: I will call Firebase");
        Intent notificationsService = new Intent(this, FirebaseBackgroundService.class);
        startService(notificationsService);

        mModulesTitles = getResources().getStringArray(R.array.modules_titles);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.menu_fragments_activity_left_drawer);

        mDrawerList.setAdapter(new DrawerLayoutAdapter(getLayoutInflater(), mModulesTitles));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == mModulesTitles.length - 1) {
                    new AsyncRequest(new RequestCallback() {
                        @Override
                        public void onPostRequest(SimpleResponse s) {
                            ModelStorage.getInstance().invalidateNodesCache();
                            ModelStorage.getInstance().invalidateLearntRulesCache();
                            ModelStorage.getInstance().invalidateNodeStatesCache();
                            ModelStorage.getInstance().invalidateControllersCache();
                            ModelStorage.getInstance().invalidateRulesCache();
                            ModelStorage.getInstance().invalidateUsersCache();


                            startActivity(
                                LoginActivity.newIntent(getApplicationContext(), LoginActivity.LoginAction.LOGIN)
                            );
                        }
                    }).execute(new LogoutCommand());
                } else {
                    selectFragment(i);
                }
            }
        });

        int startingFragment = getIntent().getIntExtra(EXTRA_STARTING_FRAGMENT, 0);
        selectFragment(startingFragment);

        if (FirebaseBackgroundService.getReceiver() != null) {
            FirebaseBackgroundService.getReceiver().reset();
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, 0, 0) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
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

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onNewUserResult(String username, String password) {
        UserFragment fragment = (UserFragment)mFragments.get("User");
        // Safety check
        if(fragment.isAdded()){
            fragment.onNewUserResult(username, password);
        }
    }
}
