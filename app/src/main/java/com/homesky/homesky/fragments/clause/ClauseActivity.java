package com.homesky.homesky.fragments.clause;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.homesky.homecloud_lib.model.Proposition;
import com.homesky.homecloud_lib.model.response.NodesResponse;
import com.homesky.homesky.activities.SingleFragmentActivity;

public class ClauseActivity extends SingleFragmentActivity implements PropositionDialog.PropositionDialogCallback {
    public static final String EXTRA_NODE_ID = "com.homesky.homesky.extra_node_id";
    public static final String EXTRA_CONTROLLER_ID = "com.homesky.homesky.extra_controller_id";

    private int mNodeId;
    private String mControllerId;
    private Fragment mFragment;

    public static Intent newIntent(Context context, int nodeId, String controllerId){
        Intent i = new Intent(context, ClauseActivity.class);
        i.putExtra(EXTRA_NODE_ID, nodeId);
        i.putExtra(EXTRA_CONTROLLER_ID, controllerId);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mNodeId = getIntent().getIntExtra(EXTRA_NODE_ID, -1);
        mControllerId = getIntent().getStringExtra(EXTRA_CONTROLLER_ID);
        super.onCreate(savedInstanceState);
    }

    @Override
    public Fragment createFragment() {
        mFragment = ClauseFragment.newInstance(mNodeId, mControllerId);
        return mFragment;
    }

    @Override
    public void onPropositionResult(Proposition p, int orStatementIndex) {
        ((PropositionDialog.PropositionDialogCallback)mFragment).onPropositionResult(p, orStatementIndex);
    }
}
