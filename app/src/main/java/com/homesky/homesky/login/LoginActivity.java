package com.homesky.homesky.login;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.homesky.homesky.activities.SingleFragmentActivity;

public class LoginActivity extends SingleFragmentActivity {

    @Override
    public Fragment createFragment() {
        return new LoginFragment();
    }
}
