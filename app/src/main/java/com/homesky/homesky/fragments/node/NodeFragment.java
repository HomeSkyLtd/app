package com.homesky.homesky.fragments.node;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.homesky.homecloud_lib.model.enums.CommandCategoryEnum;
import com.homesky.homecloud_lib.model.enums.DataCategoryEnum;
import com.homesky.homecloud_lib.model.enums.EnumUtil;
import com.homesky.homecloud_lib.model.enums.TypeEnum;
import com.homesky.homecloud_lib.model.response.NodesResponse;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homecloud_lib.model.response.StateResponse;
import com.homesky.homesky.R;
import com.homesky.homesky.command.NewActionCommand;
import com.homesky.homesky.request.AsyncRequest;
import com.homesky.homesky.request.ModelStorage;
import com.homesky.homesky.request.RequestCallback;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

        private static final int ITEM_TYPE_NODE_VALUE = 0;
        private static final int ITEM_TYPE_NODE_TOGGLE = 1;
        private static final int ITEM_TYPE_HEADER = 2;

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

            for (Object o : mNodeList) {
                Log.d(TAG, "setStateAndNode: " + o.toString());
            }

            Log.d(TAG, "setStateAndNode: mMiddle " + mMiddle);

            mNode = node;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            if (viewType == ITEM_TYPE_NODE_VALUE) {
                View view = layoutInflater.inflate(R.layout.node_list_item, parent, false);
                return new ValueHolder(view, mNode.getNodeId(), mNode.getControllerId());
            } else if (viewType == ITEM_TYPE_NODE_TOGGLE) {
                View view = layoutInflater.inflate(R.layout.node_list_item, parent, false);
                return new SwitchHolder(view, mNode.getNodeId(), mNode.getControllerId());
            } else if (viewType == ITEM_TYPE_HEADER) {
                View view = layoutInflater.inflate(R.layout.list_header, parent, false);
                return new HeaderHolder(view);
            }

            return null;
        }

        @Override
        public int getItemViewType(int position) {
            if (mNodeList.get(position) instanceof String)
                return ITEM_TYPE_HEADER;
            else if (position < mMiddle){
                return ITEM_TYPE_NODE_VALUE;
            } else {
                Map.Entry<Integer, BigDecimal> node = (Map.Entry<Integer, BigDecimal>) mNodeList.get(position);
                for (NodesResponse.CommandType commandType : mNode.getCommandType()) {
                    if (commandType.getId() == node.getKey()) {
                        if (commandType.getType() == TypeEnum.BOOL) {
                            return ITEM_TYPE_NODE_TOGGLE;
                        } else break;
                    }
                }
            }

            return ITEM_TYPE_NODE_VALUE;
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
                            ((NodeHolder) holder).bind(
                                    dataType,
                                    node.getValue(),
                                    dataType.getUnit()
                            );
                        }
                    }
                } else {
                    Log.d(TAG, "onBindViewHolder: commandID" + node.getKey());
                    for (NodesResponse.CommandType commandType : mNode.getCommandType()) {
                        if (commandType.getId() == node.getKey()) {
                            ((NodeHolder) holder).bind(
                                    commandType,
                                    node.getValue(),
                                    commandType.getUnit()
                            );
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

    abstract class NodeHolder extends RecyclerView.ViewHolder implements RequestCallback {

        private TextView mTypeIdTextView;
        private TextView mCategory;

        protected int mNodeId;
        protected String mControllerId;
        protected int mTypeId;

        NodeHolder(View itemView, int nodeId, String controllerId) {
            super(itemView);

            mNodeId = nodeId;
            mControllerId = controllerId;

            mCategory = (TextView) itemView.findViewById(R.id.node_list_item_category);
            mTypeIdTextView = (TextView) itemView.findViewById(R.id.node_list_item_id);
        }

        void bind(Object type, BigDecimal value, String unit) {
            if (type instanceof NodesResponse.DataType) {
                NodesResponse.DataType dataType = (NodesResponse.DataType) type;

                mTypeId = dataType.getId();
                mCategory.setText(EnumUtil.getEnumPrettyName(
                        dataType.getDataCategory().getId(), DataCategoryEnum.class));


            } else if (type instanceof NodesResponse.CommandType) {
                NodesResponse.CommandType commandType = (NodesResponse.CommandType) type;

                mTypeId = commandType.getId();
                Log.d(TAG, "bind: command category " + commandType.getCommandCategory().name());
                mCategory.setText(EnumUtil.getEnumPrettyName(
                        commandType.getCommandCategory().getId(), CommandCategoryEnum.class));

            }

            String str_id = "id " + mTypeId;
            mTypeIdTextView.setText(str_id);

            setValue(value, unit);
        }

        protected abstract void setValue(BigDecimal value, String unit);

        @Override
        public void onPostRequest(SimpleResponse s) {
            ModelStorage.getInstance().invalidateNodeStatesCache();
            updateUI();
        }
    }

    class ValueHolder extends NodeHolder {

        private TextView mValue;

        ValueHolder(View itemView, int nodeId, String controllerId) {
            super(itemView, nodeId, controllerId);

            mValue = (TextView) itemView.findViewById(R.id.node_list_item_value);
            mValue.setVisibility(View.VISIBLE);
        }

        @Override
        protected void setValue(BigDecimal value, String unit) {
            String str_value = value.toEngineeringString() + " " + unit;
            mValue.setText(str_value);
        }
    }

    class SwitchHolder extends NodeHolder implements CompoundButton.OnCheckedChangeListener {

        private Switch mSwitch;

        SwitchHolder(View itemView, int nodeId, String controllerId) {
            super(itemView, nodeId, controllerId);

            mSwitch = (Switch) itemView.findViewById(R.id.node_list_switch);
            mSwitch.setVisibility(View.VISIBLE);
            mSwitch.setOnCheckedChangeListener(this);
        }

        @Override
        protected void setValue(BigDecimal value, String unit) {
            mSwitch.setChecked(value.intValue() == 1);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            BigDecimal value = new BigDecimal(isChecked ? 1 : 0);
            new AsyncRequest(this).execute(new NewActionCommand(mNodeId, mControllerId, mTypeId, value));
        }
    }

    class HeaderHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;

        HeaderHolder(View itemView) {
            super(itemView);

            mTitle = (TextView) itemView.findViewById(R.id.list_item_header);
        }

        void bind(String title) {
            mTitle.setText(title);
        }
    }

    public static class NewActionDialogFragment extends DialogFragment {

    }

}
