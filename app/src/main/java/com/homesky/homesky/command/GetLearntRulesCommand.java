package com.homesky.homesky.command;

import com.homesky.homesky.homecloud.HomecloudHolder;
import com.homesky.homecloud_lib.model.response.SimpleResponse;

public class GetLearntRulesCommand implements Command {
    @Override
    public SimpleResponse execute() {
        return HomecloudHolder.getInstance().getLearntRules();
    }
}
