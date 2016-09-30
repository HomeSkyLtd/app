package com.homesky.homesky.command;

import com.homesky.homecloud_lib.exceptions.NetworkException;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homesky.homecloud.HomecloudHolder;

public class RemoveNodeCommand implements Command{
    int mNodeId;
    String mControllerId;

    public RemoveNodeCommand(int nodeId, String controllerId) {
        mNodeId = nodeId;
        mControllerId = controllerId;
    }

    @Override
    public SimpleResponse execute() throws NetworkException {
        return HomecloudHolder.getInstance().removeNode(mNodeId, mControllerId);
    }
}
