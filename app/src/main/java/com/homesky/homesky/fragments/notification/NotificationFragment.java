package com.homesky.homesky.fragments.notification;

import android.app.Dialog;
import android.content.Context;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.homesky.homecloud_lib.model.Rule;
import com.homesky.homecloud_lib.model.enums.CommandCategoryEnum;
import com.homesky.homecloud_lib.model.enums.DataCategoryEnum;
import com.homesky.homecloud_lib.model.enums.EnumUtil;
import com.homesky.homecloud_lib.model.enums.TypeEnum;
import com.homesky.homecloud_lib.model.notification.Notification;
import com.homesky.homecloud_lib.model.response.NodesResponse;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homesky.R;
import com.homesky.homesky.command.AcceptNodeCommand;
import com.homesky.homesky.command.AcceptRuleCommand;
import com.homesky.homesky.fragments.state.StateFragment;
import com.homesky.homesky.login.LoginActivity;
import com.homesky.homesky.request.AsyncRequest;
import com.homesky.homesky.request.ModelStorage;
import com.homesky.homesky.request.RequestCallback;
import com.homesky.homesky.utils.AppStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by henrique on 9/22/16.
 */
public class NotificationFragment extends Fragment {

    private static final String TAG = "NotificationFragment";

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mStateSwipeRefresh;
    private RelativeLayout mLoadingPanel;
    private NotificationAdapter mAdapter;

    private static final String NODE_MAP_NAME = "name";
    private static final String NODE_MAP_ROOM = "room";

    public NotificationFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        mLoadingPanel = (RelativeLayout) view.findViewById(R.id.notification_fragment_loading_panel);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_notification_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mStateSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.notification_fragment_swipe_refresh_layout);
        mStateSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ModelStorage.getInstance().invalidateNodesCache();
                ModelStorage.getInstance().invalidateLearntRulesCache();
                updateUI();
                mStateSwipeRefresh.setRefreshing(false);
            }
        });

        updateUI();

        return view;
    }

    private void updateUI() {
        mAdapter = new NotificationAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setNodes();
    }

    private class NotificationHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private static final String DIALOG_TAG = "state_holder_dialog_tag";

        private NodesResponse.Node mNode;
        private Rule mRule;
        private TextView mName, mRoom;
        private ImageButton mDeny, mAccept;

        private class NodeClickListener implements View.OnClickListener {
            int mAccept;
            NodeClickListener(int accept) { mAccept = accept; }
            @Override
            public void onClick(View v) {
                new AsyncRequest(new AcceptCallback(mNode, mRule))
                        .execute(new AcceptNodeCommand(mNode.getNodeId(),mNode.getControllerId(),mAccept));
            }
        }

        private class RuleClickListener implements View.OnClickListener {
            int mAccept;
            RuleClickListener(int accept) { mAccept = accept; }
            @Override
            public void onClick(View v) {
                Rule.Command c = mRule.getCommand();
                new AsyncRequest(new AcceptCallback(mNode, mRule))
                        .execute(new AcceptRuleCommand(
                                c.getNodeId(), c.getCommandId(), mNode.getControllerId(),
                                c.getValue(), mAccept));
            }
        }

        NotificationHolder(View itemView) {
            super(itemView);

            mName = (TextView) itemView.findViewById(R.id.list_notification_node_name);
            mRoom = (TextView) itemView.findViewById(R.id.list_notification_room_name);

            mAccept = (ImageButton) itemView.findViewById(R.id.list_notification_accept_button);
            mDeny = (ImageButton) itemView.findViewById(R.id.list_notification_deny_button);

            itemView.setOnClickListener(this);
        }

        void bind(Object o) {

            if (o instanceof NodesResponse.Node) {
                mRule = null;
                mNode = (NodesResponse.Node) o;

                String str = "A " + mNode.getExtra().get(NODE_MAP_NAME) + " detected";
                mName.setText(str);
                mRoom.setText(mNode.getExtra().get(NODE_MAP_ROOM));

                mAccept.setOnClickListener(new NodeClickListener(1));
                mDeny.setOnClickListener(new NodeClickListener(0));

            } else if (o instanceof Rule) {
                mRule = (Rule) o;

                List<NodesResponse.Node> nodes = ModelStorage.getInstance().getNodes(new GetNodesInfoRequest());

                if (nodes != null) {
                    for (NodesResponse.Node n : nodes) {
                        if (n.getNodeId() == mRule.getCommand().getNodeId()
                                && n.getControllerId().equals(mRule.getControllerId())) {
                            mNode = n;

                            String str = "New rule for " + n.getExtra().get(NODE_MAP_NAME);
                            mName.setText(str);
                            mRoom.setText(n.getExtra().get(NODE_MAP_ROOM));

                            mAccept.setOnClickListener(new RuleClickListener(1));
                            mDeny.setOnClickListener(new RuleClickListener(0));

                            break;
                        }
                    }
                }
            }
        }

        @Override
        public void onClick(View v) {
            ShowInfosDialogFragment.newInstance(mNode, mRule).show(getFragmentManager(), DIALOG_TAG);
        }
    }

    class AcceptCallback implements RequestCallback {

        NodesResponse.Node mNode;
        Rule mRule;

        AcceptCallback(NodesResponse.Node node, Rule rule) {
            mNode = node;
            mRule = rule;
        }

        @Override
        public void onPostRequest(SimpleResponse s) {
            List<Object> nodes = mAdapter.getNotifications();

            for (int i = 0; i < nodes.size(); i++) {
                Object o = nodes.get(i);

                if (mRule == null && mNode == o || mRule == o) {
                    nodes.remove(i);
                    mAdapter.notifyDataSetChanged();

                    Snackbar.make(
                            getActivity().findViewById(R.id.menu_fragments_activity_container),
                            "Done!",
                            Snackbar.LENGTH_SHORT
                    ).show();

                    break;
                }
            }
        }
    }

    class NotificationAdapter extends RecyclerView.Adapter<NotificationHolder> {

        List<Object> mNotifications;

        NotificationAdapter() {
            mNotifications = new ArrayList<>();
        }

        List<Object> getNotifications() {
            return mNotifications;
        }

        void setNodes() {
            List<Object> list = new ArrayList<>();

            List<NodesResponse.Node> nodeList = ModelStorage.getInstance().getNodes(new GetNodesInfoRequest());
            List<Rule> ruleList = ModelStorage.getInstance().getLearntRules(new GetLearntRulesRequest());

            if (nodeList != null) {
                for (NodesResponse.Node n : nodeList) {
                    if (n.getAccepted() == 0)
                        list.add(n);
                }
            }

            if (ruleList != null) {
                list.addAll(ruleList);
            }

            if (nodeList == null || ruleList == null) {
                mLoadingPanel.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            } else {
                mLoadingPanel.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);

                mNotifications = list;
                notifyDataSetChanged();
            }
        }


        @Override
        public NotificationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_notification_item, parent, false);
            return new NotificationHolder(view);
        }

        @Override
        public void onBindViewHolder(NotificationHolder holder, int position) {
            holder.bind(mNotifications.get(position));
        }

        @Override
        public int getItemCount() {
            return mNotifications.size();
        }
    }

    class GetNodesInfoRequest implements RequestCallback {
        @Override
        public void onPostRequest(SimpleResponse s) {
            if (s == null) {
                Toast.makeText(
                        getActivity(),
                        getResources().getText(R.string.state_fragment_error),
                        Toast.LENGTH_LONG).show();
            } else if (s.getStatus() == 0 && s.getErrorMessage().equals(AsyncRequest.NOT_CREDENTIALS_ERROR)) {
                getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
            } else {
                updateUI();
            }
        }
    }

    class GetLearntRulesRequest implements RequestCallback {
        @Override
        public void onPostRequest(SimpleResponse s) {
            if (s == null) {
                Toast.makeText(
                        getActivity(),
                        getResources().getText(R.string.state_fragment_error),
                        Toast.LENGTH_LONG).show();
            } else if (s.getStatus() == 0 && s.getErrorMessage().equals(AsyncRequest.NOT_CREDENTIALS_ERROR)) {
                getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
            } else {
                updateUI();
            }
        }
    }

    public static class ShowInfosDialogFragment extends DialogFragment {

        private NodesResponse.Node mNode;
        private Rule mRule;

        private TextView mTitle;
        private TextView mControllerId;
        private TextView mNodeId;

        private TextView mResult;
        private TextView mClause;


        public void setNode(NodesResponse.Node node) {
            mNode = node;
        }

        public void setRule(Rule rule) {
            mRule = rule;
        }

        static ShowInfosDialogFragment newInstance(NodesResponse.Node node, Rule rule) {
            ShowInfosDialogFragment fragment = new ShowInfosDialogFragment();

            fragment.setNode(node);
            fragment.setRule(rule);

            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public void onResume() {
            super.onResume();
            int width = getResources().getDimensionPixelSize(R.dimen.dialog_width);
            int height = getResources().getDimensionPixelSize(R.dimen.dialog_height);
            getDialog().getWindow().setLayout(width, height);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialogInfos);
            return super.onCreateDialog(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.notification_dialog_fragment, container, false);

            getDialog().setCanceledOnTouchOutside(true);
            Window window  = getDialog().getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(R.drawable.round_dialog);
            }

            mTitle = (TextView) view.findViewById(R.id.notification_dialog_fragment_name);
            mTitle.setText(mNode.getExtra().get("name"));

            mControllerId = (TextView) view.findViewById(R.id.notification_dialog_fragment_controller_id);
            mControllerId.setText(mNode.getControllerId());

            mNodeId = (TextView) view.findViewById(R.id.notification_dialog_fragment_node_id);
            String nodeId = String.valueOf(mNode.getNodeId());
            mNodeId.setText(nodeId);

            if (mRule != null) {
                view.findViewById(R.id.clause_tablerow).setVisibility(View.VISIBLE);
                view.findViewById(R.id.result_tablerow).setVisibility(View.VISIBLE);

                mResult = (TextView) view.findViewById(R.id.notification_dialog_fragment_res_id);
                mResult.setText(AppStringUtils.getRuleEffectLegibleText(
                        getActivity(),
                        mRule,
                        ModelStorage.getInstance().getNodes(null))
                );

                mClause = (TextView) view.findViewById(R.id.notification_dialog_fragment_clause_id);
                mClause.setText(AppStringUtils.getRuleConditionLegibleText(
                        getActivity(),
                        mRule,
                        ModelStorage.getInstance().getNodes(null))
                );
            } else {

                LinearLayout ll = (LinearLayout) view.findViewById(R.id.notification_dialog_fragment_scrollview);
                LayoutInflater linflater = LayoutInflater.from(getActivity());

                for (NodesResponse.DataType d : mNode.getDataType()) {

                    View footerView = linflater.inflate(R.layout.notification_node_view_dialog_fragment, null, false);

                    TextView node = (TextView) footerView.findViewById(R.id.dialog_node_view_node_type);
                    node.setText("Sensor");

                    TextView type = (TextView) footerView.findViewById(R.id.dialog_node_view_value_type);
                    type.setText(EnumUtil.getEnumPrettyName(d.getType().getId(), TypeEnum.class));

                    TextView category = (TextView) footerView.findViewById(R.id.dialog_node_view_category);
                    category.setText(EnumUtil.getEnumPrettyName(d.getDataCategory().getId(), DataCategoryEnum.class));

                    TextView unit = (TextView) footerView.findViewById(R.id.dialog_node_view_unit);
                    unit.setText(d.getUnit());

                    TextView range = (TextView) footerView.findViewById(R.id.dialog_node_view_range);
                    String str = "[" + d.getRange()[0] + ", " + d.getRange()[1] + "]";
                    range.setText(str);

                    ll.addView(footerView);
                }

                for (NodesResponse.CommandType c : mNode.getCommandType()) {

                    View footerView = linflater.inflate(R.layout.notification_node_view_dialog_fragment, null, false);

                    TextView node = (TextView) footerView.findViewById(R.id.dialog_node_view_node_type);
                    node.setText("Actuator");

                    TextView type = (TextView) footerView.findViewById(R.id.dialog_node_view_value_type);
                    type.setText(EnumUtil.getEnumPrettyName(c.getType().getId(), TypeEnum.class));

                    TextView category = (TextView) footerView.findViewById(R.id.dialog_node_view_category);
                    category.setText(EnumUtil.getEnumPrettyName(c.getCommandCategory().getId(), CommandCategoryEnum.class));

                    TextView unit = (TextView) footerView.findViewById(R.id.dialog_node_view_unit);
                    unit.setText(c.getUnit());

                    TextView range = (TextView) footerView.findViewById(R.id.dialog_node_view_range);
                    String str = "[" + c.getRange()[0] + ", " + c.getRange()[1] + "]";
                    range.setText(str);

                    ll.addView(footerView);
                }
            }

            return view;
        }
    }
}
