package com.homesky.homesky.fragments.state;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.homesky.homecloud_lib.model.response.NodesResponse;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homecloud_lib.model.response.StateResponse;
import com.homesky.homesky.R;
import com.homesky.homesky.command.GetHouseStateCommand;
import com.homesky.homesky.command.GetNodesInfoCommand;
import com.homesky.homesky.fragments.node.NodeActivity;
import com.homesky.homesky.fragments.node.NodeFragment;
import com.homesky.homesky.request.AsyncRequest;
import com.homesky.homesky.request.ModelStorage;
import com.homesky.homesky.request.RequestCallback;
import com.homesky.homecloud_lib.model.response.StateResponse.NodeState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.view.View.GONE;

/**
 * Created by henrique on 9/22/16.
 */
public class StateFragment extends Fragment {

    private static final String TAG = "StateFragment";

    private RecyclerView mListOfNodes;
    private StateAdapter mStateAdapter;
    private RelativeLayout mLoadingPanel;
    private SwipeRefreshLayout mStateSwipeRefresh;

    public StateFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_state, container, false);

        mLoadingPanel = (RelativeLayout) view.findViewById(R.id.state_fragment_loading_panel);
        mListOfNodes = (RecyclerView) view.findViewById(R.id.state_fragment_list_nodes);
        mListOfNodes.setLayoutManager(new LinearLayoutManager(getActivity()));

        mStateSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.state_fragment_swipe_refresh_layout);
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
        if (mStateAdapter == null) {
            mStateAdapter = new StateAdapter();
            mListOfNodes.setAdapter(mStateAdapter);
        } else {
            //int position = mStateAdapter.getItemCount();
            mStateAdapter.setNodes(ModelStorage.getInstance().getNodes(new GetNodesInfoRequest()));
            mStateAdapter.notifyDataSetChanged();
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
            } else {
                updateUI();
            }

            mLoadingPanel.setVisibility(View.GONE);
            mListOfNodes.setVisibility(View.VISIBLE);
        }
    }














    /* State adapter */

    class StateAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<Object> mNodes;
        private static final int ITEM_STATE = 0;
        private static final int ITEM_HEADER = 1;

        StateAdapter() {
            mNodes = new ArrayList<>();

            setNodes(ModelStorage.getInstance().getNodes(new GetNodesInfoRequest()));
            if (!mNodes.isEmpty()) {
                mLoadingPanel.setVisibility(View.GONE);
                mListOfNodes.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            if (viewType == ITEM_STATE) {
                View view = layoutInflater.inflate(R.layout.list_item_state, parent, false);
                return new StateHolder(view);
            } else if (viewType == ITEM_HEADER) {
                View view = layoutInflater.inflate(R.layout.list_item_state_header, parent, false);
                return new HeaderHolder(view);
            }

            return null;
        }

        @Override
        public int getItemViewType(int position) {
            if (mNodes.get(position) instanceof String) {
                return ITEM_HEADER;
            } else {
                return ITEM_STATE;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int itemType = getItemViewType(position);

            Log.d(TAG, "onBindViewHolder: " + position + " " + mNodes.get(position));

            if (itemType == ITEM_HEADER) {
                ((HeaderHolder) holder).bind((String) mNodes.get(position));
            } else if (itemType == ITEM_STATE) {
                ((StateHolder) holder).bind((NodesResponse.Node) mNodes.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return mNodes.size();
        }

        void setNodes (List<NodesResponse.Node> nodes) {
            if (nodes == null || nodes.isEmpty()) return;

            Map<String, List<NodesResponse.Node>> nodesPerRoom = new HashMap<>();

            for (NodesResponse.Node n : nodes) {
                String room = n.getExtra().get("room");
                if (nodesPerRoom.containsKey(room)) {
                    nodesPerRoom.get(room).add(n);
                } else {
                    List<NodesResponse.Node> list = new LinkedList<>();
                    list.add(n);
                    nodesPerRoom.put(room, list);
                }
            }

            mNodes = new ArrayList<>();
            for (Map.Entry entry : nodesPerRoom.entrySet()) {
                mNodes.add(entry.getKey());
                mNodes.addAll((Collection<?>) entry.getValue());
            }
        }
    }

    class StateHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mNodeIdTextView;
        private TextView mNodeName;

        private int mNodeId;
        private String mControllerId;

        StateHolder (View itemView) {
            super(itemView);
            mNodeIdTextView = (TextView) itemView.findViewById(R.id.state_fragment_node_id);
            mNodeName = (TextView) itemView.findViewById(R.id.state_fragment_node_name);

            itemView.setOnClickListener(this);
        }

        void bind (NodesResponse.Node node) {
            mNodeId = node.getNodeId();
            mControllerId = node.getControllerId();

            String nodeTextView = "id " + mNodeId;
            mNodeIdTextView.setText(nodeTextView);
            mNodeName.setText(node.getExtra().get("name"));
        }

        @Override
        public void onClick(View v) {
            startActivity(NodeActivity.newIntent(getActivity(), mNodeId, mControllerId));
        }
    }

    class HeaderHolder extends RecyclerView.ViewHolder {

        private TextView mHeaderName;

        HeaderHolder(View itemView) {
            super(itemView);
            mHeaderName = (TextView) itemView.findViewById(R.id.state_fragment_list_header);
        }

        void bind(String header) {
            mHeaderName.setText(header);
        }
    }


}
