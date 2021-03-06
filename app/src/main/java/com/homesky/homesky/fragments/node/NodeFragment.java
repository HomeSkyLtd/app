package com.homesky.homesky.fragments.node;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.homesky.homesky.activities.SingleFragmentActivity;
import com.homesky.homesky.command.NewActionCommand;
import com.homesky.homesky.fragments.notification.FirebaseBackgroundService;
import com.homesky.homesky.login.LoginActivity;
import com.homesky.homesky.request.AsyncRequest;
import com.homesky.homesky.request.ModelStorage;
import com.homesky.homesky.request.RequestCallback;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NodeFragment extends Fragment {

    private static final String TAG = "NodeFragment";

    private NodesResponse.Node mNode;
    private StateResponse.NodeState mNodeState;
    private RecyclerView mRecyclerView;
    private ItemAdapter mAdapter;

    protected int mNodeId;
    protected String mControllerId;
    protected SwipeRefreshLayout mNodeSwipeRefresh;
    private RelativeLayout mLoadingPanel;

    private int mAttemptsCounter = 0;
    private static final int sNumberOfAttempts = 3;

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

        mLoadingPanel = (RelativeLayout) view.findViewById(R.id.node_fragment_loading_panel);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_node_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mNodeSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.node_fragment_swipe_refresh_layout);
        mNodeSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ((NodeActivity) getActivity()).lockActivity(true, "Wait a second, fetching state...");
                ModelStorage.getInstance().invalidateNodeStatesCache();
                mAttemptsCounter = 0;
                updateUI();
            }
        });

        updateUI();

        return view;
    }

    private void setNodeAndState (boolean forceSync) {
        if (ModelStorage.getInstance().getNodeIdToValue(forceSync) == null) {
            mAttemptsCounter++;
            return;
        }

        for (NodesResponse.Node node : ModelStorage.getInstance().getNodeIdToValue(false).keySet()) {
            if (node.getNodeId() == mNodeId && node.getControllerId().equals(mControllerId)) {
                mNode = node;
                break;
            }
        }
        mNodeState = ModelStorage.getInstance().getNodeIdToValue(false).get(mNode);
    }

    private void updateUI() {
        if(getActivity() == null) return;

        if (mAttemptsCounter > sNumberOfAttempts) {
            mAttemptsCounter = 0;
            mLoadingPanel.setVisibility(View.GONE);
            mNodeSwipeRefresh.setRefreshing(false);
            ((NodeActivity) getActivity()).lockActivity(false, null);
            Snackbar.make(
                    getActivity().findViewById(R.id.fragment_container),
                    getActivity().getResources().getText(R.string.node_fragment_no_connection),
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        List<StateResponse.NodeState> list = ModelStorage.getInstance().getNodeStates(new GetHouseStateRequest());
        if (list != null) {
            mLoadingPanel.setVisibility(View.GONE);
            setNodeAndState(false);
        } else {
            mLoadingPanel.setVisibility(View.VISIBLE);
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
            if(!NodeFragment.this.isAdded()) return;


            if (s == null) {
                Toast.makeText(
                        getActivity(),
                        getResources().getText(R.string.state_fragment_error),
                        Toast.LENGTH_LONG).show();
            } else if (s.getStatus() == 0 && s.getErrorMessage().equals(AsyncRequest.NOT_CREDENTIALS_ERROR)) {
                getActivity().startActivity(LoginActivity.newIntent(getActivity(), LoginActivity.LoginAction.LOGIN));
            } else {
                setNodeAndState(true);

                if (getActivity() != null) {
                    updateUI();
                    mNodeSwipeRefresh.setRefreshing(false);
                    ((NodeActivity) getActivity()).lockActivity(false, null);
                }
            }
        }
    }












    /* ITEM ADAPTER */

    class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int ITEM_TYPE_DATA_VALUE = 0;
        private static final int ITEM_TYPE_COMMAND_VALUE = 1;
        private static final int ITEM_TYPE_NODE_TOGGLE = 2;
        private static final int ITEM_TYPE_HEADER = 3;

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

                if (!nodeState.getData().isEmpty() && getActivity() != null) {
                    mNodeList.add(getActivity().getResources().getString(R.string.node_fragment_list_div_sensor));
                    mNodeList.addAll(nodeState.getData().entrySet());
                    mMiddle = 1 + nodeState.getData().size();
                }

                if (!nodeState.getCommand().isEmpty() && getActivity() != null) {
                    mNodeList.add(getActivity().getResources().getString(R.string.node_fragment_list_div_actuator));
                    mNodeList.addAll(nodeState.getCommand().entrySet());
                }
            }

            mNode = node;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            if (viewType == ITEM_TYPE_DATA_VALUE || viewType == ITEM_TYPE_COMMAND_VALUE) {
                View view = layoutInflater.inflate(R.layout.node_list_item, parent, false);
                return new ValueHolder(view, mNode, viewType);
            } else if (viewType == ITEM_TYPE_NODE_TOGGLE) {
                View view = layoutInflater.inflate(R.layout.node_list_item, parent, false);
                return new SwitchHolder(view, mNode);
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
                return ITEM_TYPE_DATA_VALUE;
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

            return ITEM_TYPE_COMMAND_VALUE;
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
                                    node.getValue()
                            );
                        }
                    }
                } else {
                    for (NodesResponse.CommandType commandType : mNode.getCommandType()) {
                        if (commandType.getId() == node.getKey()) {
                            ((NodeHolder) holder).bind(
                                    commandType,
                                    node.getValue()
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

        private TextView mCategory;
        RelativeLayout mProgressBar;
        private ImageView mIcon;
        private TextView mDiv;

        private View mItemView;
        BigDecimal mValue;
        BigDecimal mOldValue;

        NodesResponse.Node mNode;
        NodesResponse.Type mType;

        NodeHolder(View itemView, NodesResponse.Node node) {
            super(itemView);

            mNode = node;

            mCategory = (TextView) itemView.findViewById(R.id.node_list_item_category);
            mProgressBar = (RelativeLayout) itemView.findViewById(R.id.node_list_item_loading_panel);
            mIcon = (ImageView) itemView.findViewById(R.id.node_list_item_icon);
            mDiv = (TextView) itemView.findViewById(R.id.list_header_div);

            mItemView = itemView;
        }

        void setClickable(boolean clickable) {
            mItemView.setClickable(clickable);
        }

        void bind(NodesResponse.Type type, BigDecimal value) {
            mType = type;

            if (type instanceof NodesResponse.DataType) {
                NodesResponse.DataType dataType = (NodesResponse.DataType) type;

                mCategory.setText(EnumUtil.getEnumPrettyName(
                        dataType.getDataCategory().getId(), DataCategoryEnum.class));

                mIcon.setVisibility(View.GONE);
                mDiv.setVisibility(View.GONE);

            } else if (type instanceof NodesResponse.CommandType) {
                NodesResponse.CommandType commandType = (NodesResponse.CommandType) type;

                mCategory.setText(EnumUtil.getEnumPrettyName(
                        commandType.getCommandCategory().getId(), CommandCategoryEnum.class));

                mIcon.setVisibility(View.VISIBLE);
            }

            setValue(value);
        }

        protected abstract void setValue(BigDecimal valueTextView);

        protected void setLoading(boolean loading, boolean success) {
            if (success) {
                mNodeState.getCommand().put(mType.getId(), mValue);
            } else {
                mValue = mOldValue;
                setValue(mValue);
            }
        }

        void setValueLoading(BigDecimal value) {
            if (value == null) return;

            mOldValue = mValue;
            mValue = value;
            mProgressBar.setVisibility(View.VISIBLE);
            setValue(value);
            setLoading(true, true);
        }

        @Override
        public void onPostRequest(SimpleResponse s) {
            if(!NodeFragment.this.isAdded()) return;

            Log.d(TAG, s.getStatus() + "");

            if (s.getStatus() != 200) {
                String msg = getActivity().getResources().getString(R.string.login_fragment_connection_failed);

                if (s.getStatus() == 403) {
                    msg = getActivity().getResources().getString(R.string.node_fragment_not_logged);
                }

                Snackbar.make(
                        getActivity().findViewById(R.id.fragment_container),
                        msg,
                        Snackbar.LENGTH_LONG).show();
            }

            setLoading(false, s.getStatus() == 200);
            mProgressBar.setVisibility(View.GONE);
            setClickable(true);
            ((NodeActivity) getActivity()).lockActivity(false, null);
        }
    }

    class ValueHolder extends NodeHolder implements View.OnClickListener {

        private TextView mValueTextView;

        private static final String DIALOG_TAG = "value_holder_dialog_tag";

        ValueHolder(View itemView, NodesResponse.Node node, int viewType) {
            super(itemView, node);

            mValueTextView = (TextView) itemView.findViewById(R.id.node_list_item_value);
            mValueTextView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);

            if (viewType == ItemAdapter.ITEM_TYPE_COMMAND_VALUE) {
                itemView.setOnClickListener(this);
            }
        }

        protected void setValue(BigDecimal value) {
            if (value == null) return;

            if (mType.getType() == TypeEnum.BOOL) {
                int intValue = value.intValue();

                switch (intValue) {
                    case 0:
                        mValueTextView.setText(R.string.no);
                        break;
                    case 1:
                        mValueTextView.setText(R.string.yes);
                        break;
                }

            } else {
                String str_value = value.toEngineeringString() + " " + mType.getUnit();
                mValueTextView.setText(str_value);
            }

            mValue = value;
        }

        @Override
        public void setLoading(boolean loading, boolean success) {
            if (loading) {
                mValueTextView.setVisibility(View.GONE);
            } else {
                mValueTextView.setVisibility(View.VISIBLE);
                super.setLoading(false, success);
            }
        }


        @Override
        public void onClick(View v) {
            NewActionDialogFragment.newInstance(this, mType).show(getFragmentManager(), DIALOG_TAG);
        }
    }

    class SwitchHolder extends NodeHolder implements View.OnClickListener {

        private Switch mSwitch;

        SwitchHolder(View itemView, NodesResponse.Node node) {
            super(itemView, node);

            mSwitch = (Switch) itemView.findViewById(R.id.node_list_switch);
            mSwitch.setVisibility(View.VISIBLE);
            mSwitch.setOnClickListener(this);
        }

        protected void setValue(BigDecimal value) {
            if (value == null) return;

            mValue = value;
            mSwitch.setChecked(value.intValue() == 1);
        }

        @Override
        protected void setLoading(boolean loading, boolean success) {if (loading) {
                mSwitch.setVisibility(View.GONE);
            } else {
                mSwitch.setVisibility(View.VISIBLE);
                super.setLoading(false, success);
            }
        }



        @Override
        public void onClick(View v) {
            BigDecimal value = new BigDecimal(mSwitch.isChecked() ? 1 : 0);
            setValue(value);

            new AsyncRequest(this).execute(new NewActionCommand(mNodeId, mControllerId, mType.getId(), value));
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

    public static class NewActionDialogFragment extends DialogFragment implements View.OnClickListener {

        private TextView mValueTextView;
        private NodeHolder mNodeHolder;
        private NodesResponse.Type mType;

        void setNodeHolder(NodeHolder nodeHolder) {
            this.mNodeHolder = nodeHolder;
        }

        public void setType(NodesResponse.Type type) {
            mType = type;
        }

        static NewActionDialogFragment newInstance(NodeHolder nodeHolder, NodesResponse.Type type) {
            NewActionDialogFragment fragment = new NewActionDialogFragment();

            fragment.setNodeHolder(nodeHolder);
            fragment.setType(type);

            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
            return super.onCreateDialog(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.node_dialog_fragment, container, false);

            getDialog().setCanceledOnTouchOutside(true);
            if (getDialog().getWindow() != null) {
                getDialog().getWindow().setBackgroundDrawableResource(R.drawable.round_dialog);
            }

            Button sendValue = (Button) view.findViewById(R.id.node_dialog_fragment_button);
            sendValue.setOnClickListener(this);

            mValueTextView = (TextView) view.findViewById(R.id.node_dialog_fragment_edit_text);

            if (mType.getType() == TypeEnum.INT)
                mValueTextView.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED);
            else if (mType.getType() == TypeEnum.REAL)
                mValueTextView.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED);

            return view;
        }


        @Override
        public void onClick(View v) {
            String str_value = mValueTextView.getText().toString();

            if (str_value.isEmpty()) {
                Snackbar.make(
                        getActivity().findViewById(R.id.fragment_container),
                        getResources().getString(R.string.send_action_dialog_null_value),
                        Snackbar.LENGTH_SHORT)
                        .show();
            } else {
                BigDecimal value =  new BigDecimal(str_value);

                if (value.compareTo(mType.getRange()[0]) < 0 || value.compareTo(mType.getRange()[1]) > 0) {
                    Toast.makeText(getActivity(),
                            getResources().getString(
                                    R.string.send_action_dialog_out_of_bounds_value,
                                    mType.getRange()[0].toString(),
                                    mType.getRange()[1].toString()), Toast.LENGTH_LONG).show();
                } else {
                    mNodeHolder.setValueLoading(value);
                    mNodeHolder.setClickable(false);
                    ((NodeActivity) getActivity()).lockActivity(true, "Wait a second, sending action...");
                    new AsyncRequest(mNodeHolder)
                            .execute(new NewActionCommand(
                                    mNodeHolder.mNode.getNodeId(),
                                    mNodeHolder.mNode.getControllerId(),
                                    mType.getId(),
                                    value));
                    getDialog().cancel();
                }
            }
        }
    }

}