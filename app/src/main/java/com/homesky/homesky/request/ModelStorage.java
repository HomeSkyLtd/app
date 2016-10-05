package com.homesky.homesky.request;

import android.util.Log;

import com.homesky.homecloud_lib.model.Rule;
import com.homesky.homecloud_lib.model.response.NodesResponse;
import com.homesky.homecloud_lib.model.response.RuleResponse;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homecloud_lib.model.response.StateResponse;
import com.homesky.homesky.command.GetHouseStateCommand;
import com.homesky.homesky.command.GetNodesInfoCommand;
import com.homesky.homesky.command.GetRulesCommand;

import java.util.List;

public class ModelStorage implements RequestCallback{
    private static String TAG = "ModelStorage";

    private static ModelStorage instance = null;

    private List<NodesResponse.Node> mNodes = null;
    private List<StateResponse.NodeState> mNodeStates = null;
    private List<Rule> mRules = null;

    public static ModelStorage getInstance(){
        if(instance == null){
            instance = new ModelStorage();
        }
        return instance;
    }

    public List<NodesResponse.Node> getNodes(RequestCallback source){
        if(mNodes != null){
            return mNodes;
        }
        else{
            new AsyncRequest(this, source).execute(new GetNodesInfoCommand());
            return null;
        }
    }

    public List<StateResponse.NodeState> getNodeStates(RequestCallback source){

        if(mNodeStates != null){
            return mNodeStates;
        }
        else{
            new AsyncRequest(this, source).execute(new GetHouseStateCommand());
            return null;
        }
    }

    public List<Rule> getRules(RequestCallback source){
        if(mRules != null){
            return mRules;
        }
        else{
            new AsyncRequest(this, source).execute(new GetRulesCommand());
            return null;
        }
    }

    public void invalidateNodesCache(){
        mNodes = null;
    }

    public void invalidateNodeStatesCache(){
        mNodeStates = null;
    }

    public void invalidateRulesCache(){
        mRules = null;
    }


    @Override
    public void onPostRequest(SimpleResponse s) {
        if(s instanceof NodesResponse){
            mNodes = ((NodesResponse) s).getNodes();
        }
        else if(s instanceof StateResponse){
            mNodeStates = ((StateResponse) s).getState();
        }
        else if(s instanceof RuleResponse){
            mRules = ((RuleResponse) s).getRules();
            Log.d(TAG, "Received " + mRules.size() + " rules");
        }
    }
}
