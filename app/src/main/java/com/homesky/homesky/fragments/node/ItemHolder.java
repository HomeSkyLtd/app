package com.homesky.homesky.fragments.node;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.homesky.homesky.R;

/**
 * Created by henrique on 10/3/16.
 */

class ItemHolder extends RecyclerView.ViewHolder {

    private TextView mId;
    private TextView mValue;

    ItemHolder(View itemView) {
        super(itemView);

        mId = (TextView) itemView.findViewById(R.id.node_list_item_id);
        mValue = (TextView) itemView.findViewById(R.id.node_list_item_value);
    }

    void bind(NodeInfo nodeInfo) {
        mId.setText(nodeInfo.getId());
        mValue.setText(nodeInfo.getValue().toEngineeringString());
    }
}
