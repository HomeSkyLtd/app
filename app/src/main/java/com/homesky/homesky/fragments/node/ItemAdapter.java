package com.homesky.homesky.fragments.node;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.homesky.homecloud_lib.model.response.NodesResponse;
import com.homesky.homesky.R;

import java.util.List;

/**
 * Created by henrique on 10/3/16.
 */

class ItemAdapter extends RecyclerView.Adapter<ItemHolder> {

    private Context mContext;

    ItemAdapter(Context context, List<NodesResponse.Node> nodeInfos) {
        mContext = context;
        mNodeInfos = nodeInfos;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.node_list_item, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        NodesResponse.Node node = mNodeInfos.get(position);
        holder.bind(node);
    }

    @Override
    public int getItemCount() {
        return mNodeInfos.size();
    }
}
