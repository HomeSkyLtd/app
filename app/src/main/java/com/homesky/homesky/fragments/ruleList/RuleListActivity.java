package com.homesky.homesky.fragments.ruleList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.homesky.homecloud_lib.model.response.NodesResponse;
import com.homesky.homesky.activities.SingleFragmentActivity;
import com.homesky.homesky.fragments.node.NodeFragment;


public class RuleListActivity extends SingleFragmentActivity {

    public static final String EXTRA_NODE_ID = "com.homesky.homesky.extra_node";

    private int mNodeId;

    public static Intent newIntent(Context context, NodesResponse.Node node){
        Intent i = new Intent(context, RuleListActivity.class);
        i.putExtra(EXTRA_NODE_ID, node.getNodeId());
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mNodeId = getIntent().getIntExtra(EXTRA_NODE_ID, -1);
        super.onCreate(savedInstanceState);
    }

    @Override
    public Fragment createFragment() {
        return RuleListFragment.newInstance(mNodeId);
    }
}
