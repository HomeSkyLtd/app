package com.homesky.homesky.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.homesky.homesky.R;
import com.homesky.homesky.state.StateActivity;

public class LoginFragment extends Fragment {

    public LoginFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        view.findViewById(R.id.login_fragment_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(StateActivity.newIntent(getActivity()));
            }
        });

        return view;
    }
}
