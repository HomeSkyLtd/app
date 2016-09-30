package com.homesky.homesky.command;

import com.homesky.homecloud_lib.exceptions.NetworkException;
import com.homesky.homesky.homecloud.HomecloudHolder;
import com.homesky.homecloud_lib.model.response.SimpleResponse;

public class AcceptNodeCommand implements Command{
    int mNodeId, mAccept;
    String mControllerId;

    public AcceptNodeCommand(int nodeId,String controllerId, int accept) {
        mNodeId = nodeId;
        this.mAccept = accept;
        this.mControllerId = controllerId;
    }

    @Override
    public SimpleResponse execute() throws NetworkException {
        return HomecloudHolder.getInstance().acceptNode(mNodeId, mControllerId, mAccept);
    }
}
