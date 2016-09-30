package com.homesky.homesky.fragments.state;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homesky.R;
import com.homesky.homesky.command.GetHouseStateCommand;
import com.homesky.homesky.request.AsyncRequest;
import com.homesky.homesky.request.RequestCallback;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by henrique on 9/22/16.
 */
public class StateFragment extends Fragment implements RequestCallback {

    private static final String TAG = "StateFragment";

    private RecyclerView mListOfNodes;
    private StateAdapter mStateAdapter;

    public StateFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_state, container, false);

        mListOfNodes = (RecyclerView) view.findViewById(R.id.state_fragment_list_nodes);
        mListOfNodes.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();
        new AsyncRequest(this).execute(new GetHouseStateCommand());

        return view;
    }

    private void updateUI() {
        List<Node> mNodes = new LinkedList<>();

        /** TEST: DELETE **/
        for (int i = 0; i < 50; i++)
            mNodes.add(new Node(String.valueOf(i)));

        mStateAdapter = new StateAdapter(getActivity(), mNodes);
        mListOfNodes.setAdapter(mStateAdapter);
    }

    @Override
    public void onPostRequest(SimpleResponse s) {
        if (s == null) {
            Toast.makeText(
                    getActivity(),
                    getResources().getText(R.string.login_fragment_server_offline),
                    Toast.LENGTH_LONG).show();
        } else {
            Log.i(TAG, s.toString());
        }
    }
}
