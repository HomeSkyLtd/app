package com.homesky.homesky.fragments.state;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.homesky.homesky.R;

/**
 * Created by henrique on 9/28/16.
 */

class StateHolder extends RecyclerView.ViewHolder {

    private TextView mNodeName;
    private Context mContext;

    StateHolder (View itemView, Context context) {
        super(itemView);
        mContext = context;
        mNodeName = (TextView) itemView.findViewById(R.id.state_fragment_state_name);
    }

    void bind (Node node) {
        mNodeName.setText(
                        mContext.getResources().getString(R.string.state_fragment_item_row,
                        node.getId()));
    }
}
