package com.homesky.homesky.command;

import com.homesky.homecloud_lib.exceptions.NetworkException;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homesky.homecloud.HomecloudHolder;

import java.math.BigDecimal;

/**
 * Created by henrique on 10/19/16.
 */

public class AcceptRuleCommand implements Command {

    private int mNodeId, mCommandId;
    private String mControllerId;
    private BigDecimal mValue;

    public AcceptRuleCommand(int nodeId, int commandId, String controllerId, BigDecimal value) {
        mNodeId = nodeId;
        mCommandId = commandId;
        mControllerId = controllerId;
        mValue = value;
    }

    @Override
    public SimpleResponse execute() throws NetworkException {
        return HomecloudHolder.getInstance().acceptRule(mNodeId, mCommandId, mValue, mControllerId);
    }
}
