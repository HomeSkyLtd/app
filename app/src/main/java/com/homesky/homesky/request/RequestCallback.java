package com.homesky.homesky.request;

import com.homesky.homecloud_lib.model.response.SimpleResponse;

/**
 * Created by henrique on 9/30/16.
 */

public interface RequestCallback {
    void onPostRequest(SimpleResponse s);
}

