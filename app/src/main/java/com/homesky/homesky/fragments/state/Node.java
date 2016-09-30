package com.homesky.homesky.fragments.state;

/**
 * Created by henrique on 9/28/16.
 */

class Node {
    private String mName;
    private int mId;

    public Node() {}

    public String getNodeName() {
        return mName;
    }

    public Node setNodeName(String nodeName) {
        mName = nodeName;
        return this;
    }

    public int getId() {
        return mId;
    }

    public Node setId(int id) {
        mId = id;
        return this;
    }
}
