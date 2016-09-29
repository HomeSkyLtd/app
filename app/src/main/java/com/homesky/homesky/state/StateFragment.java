package com.homesky.homesky.state;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.homesky.homesky.R;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by henrique on 9/22/16.
 */
public class StateFragment extends Fragment {

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

        return view;
    }

    private void updateUI() {
        List<State> states  = new LinkedList<>();

        /** TEST: DELETE **/
        for (int i = 0; i < 50; i++)
            states.add(new State(String.valueOf(i)));

        mStateAdapter = new StateAdapter(getActivity(), states);
        mListOfNodes.setAdapter(mStateAdapter);
    }
}
