package com.homesky.homesky.fragments.clause;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.homesky.homecloud_lib.model.response.NodesResponse;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homesky.R;
import com.homesky.homesky.fragments.orStatement.OrStatementFragment;
import com.homesky.homesky.request.ModelStorage;
import com.homesky.homesky.request.RequestCallback;
import com.homesky.homesky.utils.AppEnumUtils;
import com.homesky.homesky.utils.AppFindElementUtils;

import java.util.HashMap;
import java.util.List;

public class ClauseFragment extends Fragment implements RequestCallback {
    private static final String TAG = "ClauseFragment";
    private static final String ARG_NODE_ID = "nodeId";

    private int mNodeId;

    private Spinner mActionCommandSpinner;
    private EditText mActionValueEditText;
    private ViewPager mViewPager;

    public static Fragment newInstance(int nodeId){
        Bundle args = new Bundle();
        args.putInt(ARG_NODE_ID, nodeId);
        Fragment fragment = new ClauseFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNodeId = getArguments().getInt(ARG_NODE_ID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clause, container, false);

        mActionCommandSpinner = (Spinner)view.findViewById(R.id.fragment_clause_action_command_spinner);
        List<NodesResponse.Node> nodes = ModelStorage.getInstance().getNodes(this);
        NodesResponse.Node node = AppFindElementUtils.findNodeFromId(mNodeId, nodes);
        HashMap<String, Integer> commandNameToId = new HashMap<>();
        for(NodesResponse.CommandType ct : node.getCommandType())
            commandNameToId.put(
                    AppEnumUtils.commandCategoryToString(getActivity(), ct.getCommandCategory()),
                    ct.getId()
            );
        String[] commandNames = commandNameToId.keySet().toArray(new String[commandNameToId.size()]);
        mActionCommandSpinner.setAdapter(new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_spinner_item, commandNames));

        mActionValueEditText = (EditText)view.findViewById(R.id.fragment_clause_action_value_edit_text);

        mViewPager = (ViewPager)view.findViewById(R.id.fragment_clause_view_pager);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {
                return OrStatementFragment.newInstance(0);
            }

            @Override
            public int getCount() {
                return 5;
            }
        });
        return view;
    }

    @Override
    public void onPostRequest(SimpleResponse s) {

    }


}
