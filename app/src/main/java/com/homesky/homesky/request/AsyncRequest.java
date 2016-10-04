package com.homesky.homesky.request;

import android.os.AsyncTask;
import android.util.Log;

import com.homesky.homecloud_lib.exceptions.NetworkException;
import com.homesky.homecloud_lib.model.Constants;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homesky.command.Command;

import java.util.Formatter;

public class AsyncRequest extends AsyncTask<Command, Void, SimpleResponse> {
    private static final String TAG = "AsyncTask";

    RequestCallback[] mCallback = new RequestCallback[2];

    public AsyncRequest(RequestCallback callback) {
        mCallback[0] = callback;
    }

    public AsyncRequest(RequestCallback modelStorageCallback, RequestCallback fragmentCallback) {
        mCallback[0] = modelStorageCallback;
        mCallback[1] = fragmentCallback;
    }

    @Override
    protected SimpleResponse doInBackground(Command... params) {
        SimpleResponse response;
        try{
            return response = params[0].execute();
        } catch(NetworkException e){
            Log.e(TAG, "NetworkException caught: " + e.getMessage());
            Formatter f = new Formatter();
            f.format("{'%s': 0, '%s': '%s'}",
                    Constants.Fields.Common.STATUS,
                    Constants.Fields.Common.ERROR_MESSAGE,
                    e.getMessage()
            );
            return SimpleResponse.from(f.toString());
        }
    }

    @Override
    protected void onPostExecute(SimpleResponse simpleResponse) {
        super.onPostExecute(simpleResponse);

        for(RequestCallback cb : mCallback) {
            if (cb != null)
                cb.onPostRequest(simpleResponse);
        }
    }
}
