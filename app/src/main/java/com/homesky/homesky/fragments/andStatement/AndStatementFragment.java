package com.homesky.homesky.fragments.andStatement;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.homesky.homecloud_lib.model.Proposition;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homesky.R;
import com.homesky.homesky.request.ModelStorage;
import com.homesky.homesky.request.RequestCallback;
import com.homesky.homesky.utils.AppStringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AndStatementFragment extends Fragment implements RequestCallback {

    private static final String ARG_STATEMENT_ITEMS = "statement_items";

    private List<String> mStatement;
    private RecyclerView mRecyclerView;
    private AndStatementAdapter mAdapter;

    public static Fragment newInstance(List<String> andStatement) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_STATEMENT_ITEMS, (Serializable) andStatement);
        Fragment fragment = new AndStatementFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStatement = (List<String>) getArguments().getSerializable(ARG_STATEMENT_ITEMS);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_and_statement, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_and_statement_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (mAdapter == null) {
            mAdapter = new AndStatementAdapter(mStatement);
        } else {
            mAdapter.setPropositions(mStatement);
            mAdapter.notifyDataSetChanged();
        }
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onPostRequest(SimpleResponse s) {

    }

    class AndStatementAdapter extends RecyclerView.Adapter<AndStatementHolder> {

        List<String> mPropositionsAsString;

        public AndStatementAdapter(List<String> propositionsAsString) {
            mPropositionsAsString = propositionsAsString;
        }


        public void setPropositions(List<String> propositionsAsString) {
            mPropositionsAsString = propositionsAsString;
        }

        @Override
        public AndStatementHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_and_statement_item, parent, false);
            return new AndStatementHolder(view);
        }

        @Override
        public void onBindViewHolder(AndStatementHolder holder, int position) {
            holder.bindProposition(mPropositionsAsString.get(position));
        }

        @Override
        public int getItemCount() {
            return mPropositionsAsString.size();
        }
    }

    class AndStatementHolder extends RecyclerView.ViewHolder {
        private TextView mStatementTextView;

        public AndStatementHolder(View itemView) {
            super(itemView);
            mStatementTextView = (TextView) itemView.findViewById(R.id.and_statement_item_text_view);
        }

        public void bindProposition(String propositionAsString) {
            mStatementTextView.setText(propositionAsString);
        }
    }
}
