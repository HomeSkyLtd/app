package com.homesky.homesky.fragments.state;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.homesky.homecloud_lib.model.response.NodesResponse;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homecloud_lib.model.response.StateResponse;
import com.homesky.homesky.R;
import com.homesky.homesky.command.GetHouseStateCommand;
import com.homesky.homesky.command.GetNodesInfoCommand;
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
    private List<NodeState> mNodeStates;
    private List<NodesResponse.Node> mNodes;

    Map<NodeState, NodesResponse.Node> mNodeIdToValue;

    public StateFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_state, container, false);

        mListOfNodes = (RecyclerView) view.findViewById(R.id.state_fragment_list_nodes);
        mListOfNodes.setLayoutManager(new LinearLayoutManager(getActivity()));

        mNodeIdToValue = new HashMap<>();

        new AsyncRequest(new GetHouseStateRequest()).execute(new GetHouseStateCommand());
        new AsyncRequest(new GetNodesInfoRequest()).execute(new GetNodesInfoCommand());

        mNodeStates = new LinkedList<>();
        mNodes = new LinkedList<>();

        updateUI(mNodeStates);

        return view;

        //TODO: Fazer match no mapa de NodeState para Node
    }

    private void updateUI(List<NodeState> nodes) {
        mStateAdapter = new StateAdapter(getActivity(), nodes);
        mListOfNodes.setAdapter(mStateAdapter);
    }

    class GetHouseStateRequest implements RequestCallback {
        @Override
        public void onPostRequest(SimpleResponse s) {
            if (s == null) {
                Toast.makeText(
                        getActivity(),
                        getResources().getText(R.string.login_fragment_server_offline),
                        Toast.LENGTH_LONG).show();
            } else {
                StateResponse sr = (StateResponse) s;

                int position = mNodeStates.size();
                for (NodeState state : sr.getState()) {
                    mNodeStates.add(state);
                }

                mStateAdapter.notifyItemRangeChanged(position, mStateAdapter.getItemCount() - position);
            }
        }
    }

    class GetNodesInfoRequest implements RequestCallback {
        @Override
        public void onPostRequest(SimpleResponse s) {
            if (s == null) {
                Toast.makeText(
                        getActivity(),
                        getResources().getText(R.string.login_fragment_server_offline),
                        Toast.LENGTH_LONG).show();
            } else {
                NodesResponse nr = (NodesResponse) s;

                for (NodesResponse.Node node : nr.getNodes()) {
                    mNodes.add(node);
                }
            }
        }
    }
}
