package com.homesky.homesky.fragments.clause;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.homesky.homecloud_lib.model.Proposition;
import com.homesky.homecloud_lib.model.enums.EnumUtil;
import com.homesky.homecloud_lib.model.response.NodesResponse;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homesky.R;
import com.homesky.homesky.fragments.andStatement.AndStatementFragment;
import com.homesky.homesky.fragments.andStatement.AndStatementFragmentEmpty;
import com.homesky.homesky.fragments.ruleList.RuleListFragment;
import com.homesky.homesky.request.ModelStorage;
import com.homesky.homesky.request.RequestCallback;
import com.homesky.homesky.utils.AppEnumUtils;
import com.homesky.homesky.utils.AppFindElementUtils;
import com.homesky.homesky.utils.AppStringUtils;

import java.math.BigDecimal;
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
    private static final String ARG_CONTROLLER_ID = "controllerId";
    private static final String DIALOG_TAG = "ClauseFragmentTag";
    public static final String EXTRA_CLAUSE = "extraClause";
    public static final String EXTRA_COMMAND_ID = "extraCommandId";
    public static final String EXTRA_VALUE = "extraValue";


    private int mNodeId;
    private String mControllerId;
    private NodesResponse.Node mNode;
    private List<NodesResponse.Node> mNodes;
    private ArrayList<ArrayList<Proposition>> mClause;
    ArrayList<NodesResponse.CommandType> mCommandSpinnerIndexToCommandType = new ArrayList<>();

    private Spinner mActionCommandSpinner;
    private EditText mActionValueEditText;
    private Switch mActionValueSwitch;
    private ViewPager mViewPager;
    private FloatingActionButton mFloatingActionButton;

    public static Fragment newInstance(int nodeId, String controllerId){
        Bundle args = new Bundle();
        args.putInt(ARG_NODE_ID, nodeId);
        args.putString(ARG_CONTROLLER_ID, controllerId);
        Fragment fragment = new ClauseFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNodeId = getArguments().getInt(ARG_NODE_ID);
        mControllerId = getArguments().getString(ARG_CONTROLLER_ID);
        mClause = new ArrayList<>();
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clause, container, false);

        mActionCommandSpinner = (Spinner)view.findViewById(R.id.fragment_clause_action_command_spinner);

        mActionValueEditText = (EditText)view.findViewById(R.id.fragment_clause_action_value_edit_text);
        mActionValueSwitch = (Switch)view.findViewById(R.id.fragment_clause_action_value_switch);
        mActionCommandSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                NodesResponse.CommandType ct = mCommandSpinnerIndexToCommandType.get(i);
                mActionValueEditText.setText("");
                switch (ct.getType()){
                    case INT:
                        Log.d(TAG, "Changing to int");
                        mActionValueEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        mActionValueEditText.setVisibility(View.VISIBLE);
                        mActionValueSwitch.setVisibility(View.GONE);
                        break;
                    case REAL:
                        Log.d(TAG, "Changing to real");
                        mActionValueEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
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

        mFloatingActionButton = (FloatingActionButton)view.findViewById(R.id.fragment_clause_fab);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PropositionDialog dialog = PropositionDialog.newInstance(mViewPager.getCurrentItem(), mControllerId);
                dialog.show(getActivity().getSupportFragmentManager(), DIALOG_TAG);
            }
        });

        updateUI();

        return view;
    }

    public void updateUI(){
        mNodes = ModelStorage.getInstance().getNodes(this);
        if(mNodes != null){
            List<NodesResponse.Node> nodes = ModelStorage.getInstance().getNodes(this);
            mNode = AppFindElementUtils.findNodeFromId(mNodeId, mControllerId, nodes);

            List<String> commandNames = new ArrayList<>();
            for(NodesResponse.CommandType ct : mNode.getCommandType()){
                mCommandSpinnerIndexToCommandType.add(ct);
                commandNames.add(AppEnumUtils.commandCategoryToString(getActivity(), ct.getCommandCategory()));
            }
            mActionCommandSpinner.setAdapter(new ArrayAdapter<String>(
                    getActivity(), android.R.layout.simple_spinner_dropdown_item, commandNames));

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
                                    getActivity(), p, mNodes, mControllerId));
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

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_clause, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.menu_clause_done){
            if(validateInput()){
                Intent data = new Intent();
                data.putExtra(EXTRA_CLAUSE, mClause);

                data.putExtra(EXTRA_COMMAND_ID, mCommandSpinnerIndexToCommandType.get(
                        mActionCommandSpinner.getSelectedItemPosition()).getId());

                BigDecimal actionValue = null;
                if(mActionValueEditText.getVisibility() == View.VISIBLE){
                    actionValue = new BigDecimal(mActionValueEditText.getText().toString());
                }
                else{
                    actionValue = new BigDecimal(mActionValueSwitch.isChecked()? 1 : 0);
                }
                data.putExtra(EXTRA_VALUE, actionValue);

                getActivity().setResult(Activity.RESULT_OK, data);
                NavUtils.navigateUpTo(getActivity(), new Intent(getActivity(), RuleListFragment.class));
                return true;
            }
            else {
                Toast.makeText(
                        getActivity(),
                        getResources().getText(R.string.clause_save_error_message),
                        Toast.LENGTH_LONG).show();
                return true;
            }
        }
        else if(id == R.id.menu_clause_cancel){
            getActivity().setResult(Activity.RESULT_CANCELED, null);
            NavUtils.navigateUpTo(getActivity(), new Intent(getActivity(), RuleListFragment.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPostRequest(SimpleResponse s) {
        if(s instanceof NodesResponse)
            updateUI();
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

    private boolean validateInput(){
        if(mActionValueEditText.getVisibility() == View.VISIBLE && !isValidFloat(mActionValueEditText.getText().toString())){
            return false;
        }
        else if(mClause.size() == 0){
            return false;
        }
        else{
            return true;
        }
    }

    private boolean isValidFloat(String s){
        try{
            Float.parseFloat(s);
            return true;
        }
        catch(NumberFormatException e){
            return false;
        }
    }
}
