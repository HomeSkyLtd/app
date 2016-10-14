package com.homesky.homesky.fragments.ruleList;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.homesky.homecloud_lib.model.Proposition;
import com.homesky.homecloud_lib.model.Rule;
import com.homesky.homecloud_lib.model.response.NodesResponse;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homesky.R;
import com.homesky.homesky.fragments.clause.ClauseActivity;
import com.homesky.homesky.request.ModelStorage;
import com.homesky.homesky.request.RequestCallback;
import com.homesky.homesky.utils.AppEnumUtils;
import com.homesky.homesky.utils.AppFindElementUtils;
import com.homesky.homesky.utils.AppStringUtils;

import java.util.ArrayList;
import java.util.List;

public class RuleListFragment extends Fragment implements RequestCallback{
    private static final String TAG = "RuleListFragment";
    private static final String ARG_NODE_ID = "nodeId";

    private final String NODE_EXTRA_NAME = "name";

    private int mNodeId;
    private RuleAdapter mAdapter;
    private List<NodesResponse.Node> mNodes = null;
    private List<Rule> mRules = null;

    private TextView mActuatorTextView;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mRuleListSwipeRefresh;
    private FloatingActionButton mFloatingActionButton;

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
        NodesResponse.Node node = AppFindElementUtils.findNodeFromId(mNodeId, ModelStorage.getInstance().getNodes(this));
        mActuatorTextView.setText(node.getExtra().get(NODE_EXTRA_NAME));

        mRuleListSwipeRefresh = (SwipeRefreshLayout)view.findViewById(R.id.rule_list_swipe_refresh_layout);
        mRuleListSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mNodes = null;
                mRules = null;
                ModelStorage.getInstance().invalidateNodesCache();
                ModelStorage.getInstance().invalidateRulesCache();
                updateUI();
                mRuleListSwipeRefresh.setRefreshing(false);
            }
        });

        mFloatingActionButton = (FloatingActionButton)view.findViewById(R.id.fragment_rule_list_fab);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = ClauseActivity.newIntent(getActivity(), mNodeId);
                startActivity(i);
            }
        });

        updateUI();
        return view;
    }

    private void updateUI(){
        mRules = ModelStorage.getInstance().getRules(this);
        if(mRules != null)
            mNodes = ModelStorage.getInstance().getNodes(this);

        if(mRules != null && mNodes != null) {
            List<Rule> filtered = AppFindElementUtils.findRulesFromNodeId(mNodeId, mRules);
            if (mAdapter == null) {
                mAdapter = new RuleAdapter(filtered);
            } else {
                mAdapter.setRules(filtered);
                mAdapter.notifyDataSetChanged();
            }
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public void onPostRequest(SimpleResponse s) {
        if (s == null) {
            Toast.makeText(
                    getActivity(),
                    getResources().getText(R.string.login_fragment_server_offline),
                    Toast.LENGTH_LONG).show();
        } else {
            updateUI();
        }
    }

    class RuleAdapter extends RecyclerView.Adapter<RuleHolder> {
        private List<Rule> mRules;

        public RuleAdapter(List<Rule> rules) {
            mRules = rules;
        }

        public void setRules(List<Rule> rules){
            mRules = rules;
        }

        @Override
        public RuleHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_rule_item, parent, false);

            return new RuleHolder(view);
        }

        @Override
        public void onBindViewHolder(RuleHolder holder, int position) {
            holder.bindRule(mRules.get(position));
        }

        @Override
        public int getItemCount() {
            return mRules.size();
        }
    }

    class RuleHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        Rule mRule;

        TextView mRuleCondition, mRuleEffect;

        public RuleHolder(View itemView) {
            super(itemView);
            mRuleCondition = (TextView)itemView.findViewById(R.id.rule_condition_text_view);
            mRuleEffect = (TextView)itemView.findViewById(R.id.rule_effect_text_view);

            itemView.setOnClickListener(this);
        }

        public void bindRule(Rule r){
            mRule = r;
            mRuleEffect.setText(AppStringUtils.getRuleEffectLegibleText(getActivity(), mRule, mNodes));
            mRuleCondition.setText(AppStringUtils.getRuleConditionLegibleText(getActivity(), mRule, mNodes));
        }

        @Override
        public void onClick(View view) {
        }
    }
}
