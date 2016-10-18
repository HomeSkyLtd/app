package com.homesky.homesky.fragments.ruleList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.homesky.homecloud_lib.model.Proposition;
import com.homesky.homecloud_lib.model.Rule;
import com.homesky.homecloud_lib.model.response.ConflictingRuleResponse;
import com.homesky.homecloud_lib.model.response.NodesResponse;
import com.homesky.homecloud_lib.model.response.RuleResponse;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homesky.R;
import com.homesky.homesky.command.NewRulesCommand;
import com.homesky.homesky.fragments.clause.ClauseActivity;
import com.homesky.homesky.fragments.clause.ClauseFragment;
import com.homesky.homesky.homecloud.HomecloudHolder;
import com.homesky.homesky.login.LoginActivity;
import com.homesky.homesky.request.AsyncRequest;
import com.homesky.homesky.request.ModelStorage;
import com.homesky.homesky.request.RequestCallback;
import com.homesky.homesky.utils.AppEnumUtils;
import com.homesky.homesky.utils.AppFindElementUtils;
import com.homesky.homesky.utils.AppStringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RuleListFragment extends Fragment implements RequestCallback{
    private static final String TAG = "RuleListFragment";
    private static final String ARG_NODE_ID = "nodeId";
    private static final String ARG_CONTROLLER_ID = "controllerId";

    private final String NODE_EXTRA_NAME = "name";
    private static int NEW_RULE_REQUEST = 1;

    private int mNodeId;
    private String mControllerId;
    private RuleAdapter mAdapter;
    private List<NodesResponse.Node> mNodes = null;
    private List<Rule> mRules = null;
    private List<Rule> mRuleToSend = null;

    private TextView mActuatorTextView;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mRuleListSwipeRefresh;
    private FloatingActionButton mFloatingActionButton;
    private ProgressDialog mRingProgressDialog;
    private RelativeLayout mLoadingLayout;

    public static Fragment newInstance(int nodeId, String controllerId){
        Bundle args = new Bundle();
        args.putInt(ARG_NODE_ID, nodeId);
        args.putString(ARG_CONTROLLER_ID, controllerId);
        Fragment fragment = new RuleListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNodeId = getArguments().getInt(ARG_NODE_ID);
        mControllerId = getArguments().getString(ARG_CONTROLLER_ID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rule_list, container, false);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.fragment_rule_list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(48));

        mActuatorTextView = (TextView)view.findViewById(R.id.rule_list_actuator_text_view);
        NodesResponse.Node node = AppFindElementUtils.findNodeFromId(mNodeId, mControllerId, ModelStorage.getInstance().getNodes(this));
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
            }
        });

        mFloatingActionButton = (FloatingActionButton)view.findViewById(R.id.fragment_rule_list_fab);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAdapter.getShouldRetry()){
                    Toast.makeText(
                            getActivity(),
                            getResources().getText(R.string.rule_list_fab_when_retry_message),
                            Toast.LENGTH_LONG).show();
                }
                else{
                    Intent i = ClauseActivity.newIntent(getActivity(), mNodeId, mControllerId);
                    startActivityForResult(i, NEW_RULE_REQUEST);
                }
            }
        });

        mLoadingLayout = (RelativeLayout)view.findViewById(R.id.rule_list_fragment_loading_panel);
        mLoadingLayout.setVisibility(View.VISIBLE);

        updateUI();
        return view;
    }

    private void updateUI(){
        mRules = ModelStorage.getInstance().getRules(this);
        if(mRules != null)
            mNodes = ModelStorage.getInstance().getNodes(this);

        if(mRules != null && mNodes != null) {
            List<Rule> filtered = AppFindElementUtils.findRulesFromNodeId(mNodeId, mControllerId, mRules);
            if (mAdapter == null) {
                mAdapter = new RuleAdapter(filtered);
                mRecyclerView.setAdapter(mAdapter);
                mLoadingLayout.setVisibility(View.GONE);
            } else {
                mAdapter.setRules(filtered);
                mAdapter.notifyDataSetChanged();

                //If layout was refreshing, hide the progress indicator
                if(mRuleListSwipeRefresh.isRefreshing()){
                    mRuleListSwipeRefresh.setRefreshing(false);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == NEW_RULE_REQUEST){
            if(resultCode == Activity.RESULT_OK){
                List<List<Proposition>> clause =
                        (List<List<Proposition>>)data.getSerializableExtra(ClauseFragment.EXTRA_CLAUSE);
                int commandId = data.getIntExtra(ClauseFragment.EXTRA_COMMAND_ID, -1);
                BigDecimal value = (BigDecimal)data.getSerializableExtra(ClauseFragment.EXTRA_VALUE);
                //send to BD
                mRuleToSend = new ArrayList<>();
                mRuleToSend.add(new Rule(mNodeId, mControllerId, commandId, value, clause));
                NewRulesCommand command = new NewRulesCommand(mRuleToSend);
                mRingProgressDialog = ProgressDialog.show(
                        getActivity(),
                        getString(R.string.rule_list_sending_progess_title),
                        getString(R.string.rule_list_sending_progess_message),
                        true);
                new AsyncRequest(this).execute(command);
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onPostRequest(SimpleResponse s) {
        if (s == null) {
            Toast.makeText(
                    getActivity(),
                    getResources().getText(R.string.login_fragment_server_offline),
                    Toast.LENGTH_LONG).show();
        }
        else if (s instanceof ConflictingRuleResponse){
            //If everything OK
            if(s.getStatus() == 200){
                mRules.add(mRuleToSend.get(0));
                List<Rule> filtered = AppFindElementUtils.findRulesFromNodeId(mNodeId, mControllerId, mRules);
                mAdapter.setRules(filtered);
                mAdapter.setShouldRetry(false);
                mAdapter.notifyDataSetChanged();
                mRuleToSend = null;
                ModelStorage.getInstance().invalidateRulesCache();
                mRingProgressDialog.dismiss();
            }
            //If unauthorized
            else if(s.getStatus() == 403){
                HomecloudHolder.getInstance().invalidateSession();
                getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
            }
            //If some other error happened, check if the rule was not sent because of a conflicting rule.
            //In this case, don't offer to resend it
            else {
                Toast.makeText(
                        getActivity(),
                        getResources().getText(R.string.rule_list_retry_message) + ": " + s.getErrorMessage(),
                        Toast.LENGTH_LONG).show();
                if(((ConflictingRuleResponse) s).getConflictingRule() != null) {
                    mAdapter.setShouldRetry(true);
                    mAdapter.notifyDataSetChanged();
                }
                mRingProgressDialog.dismiss();
            }
        }
        else if (s instanceof RuleResponse || s instanceof NodesResponse){
            updateUI();
        }
        else {
            Toast.makeText(
                    getActivity(),
                    getResources().getText(R.string.rule_list_retry_message) + ": " + s.getErrorMessage(),
                    Toast.LENGTH_LONG).show();
            mAdapter.setShouldRetry(true);
            mAdapter.notifyDataSetChanged();
            mRingProgressDialog.dismiss();
        }
    }

    class RuleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_RULE = 1, TYPE_RETRY = 2;

        private List<Rule> mRules;
        private boolean mShouldRetry = false;

        public RuleAdapter(List<Rule> rules) {
            mRules = rules;
        }

        public void setRules(List<Rule> rules) {
            mRules = rules;
        }

        public List<Rule> getRules() {
            return mRules;
        }

        private void setShouldRetry(boolean shouldRetry){
            mShouldRetry = shouldRetry;
        }

        public boolean getShouldRetry() {
            return mShouldRetry;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            if (viewType == TYPE_RULE) {
                View view = layoutInflater.inflate(R.layout.list_rule_item, parent, false);
                return new RuleHolder(view);
            } else if (viewType == TYPE_RETRY) {
                View view = layoutInflater.inflate(R.layout.list_rule_error_item, parent, false);
                return new RetryHolder(view);
            } else {
                return null;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (position < mRules.size() && !mShouldRetry)
                ((RuleHolder) holder).bindRule(mRules.get(position));
        }

        @Override
        public int getItemCount() {
            if (!mShouldRetry)
                return mRules.size();
            else
                return mRules.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (mShouldRetry && position == mRules.size()) {
                return TYPE_RETRY;
            } else {
                return TYPE_RULE;
            }
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

    class RetryHolder extends RecyclerView.ViewHolder {

        private Button mRetryButton;

        public RetryHolder(View itemView) {
            super(itemView);
            mRetryButton = (Button)itemView.findViewById(R.id.list_rule_error_item_button);
            mRetryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NewRulesCommand command = new NewRulesCommand(mRuleToSend);
                    mRingProgressDialog = ProgressDialog.show(
                            getActivity(),
                            getString(R.string.rule_list_sending_progess_title),
                            getString(R.string.rule_list_sending_progess_message),
                            true);
                    new AsyncRequest(RuleListFragment.this).execute(command);
                }
            });
        }
    }

    public class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {

        private final int mVerticalSpaceHeight;

        public VerticalSpaceItemDecoration(int mVerticalSpaceHeight) {
            this.mVerticalSpaceHeight = mVerticalSpaceHeight;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            outRect.bottom = mVerticalSpaceHeight;
        }
    }
}
