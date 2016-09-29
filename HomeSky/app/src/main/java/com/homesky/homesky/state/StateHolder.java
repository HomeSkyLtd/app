package com.homesky.homesky.state;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.homesky.homesky.R;

/**
 * Created by henrique on 9/28/16.
 */

class StateHolder extends RecyclerView.ViewHolder {

    private TextView mStateName;

    StateHolder (View itemView) {
        super(itemView);
        mStateName = (TextView) itemView.findViewById(R.id.state_fragment_state_name);
    }

    void bind (State state) {
        mStateName.setText(state.getStateName());
    }
}
