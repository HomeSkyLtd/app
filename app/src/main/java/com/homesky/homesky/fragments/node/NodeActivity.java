package com.homesky.homesky.fragments.node;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.homesky.homecloud_lib.model.response.StateResponse;
import com.homesky.homesky.activities.MenuFragmentsActivity;
import com.homesky.homesky.activities.SingleFragmentActivity;

/**
 * Created by henrique on 10/3/16.
 */

public class NodeActivity extends SingleFragmentActivity {

    static final String EXTRA_NODE = "com.homesky.homesky.fragments.node.node";

    public static Intent newIntent(Context context, int id) {
        Intent intent = new Intent(context, NodeActivity.class);
        intent.putExtra(EXTRA_NODE, node);
        return intent;
    }

    @Override
    public Fragment createFragment() {
        return new NodeFragment();
    }
}
