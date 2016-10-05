package com.homesky.homesky.fragments.ruleList;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.homesky.homecloud_lib.model.response.NodesResponse;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homesky.R;
import com.homesky.homesky.request.ModelStorage;
import com.homesky.homesky.request.RequestCallback;

import java.util.List;

public class RuleListFragment extends Fragment implements RequestCallback{
    private static final String TAG = "RuleListFragment";
    private static final String ARG_NODE_ID = "nodeId";

    private final String NODE_EXTRA_NAME = "name";

    private int mNodeId;

    private TextView mActuatorTextView;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mRuleListSwipeRefresh;

    public static Fragment newInstance(int nodeId){
        Bundle args = new Bundle();
        args.putInt(ARG_NODE_ID, nodeId);
        Fragment fragment = new RuleListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNodeId = getArguments().getInt(ARG_NODE_ID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rule_list, container, false);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.fragment_rule_list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mActuatorTextView = (TextView)view.findViewById(R.id.rule_list_actuator_text_view);
        NodesResponse.Node node = findNodeFromId(mNodeId, ModelStorage.getInstance().getNodes(this));
        mActuatorTextView.setText(node.getExtra().get(NODE_EXTRA_NAME));

        mRuleListSwipeRefresh = (SwipeRefreshLayout)view.findViewById(R.id.rule_list_swipe_refresh_layout);
        mRuleListSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

            }
        });
        return view;
    }

    private NodesResponse.Node findNodeFromId(int id, List<NodesResponse.Node> nodes) {
        for (NodesResponse.Node n : nodes) {
            if (n.getNodeId() == id)
                return n;
        }
        return null;
    }

    @Override
    public void onPostRequest(SimpleResponse s) {

    }
}
