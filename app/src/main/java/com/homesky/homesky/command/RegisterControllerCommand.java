package com.homesky.homesky.command;

import com.homesky.homecloud_lib.exceptions.NetworkException;
import com.homesky.homesky.homecloud.HomecloudHolder;
import com.homesky.homecloud_lib.model.response.SimpleResponse;

public class RegisterControllerCommand implements Command {
    private String mControllerId;

    public RegisterControllerCommand(String controllerId){
        mControllerId = controllerId;
    }

    @Override
    public SimpleResponse execute() throws NetworkException {
        return HomecloudHolder.getInstance().registerController(mControllerId);
    }
}
