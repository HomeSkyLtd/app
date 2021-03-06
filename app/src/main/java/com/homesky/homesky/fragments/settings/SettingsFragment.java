package com.homesky.homesky.fragments.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homesky.R;
import com.homesky.homesky.command.LogoutCommand;
import com.homesky.homesky.login.LoginActivity;
import com.homesky.homesky.request.AsyncRequest;
import com.homesky.homesky.request.RequestCallback;

/**
 * Created by henrique on 9/27/16.
 */

public class SettingsFragment extends Fragment {
    public SettingsFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        return view;
    }
}
