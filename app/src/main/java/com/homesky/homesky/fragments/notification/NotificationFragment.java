package com.homesky.homesky.fragments.notification;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.homesky.homecloud_lib.model.Rule;
import com.homesky.homecloud_lib.model.notification.Notification;
import com.homesky.homecloud_lib.model.response.NodesResponse;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homesky.R;
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
                updateUI();
                mStateSwipeRefresh.setRefreshing(false);
            }
        });

        updateUI();

        return view;
    }

    private void updateUI() {
        if (mAdapter == null) {
            mAdapter = new NotificationAdapter();
            mRecyclerView.setAdapter(mAdapter);
        }

        mAdapter.setNodes();
    }

    private class NotificationHolder extends RecyclerView.ViewHolder {

        private NodesResponse.Node mNode;
        private Rule mRule;
        private TextView mName, mRoom;

        NotificationHolder(View itemView) {
            super(itemView);

            mName = (TextView) itemView.findViewById(R.id.list_notification_node_name);
            mRoom = (TextView) itemView.findViewById(R.id.list_notification_room_name);
        }

        void bind(Object o) {

            if (o instanceof NodesResponse.Node) {
                mNode = (NodesResponse.Node) o;

                String str = "A " + mNode.getExtra().get(NODE_MAP_NAME) + " detected";
                mName.setText(str);
                mRoom.setText(mNode.getExtra().get(NODE_MAP_ROOM));
            } else if (o instanceof Rule) {
                mRule = (Rule) o;

                List<NodesResponse.Node> nodes = ModelStorage.getInstance().getNodes(new GetNodesInfoRequest());

                if (nodes != null) {
                    for (NodesResponse.Node n : nodes) {
                        if (n.getNodeId() == mRule.getCommand().getNodeId()
                                && n.getControllerId().equals(mRule.getControllerId())) {

                            String str = "New rule for " + n.getExtra().get(NODE_MAP_NAME);
                            mName.setText(str);
                            mRoom.setText(n.getExtra().get(NODE_MAP_ROOM));
                            break;
                        }
                    }
                }
            }
        }
    }

    class NotificationAdapter extends RecyclerView.Adapter<NotificationHolder> {

        List<Object> mNotifications;

        NotificationAdapter() {
            mNotifications = new ArrayList<>();
        }

        void setNodes() {
            List<Object> list = new ArrayList<>();

            List<NodesResponse.Node> nodeList = ModelStorage.getInstance().getNodes(new GetNodesInfoRequest());
            List<Rule> ruleList = ModelStorage.getInstance().getLearntRules(new GetLearntRules());

            if (nodeList != null) {
                for (NodesResponse.Node n : nodeList) {
                    if (n.getAccepted() == 0)
                        list.add(n);
                }
            }

            if (ruleList != null)
                list.addAll(ruleList);

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

    class GetLearntRules implements RequestCallback {
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
}
