package com.homesky.homesky.user;

import android.support.v4.app.Fragment;

import com.homesky.homesky.abstracts.MenuSingleFragmentActivity;

/**
 * Created by henrique on 9/22/16.
 */
public class UserActivity extends MenuSingleFragmentActivity {
    @Override
    public Fragment createFragment() {
        return new UserFragment();
    }
}
