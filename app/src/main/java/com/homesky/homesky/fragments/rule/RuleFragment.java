package com.homesky.homesky.fragments.rule;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.homesky.homecloud_lib.model.Rule;
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rule, container, false);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.fragment_rule_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();
        return view;
    }

    private void updateUI(){
        List<Rule> rules = ModelStorage.getInstance().getRules(this, false);

        if(rules != null) {
            if (mAdapter == null) {
                mAdapter = new RuleAdapter(rules);
                mRecyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.setRules(rules);
                mAdapter.notifyDataSetChanged();
            }
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


    class RuleAdapter extends RecyclerView.Adapter<RuleActuatorHolder> {
        private List<Rule> mRules;

        public RuleAdapter(List<Rule> rules) {
            mRules = rules;
        }

        public void setRules(List<Rule> rules){
            mRules = rules;
        }

        @Override
        public RuleActuatorHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_rule_actuator_item, parent, false);

            return new RuleActuatorHolder(view);
        }

        @Override
        public void onBindViewHolder(RuleActuatorHolder holder, int position) {
            holder.bindRuleActuator(mRules.get(position));
        }

        @Override
        public int getItemCount() {
            return mRules.size();
        }
    }

    class RuleActuatorHolder extends RecyclerView.ViewHolder {
        Rule mRule;

        TextView mId, mName, mRoom;

        public RuleActuatorHolder(View itemView) {
            super(itemView);
            mId = (TextView)itemView.findViewById(R.id.rule_node_id_text_view);
            mName = (TextView)itemView.findViewById(R.id.rule_node_name_text_view);
            mRoom = (TextView)itemView.findViewById(R.id.rule_node_room_text_view);
        }

        public void bindRuleActuator(Rule r){
            mRule = r;
            mId.setText(Integer.toString(r.getCommand().getNodeId()));
        }
    }
}
