package com.homesky.homesky.state;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.homesky.homesky.abstracts.MenuSingleFragmentActivity;

/**
 * Created by henrique on 9/22/16.
 */
public class StateActivity extends MenuSingleFragmentActivity {

    /**
     * newIntent
     * This method can be further extended to accept extras.
     * @param context
     * @return intent instance to start this activity
     */
    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, StateActivity.class);
        return intent;
    }

    @Override
    public Fragment createFragment() {
        return new StateFragment();
    }
}
