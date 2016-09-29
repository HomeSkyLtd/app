package com.homesky.homesky.user;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.homesky.homesky.activities.SingleFragmentActivity;

/**
 * Created by henrique on 9/23/16.
 */

public class UserActivity extends SingleFragmentActivity {
    @Override
    public Fragment createFragment() {
        return new UserFragment();
    }

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, UserActivity.class);
        return intent;
    }

}
