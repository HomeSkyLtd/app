package com.homesky.homesky.fragments.state;

/**
 * Created by henrique on 9/28/16.
 */

class Node {
    private String mName;
    private int mId;

    public Node(String nodeName) {
        mName = nodeName;
    }

    public String getNodeName() {
        return mName;
    }

    public void setNodeName(String nodeName) {
        mName = nodeName;
    }
}
