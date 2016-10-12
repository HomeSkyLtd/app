package com.homesky.homesky.fragments.orStatement;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.homesky.homesky.R;

public class OrStatementFragment extends Fragment {

    private static final String ARG_STATEMENT_POSITION = "statement_position";

    public static Fragment newInstance(int position){
        Bundle args = new Bundle();
        args.putInt(ARG_STATEMENT_POSITION, position);
        Fragment fragment = new OrStatementFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_or_statement, container, false);

        return view;
    }
}
