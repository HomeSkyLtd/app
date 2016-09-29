package com.homesky.homesky.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.homesky.homesky.R;
import com.homesky.homesky.activities.MenuFragmentsActivity;
import com.homesky.homesky.user.UserActivity;

public class LoginFragment extends Fragment {

    private Button mLoginButton;
    private Button mSigninButton;

    public LoginFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        view.findViewById(R.id.login_fragment_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(MenuFragmentsActivity.newIntent(getActivity()));
            }
        });

        mLoginButton = (Button) view.findViewById(R.id.login_fragment_login_button);
        mSigninButton = (Button) view.findViewById(R.id.login_fragment_signin_button);
        mSigninButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(UserActivity.newIntent(getActivity()));
            }
        });

        return view;
    }
}
