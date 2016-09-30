package com.homesky.homesky.command;

import com.homesky.homecloud_lib.exceptions.NetworkException;
import com.homesky.homesky.homecloud.HomecloudHolder;
import com.homesky.homecloud_lib.model.response.SimpleResponse;

import java.util.Map;

public class SetNodeExtraCommand implements Command {
    Map<String, String> mExtra;
    private int mNodeId;
    private String mControllerId;

    public SetNodeExtraCommand(Map<String, String> extra, int nodeId, String controllerId){
        mExtra = extra;
        mNodeId = nodeId;
        mControllerId = controllerId;
    }

    @Override
    public SimpleResponse execute() throws NetworkException {
        return HomecloudHolder.getInstance().setNodeExtra(mExtra, mNodeId, mControllerId);
    }
}
