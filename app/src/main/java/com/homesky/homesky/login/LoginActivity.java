package com.homesky.homesky.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.homesky.homesky.activities.SingleFragmentActivity;

public class LoginActivity extends SingleFragmentActivity {

    public enum LoginAction { LOGIN, SIGNIN }
    private static final String TYPE_FRAGMENT_EXTRA = "com.homesky.homesky.login.type";

    private Fragment mFragment;
    public static Intent newIntent(Context context, LoginAction action) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(TYPE_FRAGMENT_EXTRA, action);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LoginAction action = (LoginAction) getIntent().getSerializableExtra(TYPE_FRAGMENT_EXTRA);

        if (action == null || action == LoginAction.LOGIN) {
            mFragment = new LoginFragment();
        } else if (action == LoginAction.SIGNIN) {
            mFragment = new SigninFragment();
        } else {
            mFragment = new LoginFragment();
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    public Fragment createFragment() {
        return mFragment;
    }
}
