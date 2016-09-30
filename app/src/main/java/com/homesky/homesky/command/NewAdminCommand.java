package com.homesky.homesky.command;

import com.homesky.homesky.homecloud.HomecloudHolder;
import com.homesky.homecloud_lib.model.response.SimpleResponse;

/**
 * Created by fabio on 02/09/2016.
 */
public class NewAdminCommand implements Command {
    private String mUsername;
    private String mPassword;

    public NewAdminCommand(String username, String password) {
        mUsername = username;
        mPassword = password;
    }
    @Override
    public SimpleResponse execute() {
        return HomecloudHolder.getInstance().newAdmin(mUsername, mPassword);
    }
}
