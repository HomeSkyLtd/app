package com.homesky.homesky.fragments.notification;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.homesky.homesky.MessageService;
import com.homesky.homesky.R;
import com.homesky.homesky.activities.MenuFragmentsActivity;

/**
 * Created by henrique on 9/22/16.
 */
public class NotificationFragment extends Fragment {
    public NotificationFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        ((MenuFragmentsActivity) getActivity()).reset();

        return view;
    }
}
