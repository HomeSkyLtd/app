package com.homesky.homesky.fragments.node;

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
import android.widget.Toast;

import com.homesky.homecloud_lib.model.enums.CommandCategoryEnum;
import com.homesky.homecloud_lib.model.enums.DataCategoryEnum;
import com.homesky.homecloud_lib.model.enums.EnumUtil;
import com.homesky.homecloud_lib.model.enums.SingleValueEnum;
import com.homesky.homecloud_lib.model.response.NodesResponse;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homecloud_lib.model.response.StateResponse;
import com.homesky.homesky.R;
import com.homesky.homesky.fragments.state.StateFragment;
import com.homesky.homesky.request.ModelStorage;
import com.homesky.homesky.request.RequestCallback;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    private ItemAdapter mAdapter;

    private int mNodeId;
    private String mControllerId;
    private SwipeRefreshLayout mNodeSwipeRefresh;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNodeId = getActivity().getIntent().getIntExtra(NodeActivity.EXTRA_NODE_ID, 0);
        mControllerId = getActivity().getIntent().getStringExtra(NodeActivity.EXTRA_CONTROLLER_ID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_node, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_node_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mNodeSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.node_fragment_swipe_refresh_layout);
        mNodeSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ModelStorage.getInstance().invalidateNodeStatesCache();
                updateUI();
            }
        });

        updateUI();

        return view;
    }

    private void setNodeAndState (boolean forceSync) {
        for (NodesResponse.Node node : ModelStorage.getInstance().getNodeIdToValue(forceSync).keySet()) {
            if (node.getNodeId() == mNodeId && node.getControllerId().equals(mControllerId)) {
                mNode = node;
                break;
            }
        }
        mNodeState = ModelStorage.getInstance().getNodeIdToValue(false).get(mNode);
    }

    private void updateUI() {
        List<StateResponse.NodeState> list = ModelStorage.getInstance().getNodeStates(new GetHouseStateRequest());
        if (list != null) {
            setNodeAndState(false);
        }

        if (mAdapter == null) {
            mAdapter = new ItemAdapter(mNodeState, mNode);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setStateAndNode(mNodeState, mNode);
            mAdapter.notifyDataSetChanged();
        }
    }

    class GetHouseStateRequest implements RequestCallback {
        @Override
        public void onPostRequest(SimpleResponse s) {
            if (s == null) {
                Toast.makeText(
                        getActivity(),
                        getResources().getText(R.string.state_fragment_error),
                        Toast.LENGTH_LONG).show();
            } else {
                setNodeAndState(true);
                updateUI();
                mNodeSwipeRefresh.setRefreshing(false);
            }
        }
    }












    /* ITEM ADAPTER */

    class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int ITEM_TYPE_NODE = 0;
        private static final int ITEM_TYPE_HEADER = 1;

        private List<Object> mNodeList;
        private NodesResponse.Node mNode;
        private int mMiddle;

        ItemAdapter(StateResponse.NodeState nodeState, NodesResponse.Node node) {
            mNodeList = new ArrayList<>();
            setStateAndNode(nodeState, node);
        }

        void setStateAndNode(StateResponse.NodeState nodeState, NodesResponse.Node node) {
            mNodeList.clear();

            if (nodeState != null) {
                mMiddle = 1;

                if (!nodeState.getData().isEmpty()) {
                    mNodeList.add(getActivity().getResources().getString(R.string.node_fragment_list_div_sensor));
                    mNodeList.addAll(nodeState.getData().entrySet());
                    mMiddle = 1 + nodeState.getData().size();
                }

                if (!nodeState.getCommand().isEmpty()) {
                    mNodeList.add(getActivity().getResources().getString(R.string.node_fragment_list_div_actuator));
                    mNodeList.addAll(nodeState.getCommand().entrySet());
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
            if (mNodeList.get(position) instanceof String)
                return ITEM_TYPE_HEADER;
            else
                return ITEM_TYPE_NODE;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int itemType = getItemViewType(position);

            if (itemType == ITEM_TYPE_HEADER) {
                ((HeaderHolder) holder).bind((String) mNodeList.get(position));
            } else {
                Map.Entry<Integer, BigDecimal> node = (Map.Entry<Integer, BigDecimal>) mNodeList.get(position);
                if (position < mMiddle) {
                    for (NodesResponse.DataType dataType : mNode.getDataType()) {
                        if (dataType.getId() == node.getKey()) {
                            ((NodeHolder) holder).bind(dataType, node.getValue(), dataType.getUnit());
                        }
                    }
                } else {
                    for (NodesResponse.CommandType commandType : mNode.getCommandType()) {
                        if (commandType.getId() == node.getKey()) {
                            ((NodeHolder) holder).bind(commandType, node.getValue(), commandType.getUnit());
                        }
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            return mNodeList.size();
        }
    }

    class NodeHolder extends RecyclerView.ViewHolder {

        private TextView mId;
        private TextView mCategory;
        private TextView mValue;

        NodeHolder(View itemView) {
            super(itemView);

            mCategory = (TextView) itemView.findViewById(R.id.node_list_item_category);
            mId = (TextView) itemView.findViewById(R.id.node_list_item_id);
            mValue = (TextView) itemView.findViewById(R.id.node_list_item_value);
        }

        void bind(Object type, BigDecimal value, String unit) {
            int id = 0;

            if (type instanceof NodesResponse.DataType) {
                NodesResponse.DataType dataType = (NodesResponse.DataType) type;
                id = dataType.getId();
                mCategory.setText(EnumUtil.getEnumPrettyName(id, DataCategoryEnum.class));
            } else if (type instanceof NodesResponse.CommandType) {
                NodesResponse.CommandType commandType = (NodesResponse.CommandType) type;
                id = commandType.getId();
                mCategory.setText(EnumUtil.getEnumPrettyName(id, CommandCategoryEnum.class));
            }

            String str_id = "id " + id;
            mId.setText(str_id);

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
