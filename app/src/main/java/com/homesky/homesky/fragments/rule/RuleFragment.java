package com.homesky.homesky.fragments.rule;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.homesky.homesky.R;

/**
 * Created by henrique on 9/22/16.
 */
public class RuleFragment extends Fragment {
    public RuleFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rule, container, false);
        return view;
    }
}
