package com.homesky.homesky.fragments.state;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.homesky.homecloud_lib.model.response.StateResponse;
import com.homesky.homesky.R;

import java.util.List;

/**
 * Created by henrique on 9/28/16.
 */

class StateAdapter extends RecyclerView.Adapter<StateHolder> {

    private Context mContext;
    private List<StateResponse.NodeState> mNodes;

    StateAdapter(Context context, List<StateResponse.NodeState> nodes) {
        mContext = context;
        mNodes = nodes;
    }

    @Override
    public StateHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.list_item_state, parent, false);

        return new StateHolder(view, mContext);
    }

    @Override
    public void onBindViewHolder(StateHolder holder, int position) {
        StateResponse.NodeState mNode = mNodes.get(position);
        holder.bind(mNode);
    }

    @Override
    public int getItemCount() {
        return mNodes.size();
    }

    public void add(StateResponse.NodeState node) {
        mNodes.add(node);
    }
}
