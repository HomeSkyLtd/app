package com.homesky.homesky.fragments.node;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.homesky.homesky.R;

/**
 * Created by henrique on 10/3/16.
 */

public class NodeFragment extends Fragment {

    private Node mNode;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNode = (Node) getActivity().getIntent().getSerializableExtra(NodeActivity.EXTRA_NODE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_node, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_node_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ItemAdapter adapter = new ItemAdapter(getActivity(), mNode);
        mRecyclerView.setAdapter(adapter);

        return view;
    }
}
