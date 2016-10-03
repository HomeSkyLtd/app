package com.homesky.homesky.fragments.state;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.homesky.homecloud_lib.model.response.StateResponse;
import com.homesky.homesky.R;
import com.homesky.homesky.fragments.node.NodeActivity;

/**
 * Created by henrique on 9/28/16.
 */

class StateHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView mNodeName;
    private Context mContext;
    private int mId;

    StateHolder (View itemView, Context context) {
        super(itemView);
        mNodeName = (TextView) itemView.findViewById(R.id.state_fragment_state_name);

        itemView.setOnClickListener(this);
        mContext = context;
    }

    void bind (int id) {
        mId = id;
        mNodeName.setText(
                mContext.getResources().getString(
                        R.string.state_fragment_item_row,
                        node.getNodeId()
                )
        );
    }

    @Override
    public void onClick(View v) {
        mContext.startActivity(NodeActivity.newIntent(mContext, mNode));
    }
}
