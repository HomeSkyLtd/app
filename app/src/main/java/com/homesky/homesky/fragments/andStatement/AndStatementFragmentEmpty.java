package com.homesky.homesky.fragments.andStatement;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.homesky.homesky.R;

import java.util.ArrayList;
import java.util.List;

public class AndStatementFragmentEmpty extends Fragment {

    public static Fragment newInstance(){
        return new AndStatementFragmentEmpty();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_and_statement_empty, container, false);
        return view;
    }
}
