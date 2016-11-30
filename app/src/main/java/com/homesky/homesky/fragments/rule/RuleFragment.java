package com.homesky.homesky.fragments.rule;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.homesky.homecloud_lib.model.Rule;
import com.homesky.homecloud_lib.model.enums.NodeClassEnum;
import com.homesky.homecloud_lib.model.response.NodesResponse;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homesky.R;
import com.homesky.homesky.command.ForceRuleLearningCommand;
import com.homesky.homesky.fragments.ruleList.RuleListActivity;
import com.homesky.homesky.homecloud.HomecloudHolder;
import com.homesky.homesky.login.LoginActivity;
import com.homesky.homesky.request.AsyncRequest;
import com.homesky.homesky.request.ModelStorage;
import com.homesky.homesky.request.RequestCallback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RuleFragment extends Fragment implements RequestCallback {
    private static final String TAG = "RuleFragment";

    private final String NODE_EXTRA_NAME = "name";
    private final String NODE_EXTRA_ROOM = "room";

    enum PageState {
        LOADING, REFRESHING, REQUESTING_LEARN, IDLE
    }
    private PageState mPageState;

    private RecyclerView mRecyclerView;
    private RuleAdapter mAdapter;
    private SwipeRefreshLayout mRuleActuatorSwipeRefresh;
    private RelativeLayout mLoadingLayout;
    private TextView mNoInternetTextView, mEmptyTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rule, container, false);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.fragment_rule_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRuleActuatorSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.rule_actuator_swipe_refresh_layout);
        mRuleActuatorSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ModelStorage.getInstance().invalidateNodesCache();
                ModelStorage.getInstance().invalidateRulesCache();
                mPageState = PageState.REFRESHING;
                updateUI();
            }
        });

        mLoadingLayout = (RelativeLayout)view.findViewById(R.id.rule_fragment_loading_panel);
        mLoadingLayout.setVisibility(View.VISIBLE);

        mNoInternetTextView = (TextView)view.findViewById(R.id.rule_fragment_no_internet_text_view);
        mNoInternetTextView.setVisibility(View.GONE);

        mEmptyTextView = (TextView)view.findViewById(R.id.rule_fragment_empty_text_view);
        mEmptyTextView.setVisibility(View.GONE);

        mPageState = PageState.LOADING;
        updateUI();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_rule, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id){
            case R.id.menu_learn_rule:
                mPageState = PageState.REQUESTING_LEARN;
                new AsyncRequest(this).execute(new ForceRuleLearningCommand());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void updateUI(){
        List<Rule> rules = ModelStorage.getInstance().getRules(this);
        List<NodesResponse.Node> nodes = null;
        if(rules != null)
            nodes = ModelStorage.getInstance().getNodes(this);

        if(rules != null && nodes != null) {
            List<NodesResponse.Node> actuators = getActuators(nodes);
            if (mAdapter == null) {
                mAdapter = new RuleAdapter(actuators);

            } else {
                mAdapter.setActuators(actuators);
                mAdapter.notifyDataSetChanged();
            }
            mRecyclerView.setAdapter(mAdapter);

            if(actuators.size() > 0) {
                mRecyclerView.setVisibility(View.VISIBLE);
                mEmptyTextView.setVisibility(View.GONE);
            }
            else {
                mRecyclerView.setVisibility(View.GONE);
                mEmptyTextView.setVisibility(View.VISIBLE);
            }

            if(mRuleActuatorSwipeRefresh.isRefreshing())
                mRuleActuatorSwipeRefresh.setRefreshing(false);
            if(mLoadingLayout.getVisibility() == View.VISIBLE)
                mLoadingLayout.setVisibility(View.GONE);
            mNoInternetTextView.setVisibility(View.GONE);

            mPageState = PageState.IDLE;
        }
    }

    private List<NodesResponse.Node> getActuators(List<NodesResponse.Node> nodes){
        List<NodesResponse.Node> actuators = new ArrayList<>();
        for(NodesResponse.Node n : nodes){
            if(n.getNodeClass().contains(NodeClassEnum.ACTUATOR) && n.getAccepted() == 1)
                actuators.add(n);
        }
        return actuators;
    }

    private List<NodesResponse.Node> getActuatorsWithRules(
            List<NodesResponse.Node> nodes, List<Rule> rules){
        List<NodesResponse.Node> nodesWithRules = new ArrayList<>();
        for(Rule r : rules){
            int nodeId = r.getCommand().getNodeId();
            NodesResponse.Node node = findNodeFromId(nodeId, nodes);
            if(node == null)
                throw new RuntimeException("Found rule with invalid node id");
            if(!nodesWithRules.contains(node))
                nodesWithRules.add(node);
        }
        return nodesWithRules;
    }

    private NodesResponse.Node findNodeFromId(int id, List<NodesResponse.Node> nodes){
        for(NodesResponse.Node n : nodes){
            if(n.getNodeId() == id)
                return n;
        }
        return null;
    }

    @Override
    public void onPostRequest(SimpleResponse s) {
        if (s == null) {
            Toast.makeText(
                    getActivity(),
                    getResources().getText(R.string.login_fragment_server_offline),
                    Toast.LENGTH_LONG).show();
            mPageState = PageState.IDLE;
        }
        else if(s.getStatus() == 200 && (mPageState == PageState.LOADING || mPageState == PageState.REFRESHING)) {
            updateUI();
        }
        else if(s.getStatus() == 200 && mPageState == PageState.REQUESTING_LEARN){
            Toast.makeText(
                    getActivity(),
                    getResources().getText(R.string.rule_fragment_learn_request_sent),
                    Toast.LENGTH_LONG).show();
            mPageState = PageState.IDLE;
        }
        else if(s.getStatus() == 403) {
            HomecloudHolder.getInstance().invalidateSession();
            getActivity().startActivity(LoginActivity.newIntent(getActivity(), LoginActivity.LoginAction.LOGIN));
        }
        else {
            Toast.makeText(
                    getActivity(),
                    getResources().getText(R.string.rule_fragment_no_connection),
                    Toast.LENGTH_LONG).show();
            mLoadingLayout.setVisibility(View.GONE);
            mRuleActuatorSwipeRefresh.setRefreshing(false);
            mNoInternetTextView.setVisibility(View.VISIBLE);
            mEmptyTextView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);

            mPageState = PageState.IDLE;
        }
    }


    class RuleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<Object> mActuators;
        private static final int ITEM_ACTUATOR = 0;
        private static final int ITEM_HEADER = 1;

        public RuleAdapter(List<NodesResponse.Node> actuators) {
            setActuators(actuators);
        }

        public void setActuators(List<NodesResponse.Node> actuators){
            if (actuators == null) return;
            else if (actuators.isEmpty()){
                mActuators = new ArrayList<>();
                return;
            }

            Map<String, List<NodesResponse.Node>> nodesPerRoom = new HashMap<>();

            for (NodesResponse.Node n : actuators) {
                String room = n.getExtra().get(NODE_EXTRA_ROOM);
                if (nodesPerRoom.containsKey(room)) {
                    nodesPerRoom.get(room).add(n);
                } else {
                    List<NodesResponse.Node> list = new LinkedList<>();
                    list.add(n);
                    nodesPerRoom.put(room, list);
                }
            }
            mActuators = new ArrayList<>();
            for (Map.Entry entry : nodesPerRoom.entrySet()) {
                mActuators.add(entry.getKey());
                mActuators.addAll((Collection<?>) entry.getValue());
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (mActuators.get(position) instanceof String) {
                return ITEM_HEADER;
            } else {
                return ITEM_ACTUATOR;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            if (viewType == ITEM_ACTUATOR) {
                View view = layoutInflater.inflate(R.layout.list_item_state, parent, false);
                return new RuleActuatorHolder(view);
            } else if (viewType == ITEM_HEADER) {
                View view = layoutInflater.inflate(R.layout.list_header, parent, false);
                return new HeaderHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int itemType = getItemViewType(position);

            if(itemType == ITEM_ACTUATOR){
                ((RuleActuatorHolder)holder).bindRuleActuator((NodesResponse.Node) mActuators.get(position));
            }
            else if (itemType == ITEM_HEADER) {
                ((HeaderHolder) holder).bind((String) mActuators.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return mActuators.size();
        }
    }

    class RuleActuatorHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        NodesResponse.Node mActuator;

        private TextView mNodeName;
        private ImageView mNodeIcon;

        public RuleActuatorHolder(View itemView) {
            super(itemView);
            mNodeName = (TextView) itemView.findViewById(R.id.state_fragment_node_name);
            mNodeIcon = (ImageView) itemView.findViewById(R.id.list_item_state_icon);
            mNodeIcon.setImageResource(R.drawable.ic_touch_app_black_24dp);

            itemView.setOnClickListener(this);
        }

        public void bindRuleActuator(NodesResponse.Node n){
            mActuator = n;
            mNodeName.setText(mActuator.getExtra().get(NODE_EXTRA_NAME));

        }

        @Override
        public void onClick(View view) {
            Intent i = RuleListActivity.newIntent(getActivity(), mActuator);
            Log.d(TAG, "Node id: " + mActuator.getNodeId());
            startActivity(i);
        }
    }

    class HeaderHolder extends RecyclerView.ViewHolder {

        private TextView mHeaderName;

        HeaderHolder(View itemView) {
            super(itemView);
            mHeaderName = (TextView) itemView.findViewById(R.id.list_item_header);
        }

        void bind(String header) {
            mHeaderName.setText(header);
        }
    }
}
