package com.homesky.homesky.request;

import android.os.AsyncTask;

import com.homesky.homecloud_lib.exceptions.NetworkException;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homesky.command.Command;

/**
 * Created by henrique on 9/30/16.
 */

public class AsyncRequest extends AsyncTask<Command, Void, SimpleResponse> {

    RequestCallback mCallback;

    public AsyncRequest(RequestCallback callback) {
        mCallback = callback;
    }

    @Override
    protected SimpleResponse doInBackground(Command... params) {
        try {
            return params[0].execute();
        } catch (NetworkException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(SimpleResponse simpleResponse) {
        super.onPostExecute(simpleResponse);

        if (mCallback != null)
            mCallback.onPostRequest(simpleResponse);
    }
}
