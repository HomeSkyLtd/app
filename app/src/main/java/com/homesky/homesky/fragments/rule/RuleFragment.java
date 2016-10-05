package com.homesky.homesky.fragments.rule;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.homesky.homecloud_lib.model.Rule;
import com.homesky.homecloud_lib.model.response.NodesResponse;
import com.homesky.homecloud_lib.model.response.RuleResponse;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homecloud_lib.model.response.StateResponse;
import com.homesky.homesky.R;
import com.homesky.homesky.command.GetHouseStateCommand;
import com.homesky.homesky.request.AsyncRequest;
import com.homesky.homesky.request.ModelStorage;
import com.homesky.homesky.request.RequestCallback;

import java.util.ArrayList;
import java.util.List;

public class RuleFragment extends Fragment implements RequestCallback {
    private static final String TAG = "RuleFragment";

    private RecyclerView mRecyclerView;
    private RuleAdapter mAdapter;
    private SwipeRefreshLayout mRuleActuatorSwipeRefresh;

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
                updateUI();
                mRuleActuatorSwipeRefresh.setRefreshing(false);
            }
        });

        updateUI();
        return view;
    }

    private void updateUI(){
        List<Rule> rules = ModelStorage.getInstance().getRules(this);
        List<NodesResponse.Node> nodes = null;
        if(rules != null)
            nodes = ModelStorage.getInstance().getNodes(this);

        if(rules != null && nodes != null) {
            List<NodesResponse.Node> actuators = getActuatorsWithRules(nodes, rules);
            if (mAdapter == null) {
                mAdapter = new RuleAdapter(actuators);
                mRecyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.setRules(actuators);
                mAdapter.notifyDataSetChanged();
            }
        }
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
        } else {
            updateUI();
        }
    }


    class RuleAdapter extends RecyclerView.Adapter<RuleActuatorHolder> {
        private List<NodesResponse.Node> mActuators;

        public RuleAdapter(List<NodesResponse.Node> actuators) {
            mActuators = actuators;
        }

        public void setRules(List<NodesResponse.Node> actuators){
            mActuators = actuators;
        }

        @Override
        public RuleActuatorHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_rule_actuator_item, parent, false);

            return new RuleActuatorHolder(view);
        }

        @Override
        public void onBindViewHolder(RuleActuatorHolder holder, int position) {
            holder.bindRuleActuator(mActuators.get(position));
        }

        @Override
        public int getItemCount() {
            return mActuators.size();
        }
    }

    class RuleActuatorHolder extends RecyclerView.ViewHolder {
        NodesResponse.Node mActuator;

        TextView mId, mName, mRoom;

        public RuleActuatorHolder(View itemView) {
            super(itemView);
            mId = (TextView)itemView.findViewById(R.id.rule_node_id_text_view);
            mName = (TextView)itemView.findViewById(R.id.rule_node_name_text_view);
            mRoom = (TextView)itemView.findViewById(R.id.rule_node_room_text_view);
        }

        public void bindRuleActuator(NodesResponse.Node n){
            mActuator = n;
            mId.setText(Integer.toString(n.getNodeId()));
            mName.setText(mActuator.getExtra().get("name"));
            mRoom.setText(mActuator.getExtra().get("room"));
        }
    }
}
