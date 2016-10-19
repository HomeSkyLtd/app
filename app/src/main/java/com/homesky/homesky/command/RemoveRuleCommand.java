package com.homesky.homesky.command;

import com.homesky.homecloud_lib.exceptions.NetworkException;
import com.homesky.homecloud_lib.model.Rule;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homesky.homecloud.HomecloudHolder;

public class RemoveRuleCommand implements Command{

    private Rule mRule;

    public RemoveRuleCommand(Rule rule){
        mRule = rule;
    }

    @Override
    public SimpleResponse execute() throws NetworkException {
        return HomecloudHolder.getInstance().removeRule(mRule);
    }
}
