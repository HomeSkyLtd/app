package com.homesky.homesky.request;

import android.os.AsyncTask;
import android.util.Log;

import com.homesky.homecloud_lib.exceptions.NetworkException;
import com.homesky.homecloud_lib.model.Constants;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homesky.command.Command;

import java.util.Formatter;

/**
 * Created by henrique on 9/30/16.
 */

public class AsyncRequest extends AsyncTask<Command, Void, SimpleResponse> {
    private static final String TAG = "AsyncTask";

    RequestCallback mCallback;

    public AsyncRequest(RequestCallback callback) {
        mCallback = callback;
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

        if (mCallback != null)
            mCallback.onPostRequest(simpleResponse);
    }
}
