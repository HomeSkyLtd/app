package com.homesky.homesky.fragments.andStatement;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.homesky.homesky.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AndStatementFragment extends Fragment {

    private static final String ARG_STATEMENT_ITEMS = "statement_items";

    private List<String> mStatement;

    public static Fragment newInstance(List<String> andStatement){
        Bundle args = new Bundle();
        args.putSerializable(ARG_STATEMENT_ITEMS, (Serializable)andStatement);
        Fragment fragment = new AndStatementFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStatement = (List<String>)getArguments().getSerializable(ARG_STATEMENT_ITEMS);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_and_statement, container, false);

        return view;
    }
}
