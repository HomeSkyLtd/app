package com.homesky.homesky.fragments.clause;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import com.homesky.homecloud_lib.model.Proposition;
import com.homesky.homecloud_lib.model.enums.EnumUtil;
import com.homesky.homecloud_lib.model.response.NodesResponse;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homesky.R;
import com.homesky.homesky.fragments.andStatement.AndStatementFragment;
import com.homesky.homesky.fragments.andStatement.AndStatementFragmentEmpty;
import com.homesky.homesky.request.ModelStorage;
import com.homesky.homesky.request.RequestCallback;
import com.homesky.homesky.utils.AppEnumUtils;
import com.homesky.homesky.utils.AppFindElementUtils;
import com.homesky.homesky.utils.AppStringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.homesky.homecloud_lib.model.enums.TypeEnum.BOOL;
import static com.homesky.homecloud_lib.model.enums.TypeEnum.INT;
import static com.homesky.homecloud_lib.model.enums.TypeEnum.REAL;
import static com.homesky.homecloud_lib.model.enums.TypeEnum.STRING;

public class ClauseFragment extends Fragment implements RequestCallback, PropositionDialog.PropositionDialogCallback {
    private static final String TAG = "ClauseFragment";
    private static final String ARG_NODE_ID = "nodeId";

    private int mNodeId;
    private NodesResponse.Node mNode;
    private List<List<Proposition>> mClause;
    ArrayList<NodesResponse.CommandType> mCommandSpinnerIndexToCommandType = new ArrayList<>();

    private Spinner mActionCommandSpinner;
    private EditText mActionValueEditText;
    private Switch mActionValueSwitch;
    private ViewPager mViewPager;
    private FloatingActionButton mFloatingActionButton;

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
        mClause = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clause, container, false);

        mActionCommandSpinner = (Spinner)view.findViewById(R.id.fragment_clause_action_command_spinner);
        List<NodesResponse.Node> nodes = ModelStorage.getInstance().getNodes(this);
        mNode = AppFindElementUtils.findNodeFromId(mNodeId, nodes);

        List<String> commandNames = new ArrayList<>();
        for(NodesResponse.CommandType ct : mNode.getCommandType()){
            mCommandSpinnerIndexToCommandType.add(ct);
            commandNames.add(AppEnumUtils.commandCategoryToString(getActivity(), ct.getCommandCategory()));
        }
        mActionCommandSpinner.setAdapter(new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_spinner_dropdown_item, commandNames));

        mActionValueEditText = (EditText)view.findViewById(R.id.fragment_clause_action_value_edit_text);
        mActionValueSwitch = (Switch)view.findViewById(R.id.fragment_clause_action_value_switch);
        mActionCommandSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                NodesResponse.CommandType ct = mCommandSpinnerIndexToCommandType.get(i);
                switch (ct.getType()){
                    case INT:
                        mActionValueEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        mActionValueEditText.setVisibility(View.VISIBLE);
                        mActionValueSwitch.setVisibility(View.GONE);
                        break;
                    case REAL:
                        mActionValueEditText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        mActionValueEditText.setVisibility(View.VISIBLE);
                        mActionValueSwitch.setVisibility(View.GONE);
                        break;
                    case STRING:
                        mActionValueEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                        mActionValueEditText.setVisibility(View.VISIBLE);
                        mActionValueSwitch.setVisibility(View.GONE);
                        break;
                    case BOOL:
                        mActionValueEditText.setVisibility(View.GONE);
                        mActionValueSwitch.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        mActionValueEditText = (EditText)view.findViewById(R.id.fragment_clause_action_value_edit_text);

        mViewPager = (ViewPager)view.findViewById(R.id.fragment_clause_view_pager);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {
                if(position == mClause.size())
                    return AndStatementFragmentEmpty.newInstance();
                else{
                    List<String> andStatement = new ArrayList<>(mClause.get(position).size());
                    for(Proposition p : mClause.get(position)){
                        andStatement.add(AppStringUtils.getPropositionLegibleText(
                                getActivity(), p, ModelStorage.getInstance().getNodes(ClauseFragment.this)));
                    }
                    Log.d(TAG, "Ran this");
                    return AndStatementFragment.newInstance(andStatement);
                }
            }

            @Override
            public int getCount() {
                return mClause.size() + 1;
            }

            @Override
            public int getItemPosition(Object object) {
                return POSITION_NONE;
            }

        });

        mFloatingActionButton = (FloatingActionButton)view.findViewById(R.id.fragment_clause_fab);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PropositionDialog dialog = PropositionDialog.newInstance(mViewPager.getCurrentItem());
                dialog.show(getActivity().getSupportFragmentManager(), "a");
            }
        });

        return view;
    }

    @Override
    public void onPostRequest(SimpleResponse s) {

    }


    @Override
    public void onPropositionResult(Proposition p, int orStatementIndex) {
        if(mClause.size() <= orStatementIndex){
            mClause.add(new ArrayList<Proposition>());
            mClause.get(orStatementIndex).add(p);
        }
        else{
            mClause.get(orStatementIndex).add(p);
        }
        mViewPager.getAdapter().notifyDataSetChanged();
    }
}
