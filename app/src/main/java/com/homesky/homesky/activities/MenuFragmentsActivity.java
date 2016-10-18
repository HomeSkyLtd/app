package com.homesky.homesky.activities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.homesky.homecloud_lib.model.notification.ActionResultNotification;
import com.homesky.homecloud_lib.model.notification.DetectedNodeNotification;
import com.homesky.homecloud_lib.model.notification.LearntRulesNotification;
import com.homesky.homecloud_lib.model.notification.Notification;
import com.homesky.homesky.MessageService;
import com.homesky.homesky.R;
import com.homesky.homesky.fragments.controller.ControllerFragment;
import com.homesky.homesky.fragments.notification.NotificationFragment;
import com.homesky.homesky.fragments.rule.RuleFragment;
import com.homesky.homesky.fragments.settings.SettingsFragment;
import com.homesky.homesky.fragments.state.StateFragment;
import com.homesky.homesky.homecloud.HomecloudHolder;
import com.homesky.homesky.user.UserFragment;

import java.util.HashMap;
import java.util.Map;

import static android.support.v4.app.NotificationCompat.DEFAULT_SOUND;
import static android.support.v4.app.NotificationCompat.DEFAULT_VIBRATE;

/* AppCompatActivity already extends FragmentActivity */
public class MenuFragmentsActivity extends AppCompatActivity {

    private static final String TAG = "MenuFragmentsActivity";

    public static final String EXTRA_STARTING_FRAGMENT = "com.homesky.homesky.activities";

    private String[] mModulesTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Map<String, Fragment> mFragments;
    private ActionBarDrawerToggle mDrawerToggle;

    private HomeSkyBroadcastReceiver mReceiver;

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

        int startingFragment = getIntent().getIntExtra(EXTRA_STARTING_FRAGMENT, 0);
        selectFragment(startingFragment);

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

        mReceiver = new HomeSkyBroadcastReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver((mReceiver), new IntentFilter(MessageService.NOTIF_RESULT));
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

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    public static class HomeSkyBroadcastReceiver extends BroadcastReceiver {

        private String ACTION = "action", NODE = "node", RULE = "rule";
        private Map<String, String> mNotificationsString;

        public HomeSkyBroadcastReceiver() {
            reset();
        }

        public void reset() {
            mNotificationsString = new HashMap<>(3);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Notification n = (Notification) intent.getSerializableExtra(MessageService.NOTIF_MESSAGE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

            builder
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_home_white_24dp)
                    .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                    .setColor(ResourcesCompat.getColor(context.getResources(), R.color.colorPrimary, context.getTheme()))
                    .setContentTitle("HomeSky")
                    .setContentText("Touch to see what's new in your house");

            if (n instanceof ActionResultNotification) {
                ActionResultNotification an = (ActionResultNotification) n;
                String str = "Sent action: value " + an.getAction().getValue() + " to node " + an.getAction().getNodeId();
                mNotificationsString.put(ACTION, str);

            } else if (n instanceof DetectedNodeNotification) {
                DetectedNodeNotification dn = (DetectedNodeNotification) n;
                String str = "New nodes: " + dn.getNumberOfNodes();
                mNotificationsString.put(NODE, str);

            } else if (n instanceof LearntRulesNotification) {
                LearntRulesNotification ln = (LearntRulesNotification) n;
                String str = "New rules: " + ln.getNumberOfRules();
                mNotificationsString.put(RULE, str);

            }

            NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle()
                    .setSummaryText(HomecloudHolder.getInstance().getUsername());
            for (String s : mNotificationsString.values()) {
                style.addLine(s);
            }

            builder.setStyle(style);

            Intent resultIntent = new Intent(context, MenuFragmentsActivity.class);
            resultIntent.putExtra(MenuFragmentsActivity.EXTRA_STARTING_FRAGMENT, 4);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(MenuFragmentsActivity.class);

            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            builder.setContentIntent(resultPendingIntent);
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


            notificationManager.notify(0, builder.build());
        }
    }

    public void reset() {
        mReceiver.reset();
    }
}
