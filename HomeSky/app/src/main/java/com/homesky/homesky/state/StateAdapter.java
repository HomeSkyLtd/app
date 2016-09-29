package com.homesky.homesky.state;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.homesky.homesky.R;

import java.util.List;

/**
 * Created by henrique on 9/28/16.
 */

class StateAdapter extends RecyclerView.Adapter<StateHolder> {

    private Context mContext;
    private List<State> mStates;

    StateAdapter(Context context, List<State> states) {
        mContext = context;
        mStates = states;
    }

    @Override
    public StateHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.list_item_state, parent, false);

        return new StateHolder(view);
    }

    @Override
    public void onBindViewHolder(StateHolder holder, int position) {
        State state = mStates.get(position);
        holder.bind(state);
    }

    @Override
    public int getItemCount() {
        return mStates.size();
    }
}
