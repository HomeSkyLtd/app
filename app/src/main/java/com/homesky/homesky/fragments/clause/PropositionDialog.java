package com.homesky.homesky.fragments.clause;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.homesky.homecloud_lib.model.Proposition;
import com.homesky.homecloud_lib.model.enums.NodeClassEnum;
import com.homesky.homecloud_lib.model.enums.OperatorEnum;
import com.homesky.homecloud_lib.model.response.NodesResponse;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homesky.R;
import com.homesky.homesky.request.AsyncRequest;
import com.homesky.homesky.request.ModelStorage;
import com.homesky.homesky.request.RequestCallback;
import com.homesky.homesky.utils.AppEnumUtils;
import com.homesky.homesky.utils.AppFindElementUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropositionDialog extends DialogFragment implements RequestCallback{
    private static final String TAG = "PropositionDialog";
    private static final String NODE_EXTRA_NAME = "name";
    private static final String ARG_OR_STATEMENT_INDEX = "orStatementIndex";
    private static final String ARG_CONTROLLER_ID = "controllerId";

    private int mOrStatementIndex;
    private String mControllerId;

    private List<NodesResponse.Node> mNodes;
    private List<Integer> mSpinnerIndexToNodeUId;
    private List<Integer> mLhsCommandSpinnerIndexToCommandId = new ArrayList<>();
    private List<Integer> mRhsCommandSpinnerIndexToCommandId = new ArrayList<>();

    private PropositionDialogCallback mCallback;
    private RadioGroup mRhsRadioGroup;
    private GridLayout mRhsNodeGrid, mRhsValueGrid;
    private Button mOkButton, mCancelButton;
    private Spinner mOperatorSpinner, mLhsNodeSpinner, mLhsCommandSpinner, mRhsNodeSpinner, mRhsCommandSpinner;
    private Switch mRhsValueSwitch;
    private EditText mRhsValueEditText;

    public interface PropositionDialogCallback{
        void onPropositionResult(Proposition p, int orStatementIndex);
    }

    public static PropositionDialog newInstance(int orStatementIndex, String controllerId){
        Bundle args = new Bundle();
        args.putInt(ARG_OR_STATEMENT_INDEX, orStatementIndex);
        args.putString(ARG_CONTROLLER_ID, controllerId);
        PropositionDialog fragment = new PropositionDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOrStatementIndex = getArguments().getInt(ARG_OR_STATEMENT_INDEX);
        mControllerId = getArguments().getString(ARG_CONTROLLER_ID);
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (PropositionDialogCallback)getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_proposition_dialog, container, false);

        mNodes = ModelStorage.getInstance().getNodes(this);
        List<String> sensors = new ArrayList<>();
        mSpinnerIndexToNodeUId = new ArrayList<>();
        for(NodesResponse.Node n : mNodes){
            if(n.getNodeClass().contains(NodeClassEnum.SENSOR) && n.getControllerId().equals(mControllerId)) {
                mSpinnerIndexToNodeUId.add(n.getNodeId());
                sensors.add(n.getExtra().get(NODE_EXTRA_NAME));
            }
        }

        mRhsNodeGrid = (GridLayout)v.findViewById(R.id.fragment_prop_dialog_rhs_node_grid);
        mRhsValueGrid = (GridLayout)v.findViewById(R.id.fragment_prop_dialog_rhs_value_grid);

        mRhsRadioGroup = (RadioGroup)v.findViewById(R.id.fragment_prop_dialog_rhs_radiogroup);
        mRhsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i == R.id.fragment_prop_dialog_rhs_node_radio_button){
                    mRhsNodeGrid.setVisibility(View.VISIBLE);
                    mRhsValueGrid.setVisibility(View.GONE);
                }
                else if(i == R.id.fragment_prop_dialog_rhs_value_radio_button){
                    mRhsNodeGrid.setVisibility(View.GONE);
                    mRhsValueGrid.setVisibility(View.VISIBLE);
                }
            }
        });
        mRhsRadioGroup.check(R.id.fragment_prop_dialog_rhs_node_radio_button);

        mOperatorSpinner = (Spinner)v.findViewById(R.id.fragment_prop_dialog_operator_spinner);
        mOperatorSpinner.setAdapter(new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.operators)));

        mLhsCommandSpinner = (Spinner)v.findViewById(R.id.fragment_prop_dialog_lhs_command_spinner);

        mLhsNodeSpinner = (Spinner)v.findViewById(R.id.fragment_prop_dialog_lhs_node_spinner);
        mLhsNodeSpinner.setAdapter(new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_spinner_dropdown_item, sensors.toArray(new String[sensors.size()])));
        mLhsNodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                NodesResponse.Node sensor = AppFindElementUtils.findNodeFromId(
                        mSpinnerIndexToNodeUId.get(i), mControllerId, mNodes);
                List<String> dataTypes = new ArrayList<>();
                mLhsCommandSpinnerIndexToCommandId.clear();
                for(NodesResponse.DataType dt : sensor.getDataType()){
                    mLhsCommandSpinnerIndexToCommandId.add(dt.getId());
                    dataTypes.add(AppEnumUtils.dataCategoryToString(getActivity(), dt.getDataCategory()));
                }
                mLhsCommandSpinner.setAdapter(new ArrayAdapter<String>(
                        getActivity(), android.R.layout.simple_spinner_dropdown_item, dataTypes.toArray(new String[dataTypes.size()])));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mRhsCommandSpinner = (Spinner)v.findViewById(R.id.fragment_prop_dialog_rhs_command_spinner);

        mRhsNodeSpinner = (Spinner)v.findViewById(R.id.fragment_prop_dialog_rhs_node_spinner);
        mRhsNodeSpinner.setAdapter(new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_spinner_dropdown_item, sensors.toArray(new String[sensors.size()])));
        mRhsNodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                NodesResponse.Node sensor = AppFindElementUtils.findNodeFromId(
                        mSpinnerIndexToNodeUId.get(i), mControllerId, mNodes);
                List<String> dataTypes = new ArrayList<>();
                mRhsCommandSpinnerIndexToCommandId.clear();
                for(NodesResponse.DataType dt : sensor.getDataType()){
                    mRhsCommandSpinnerIndexToCommandId.add(dataTypes.size(), dt.getId());
                    dataTypes.add(AppEnumUtils.dataCategoryToString(getActivity(), dt.getDataCategory()));
                }
                mRhsCommandSpinner.setAdapter(new ArrayAdapter<String>(
                        getActivity(), android.R.layout.simple_spinner_dropdown_item, dataTypes.toArray(new String[dataTypes.size()])));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mRhsValueEditText = (EditText)v.findViewById(R.id.fragment_prop_dialog_rhs_value_edit_text);
        mRhsValueSwitch = (Switch)v.findViewById(R.id.fragment_prop_dialog_rhs_value_switch);
        mLhsCommandSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                NodesResponse.Node sensor = AppFindElementUtils.findNodeFromId(
                        mSpinnerIndexToNodeUId.get(mLhsNodeSpinner.getSelectedItemPosition()),
                        mControllerId,
                        mNodes);
                NodesResponse.DataType dt = AppFindElementUtils.findDatatypeFromId(
                        mRhsCommandSpinnerIndexToCommandId.get(i), sensor.getDataType());
                switch (dt.getType()){
                    case INT:
                        mRhsValueEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        mRhsValueEditText.setVisibility(View.VISIBLE);
                        mRhsValueSwitch.setVisibility(View.GONE);
                        break;
                    case REAL:
                        mRhsValueEditText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        mRhsValueEditText.setVisibility(View.VISIBLE);
                        mRhsValueSwitch.setVisibility(View.GONE);
                        break;
                    case STRING:
                        mRhsValueEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                        mRhsValueEditText.setVisibility(View.VISIBLE);
                        mRhsValueSwitch.setVisibility(View.GONE);
                        break;
                    case BOOL:
                        mRhsValueEditText.setVisibility(View.GONE);
                        mRhsValueSwitch.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mOkButton = (Button)v.findViewById(R.id.fragment_prop_dialog_ok_button);
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateFields()){
                    OperatorEnum operator = AppEnumUtils.stringToOperator(
                            getActivity(), (String)mOperatorSpinner.getSelectedItem());
                    int lhsNode = mSpinnerIndexToNodeUId.get(mLhsNodeSpinner.getSelectedItemPosition());
                    int lhsCommand = mLhsCommandSpinnerIndexToCommandId.get(mLhsCommandSpinner.getSelectedItemPosition());
                    if(mRhsRadioGroup.getCheckedRadioButtonId() == R.id.fragment_prop_dialog_rhs_node_radio_button){
                        int rhsNode = mSpinnerIndexToNodeUId.get(mRhsNodeSpinner.getSelectedItemPosition());
                        int rhsCommand = mRhsCommandSpinnerIndexToCommandId.get(mRhsCommandSpinner.getSelectedItemPosition());
                        Proposition prop = new Proposition(operator, lhsNode + "." + lhsCommand, rhsNode + "." + rhsCommand);
                        mCallback.onPropositionResult(prop, mOrStatementIndex);
                    }
                    else if(mRhsRadioGroup.getCheckedRadioButtonId() == R.id.fragment_prop_dialog_rhs_value_radio_button){
                        BigDecimal value = null;
                        if(mRhsValueEditText.getVisibility() == View.VISIBLE){
                            value = new BigDecimal(mRhsValueEditText.getText().toString());
                        }
                        else{
                            value = new BigDecimal(mRhsValueSwitch.isChecked()? 1 : 0);
                        }
                        Proposition prop = new Proposition(operator, lhsNode + "." + lhsCommand, value);
                        mCallback.onPropositionResult(prop, mOrStatementIndex);
                    }
                    PropositionDialog.this.dismiss();
                }
            }
        });

        mCancelButton = (Button)v.findViewById(R.id.fragment_prop_dialog_cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PropositionDialog.this.dismiss();
            }
        });


        return v;
    }

    @Override
    public void onPostRequest(SimpleResponse s) {

    }

    private boolean validateFields(){
        if(mRhsRadioGroup.getCheckedRadioButtonId() == R.id.fragment_prop_dialog_rhs_value_radio_button &&
                mRhsValueEditText.getVisibility() == View.VISIBLE && mRhsValueEditText.getText().length() == 0){
            Toast.makeText(
                    getActivity(),
                    getResources().getText(R.string.proposition_dialog_value_error_msg),
                    Toast.LENGTH_LONG).show();
            return false;
        }
        else
            return true;
    }

}
