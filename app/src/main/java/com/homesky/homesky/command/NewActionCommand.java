package com.homesky.homesky.command;

import com.homesky.homecloud_lib.exceptions.NetworkException;
import com.homesky.homesky.homecloud.HomecloudHolder;
import com.homesky.homecloud_lib.model.response.SimpleResponse;

import java.math.BigDecimal;

public class NewActionCommand implements Command{

    private int mNodeId, mCommandId;
    private String mControllerId;
    private BigDecimal mValue;

    public NewActionCommand(int nodeId, String controllerId, int commandId, BigDecimal value) {
        mNodeId = nodeId;
        mControllerId = controllerId;
        mCommandId = commandId;
        mValue = value;
    }

    @Override
    public SimpleResponse execute() throws NetworkException {
        return HomecloudHolder.getInstance().newAction(mNodeId, mControllerId, mCommandId, mValue);
    }
}
