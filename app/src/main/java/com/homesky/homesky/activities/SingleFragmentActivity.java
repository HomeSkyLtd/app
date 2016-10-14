package com.homesky.homesky.activities;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.widget.Toast;

import com.homesky.homesky.R;

/* This class is extended by the Login activity and the Register activity */
public abstract class SingleFragmentActivity extends AppCompatActivity {

    private static final String TAG = "SingleFragmentActivity";

    private boolean mLockActivity = false;
    private String mMessage;

    public abstract Fragment createFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_fragment, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mLockActivity) {
            Toast.makeText(this, mMessage, Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }

    public void lockActivity(boolean lock, String message) {
        mLockActivity = lock;
        mMessage = message;

        android.app.ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(lock);
        }
    }
}
