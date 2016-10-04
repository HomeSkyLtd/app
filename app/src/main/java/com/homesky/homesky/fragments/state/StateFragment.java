package com.homesky.homesky.fragments.state;

import android.content.Context;
import android.content.res.Resources;
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

import com.homesky.homecloud_lib.model.response.NodesResponse;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homecloud_lib.model.response.StateResponse;
import com.homesky.homesky.R;
import com.homesky.homesky.command.GetHouseStateCommand;
import com.homesky.homesky.command.GetNodesInfoCommand;
import com.homesky.homesky.fragments.node.NodeActivity;
import com.homesky.homesky.request.AsyncRequest;
import com.homesky.homesky.request.RequestCallback;
import com.homesky.homecloud_lib.model.response.StateResponse.NodeState;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by henrique on 9/22/16.
 */
public class StateFragment extends Fragment {

    private static final String TAG = "StateFragment";

    private RecyclerView mListOfNodes;
    private StateAdapter mStateAdapter;
    List<NodeState> mNodeStates;
    List<NodesResponse.Node> mNodes;

    public StateFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_state, container, false);

        mListOfNodes = (RecyclerView) view.findViewById(R.id.state_fragment_list_nodes);
        mListOfNodes.setLayoutManager(new LinearLayoutManager(getActivity()));

        new AsyncRequest(new GetHouseStateRequest()).execute(new GetHouseStateCommand());
        new AsyncRequest(new GetNodesInfoRequest()).execute(new GetNodesInfoCommand());

        mNodeStates = new LinkedList<>();
        mNodes = new LinkedList<>();

        updateUI(mNodes);

        return view;
    }

    private void updateUI(List<NodesResponse.Node> nodes) {
        mStateAdapter = new StateAdapter(nodes);
        mListOfNodes.setAdapter(mStateAdapter);
    }

    class GetHouseStateRequest implements RequestCallback {
        @Override
        public void onPostRequest(SimpleResponse s) {
            if (s == null) {
                Toast.makeText(
                        getActivity(),
                        getResources().getText(R.string.login_fragment_server_already_logged),
                        Toast.LENGTH_LONG).show();
            } else {
                StateResponse sr = (StateResponse) s;

                for (NodeState state : sr.getState()) {
                    mNodeStates.add(state);
                }
                //TODO: call getNodeIdToValue(true)
            }
        }
    }

    class GetNodesInfoRequest implements RequestCallback {
        @Override
        public void onPostRequest(SimpleResponse s) {
            if (s == null) {
                Toast.makeText(
                        getActivity(),
                        getResources().getText(R.string.login_fragment_server_already_logged),
                        Toast.LENGTH_LONG).show();
            } else {
                NodesResponse nr = (NodesResponse) s;

                int position = mNodes.size();
                for (NodesResponse.Node node : nr.getNodes()) {
                    mNodes.add(node);
                }

                mStateAdapter.setNodes(mNodes);
                mStateAdapter.notifyItemRangeChanged(position, mStateAdapter.getItemCount() - position);

                if (mNodeIdToValue.isEmpty() && mNodeStates != null) {
                    new BuildMap().start();
                }
            }
        }
    }














    /* State adapter */

    class StateAdapter extends RecyclerView.Adapter<StateHolder> {

        private List<NodesResponse.Node> mNodes;

        StateAdapter(List<NodesResponse.Node> nodes) {
            mNodes = nodes;
        }

        @Override
        public StateHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_state, parent, false);

            return new StateHolder(view);
        }

        @Override
        public void onBindViewHolder(StateHolder holder, int position) {
            NodesResponse.Node node = mNodes.get(position);
            holder.bind(node.getNodeId(), node.getControllerId());
        }

        @Override
        public int getItemCount() {
            return mNodes.size();
        }

        void setNodes (List<NodesResponse.Node> nodes) {
            mNodes = nodes;
        }
    }

    class StateHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mNodeName;
        private int mNodeId;
        private String mControllerId;

        StateHolder (View itemView) {
            super(itemView);
            mNodeName = (TextView) itemView.findViewById(R.id.state_fragment_state_name);
            itemView.setOnClickListener(this);
        }

        void bind (int nodeId, String controllerId) {
            mNodeId = nodeId;
            mControllerId = controllerId;

            String str = "Node " + nodeId + " from Controller " + controllerId;
            mNodeName.setText(str);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick: " + mNodeIdToValue.toString());
            startActivity(NodeActivity.newIntent(getActivity(), mNodeId, mControllerId));
        }
    }


}
