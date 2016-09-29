package com.homesky.homesky.state;

/**
 * Created by henrique on 9/28/16.
 */

class State {
    private String mStateName;

    public State(String stateName) {
        mStateName = stateName;
    }

    public String getStateName() {
        return mStateName;
    }

    public void setStateName(String stateName) {
        mStateName = stateName;
    }
}
