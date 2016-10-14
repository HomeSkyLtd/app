package com.homesky.homesky.fragments.node;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.widget.Toast;

import com.homesky.homecloud_lib.model.response.NodesResponse;
import com.homesky.homecloud_lib.model.response.StateResponse;
import com.homesky.homesky.activities.MenuFragmentsActivity;
import com.homesky.homesky.activities.SingleFragmentActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by henrique on 10/3/16.
 */

public class NodeActivity extends SingleFragmentActivity {

    static final String EXTRA_NODE_ID = "com.homesky.homesky.fragments.node.node_id";
    static final String EXTRA_CONTROLLER_ID = "com.homesky.homesky.fragments.node.controller_id";

    public static Intent newIntent(Context context, int nodeId, String controllerId) {
        Intent intent = new Intent(context, NodeActivity.class);
        intent.putExtra(EXTRA_NODE_ID, nodeId);
        intent.putExtra(EXTRA_CONTROLLER_ID, controllerId);
        return intent;
    }

    @Override
    public Fragment createFragment() {
        return new NodeFragment();
    }
}
