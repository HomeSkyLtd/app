package com.homesky.homesky.login;

import android.support.v4.app.Fragment;

import com.homesky.homesky.abstracts.SingleFragmentActivity;

public class LoginActivity extends SingleFragmentActivity {

    @Override
    public Fragment createFragment() {
        return new LoginFragment();
    }
}
