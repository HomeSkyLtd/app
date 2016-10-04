package com.homesky.homesky.fragments.node;

import android.content.Context;
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

import com.homesky.homecloud_lib.model.response.NodesResponse;
import com.homesky.homecloud_lib.model.response.StateResponse;
import com.homesky.homesky.R;
import com.homesky.homesky.fragments.state.StateFragment;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by henrique on 10/3/16.
 */

public class NodeFragment extends Fragment {

    private static final String TAG = "NodeFragment";

    private NodesResponse.Node mNode;
    private StateResponse.NodeState mNodeState;
    private RecyclerView mRecyclerView;
    private HashMap<NodesResponse.Node, StateResponse.NodeState> mNodeToValue;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int nodeId = getActivity().getIntent().getIntExtra(NodeActivity.EXTRA_NODE_ID, 0);
        String controllerId = getActivity().getIntent().getStringExtra(NodeActivity.EXTRA_CONTROLLER_ID);
        mNodeToValue = StateFragment.getNodeIdToValue();

        for (NodesResponse.Node node : mNodeToValue.keySet()) {
            if (node.getNodeId() == nodeId && node.getControllerId().equals(controllerId)) {
                mNode = node;
                break;
            }
        }
        mNodeState = mNodeToValue.get(mNode);

        Log.d(TAG, "mNode: " + (mNode == null));
        Log.d(TAG, "mNodeState: " + (mNodeState == null));
        Log.d(TAG, "mNodeToValue: " + mNodeToValue.toString());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_node, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_node_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ItemAdapter adapter = new ItemAdapter(mNodeState, mNode);
        mRecyclerView.setAdapter(adapter);

        return view;
    }












    /* ITEM ADAPTER */

    class ItemAdapter extends RecyclerView.Adapter<ItemHolder> {

        private List<Map.Entry<Integer, BigDecimal>> mNodeData;
        private List<Map.Entry<Integer, BigDecimal>> mNodeCommand;
        private NodesResponse.Node mNode;

        ItemAdapter(StateResponse.NodeState nodeState, NodesResponse.Node node) {
            mNodeData = new LinkedList<>();
            mNodeCommand = new LinkedList<>();

            if (nodeState != null) {
                mNodeData.addAll(nodeState.getData().entrySet());
                mNodeCommand.addAll(nodeState.getCommand().entrySet());
            }

            mNode = node;
        }

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.node_list_item, parent, false);
            return new ItemHolder(view);
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            if (position < mNodeData.size()) {
                Map.Entry<Integer, BigDecimal> node = mNodeData.get(position);
                for (NodesResponse.DataType dataType : mNode.getDataType()) {
                    if (dataType.getId() == node.getKey()) {
                        holder.bind(node.getKey(), node.getValue(), dataType.getUnit());
                        break;
                    }
                }
            } else {
                Map.Entry<Integer, BigDecimal> node = mNodeCommand.get(position - mNodeData.size());
                for (NodesResponse.CommandType commandType : mNode.getCommandType()) {
                    if (commandType.getId() == node.getKey()) {
                        holder.bind(node.getKey(), node.getValue(), commandType.getUnit());
                        break;
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            return mNodeData.size() + mNodeCommand.size();
        }
    }

    class ItemHolder extends RecyclerView.ViewHolder {

        private TextView mId;
        private TextView mValue;

        ItemHolder(View itemView) {
            super(itemView);

            mId = (TextView) itemView.findViewById(R.id.node_list_item_id);
            mValue = (TextView) itemView.findViewById(R.id.node_list_item_value);
        }

        void bind(Integer nodeId, BigDecimal value, String unit) {
            mId.setText(nodeId);

            String str_value = value.toEngineeringString() + unit;
            mValue.setText(str_value);
        }
    }

}
