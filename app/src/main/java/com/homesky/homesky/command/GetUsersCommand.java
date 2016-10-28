package com.homesky.homesky.command;

import com.homesky.homecloud_lib.exceptions.NetworkException;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homesky.homecloud.HomecloudHolder;

public class GetUsersCommand implements Command {
    @Override
    public SimpleResponse execute() throws NetworkException {
        return HomecloudHolder.getInstance().getUsers();
    }
}
