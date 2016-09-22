package com.homesky.homesky.Login;

import android.support.v4.app.Fragment;
import android.os.Bundle;

import com.homesky.homesky.R;

public class LoginActivity extends SingleFragmentActivity {

    @Override
    public Fragment createFragment() {
        return new LoginFragment();
    }
}
