package com.homesky.homesky.command;

import com.homesky.homecloud_lib.model.response.SimpleResponse;

public interface Command {
    public SimpleResponse execute();
}
