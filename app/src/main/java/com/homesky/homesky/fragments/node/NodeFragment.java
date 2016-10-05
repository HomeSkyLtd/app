package com.homesky.homesky.fragments.node;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.homesky.homecloud_lib.model.response.NodesResponse;
import com.homesky.homecloud_lib.model.response.StateResponse;
import com.homesky.homesky.R;
import com.homesky.homesky.request.ModelStorage;

import java.math.BigDecimal;
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int nodeId = getActivity().getIntent().getIntExtra(NodeActivity.EXTRA_NODE_ID, 0);
        String controllerId = getActivity().getIntent().getStringExtra(NodeActivity.EXTRA_CONTROLLER_ID);

        for (NodesResponse.Node node : ModelStorage.getInstance().getNodeIdToValue(false).keySet()) {
            if (node.getNodeId() == nodeId && node.getControllerId().equals(controllerId)) {
                mNode = node;
                break;
            }
        }
        mNodeState = ModelStorage.getInstance().getNodeIdToValue(false).get(mNode);
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

    class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int ITEM_TYPE_NODE = 0;
        private static final int ITEM_TYPE_HEADER = 1;

        private List<Map.Entry<Integer, BigDecimal>> mNodeData;
        private List<Map.Entry<Integer, BigDecimal>> mNodeCommand;
        private List<Object> mNodes;
        private NodesResponse.Node mNode;
        private int mMiddle;

        ItemAdapter(StateResponse.NodeState nodeState, NodesResponse.Node node) {
            //mNodeData = new LinkedList<>();
            //mNodeCommand = new LinkedList<>();
            mNodes = new LinkedList<>();

            if (nodeState != null) {
                //mNodeData.addAll(nodeState.getData().entrySet());
                //mNodeCommand.addAll(nodeState.getCommand().entrySet());
                mMiddle = 1;

                if (!nodeState.getData().isEmpty()) {
                    mNodes.add(getActivity().getResources().getString(R.string.node_fragment_list_div_sensor));
                    mNodes.addAll(nodeState.getData().entrySet());
                    mMiddle = 1 + mNodeData.size();
                }

                if (!nodeState.getCommand().isEmpty()) {
                    mNodes.add(getActivity().getResources().getString(R.string.node_fragment_list_div_actuator));
                    mNodes.addAll(nodeState.getCommand().entrySet());
                }
            }

            mNode = node;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            if (viewType == ITEM_TYPE_NODE) {
                View view = layoutInflater.inflate(R.layout.node_list_item, parent, false);
                return new NodeHolder(view);
            } else if (viewType == ITEM_TYPE_HEADER) {
                View view = layoutInflater.inflate(R.layout.node_list_item_header, parent, false);
                return new HeaderHolder(view);
            }

            return null;
        }

        @Override
        public int getItemViewType(int position) {
            if (mNodes.get(position) instanceof String)
                return ITEM_TYPE_HEADER;
            else
                return ITEM_TYPE_NODE;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int itemType = getItemViewType(position);

            if (itemType == ITEM_TYPE_HEADER) {
                ((HeaderHolder) holder).bind((String) mNodes.get(position));
            } else {
                Map.Entry<Integer, BigDecimal> node = (Map.Entry<Integer, BigDecimal>) mNodes.get(position);
                if (position < mMiddle) {
                    for (NodesResponse.DataType dataType : mNode.getDataType()) {
                        if (dataType.getId() == node.getKey()) {
                            ((NodeHolder) holder).bind(node.getKey(), node.getValue(), dataType.getUnit());
                        }
                    }
                } else {
                    for (NodesResponse.CommandType commandType : mNode.getCommandType()) {
                        if (commandType.getId() == node.getKey()) {
                            ((NodeHolder) holder).bind(node.getKey(), node.getValue(), commandType.getUnit());
                        }
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            return mNodes.size();
        }
    }

    class NodeHolder extends RecyclerView.ViewHolder {

        private TextView mId;
        private TextView mValue;

        NodeHolder(View itemView) {
            super(itemView);

            mId = (TextView) itemView.findViewById(R.id.node_list_item_id);
            mValue = (TextView) itemView.findViewById(R.id.node_list_item_value);
        }

        void bind(int nodeId, BigDecimal value, String unit) {
            mId.setText(String.valueOf(nodeId));

            String str_value = value.toEngineeringString() + " " + unit;
            mValue.setText(str_value);
        }
    }

    class HeaderHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;

        HeaderHolder(View itemView) {
            super(itemView);

            mTitle = (TextView) itemView.findViewById(R.id.node_list_item_header);
        }

        void bind(String title) {
            mTitle.setText(title);
        }
    }

}
