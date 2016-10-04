package com.homesky.homesky.request;

import com.homesky.homecloud_lib.model.Rule;
import com.homesky.homecloud_lib.model.response.NodesResponse;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homecloud_lib.model.response.StateResponse;
import com.homesky.homesky.command.GetHouseStateCommand;
import com.homesky.homesky.command.GetNodesInfoCommand;
import com.homesky.homesky.command.GetRulesCommand;

import java.util.List;

public class ModelStorage {
    private ModelStorage mInstance = null;

    private List<NodesResponse.Node> mNodes = null;
    private List<StateResponse.NodeState> mNodeStates = null;
    private List<Rule> mRules = null;

    public ModelStorage getInstance(){
        if(mInstance == null){
            mInstance = new ModelStorage();
        }
        return mInstance;
    }

    public List<NodesResponse.Node> getNodes(RequestCallback source){
        if(mNodes != null){
            return mNodes;
        }
        else{
            new AsyncRequest(source).execute(new GetNodesInfoCommand());
            return null;
        }
    }

    public List<StateResponse.NodeState> getNodeStates(RequestCallback source){
        if(mNodeStates != null){
            return mNodeStates;
        }
        else{
            new AsyncRequest(source).execute(new GetHouseStateCommand());
            return null;
        }
    }

    public List<Rule> getRules(RequestCallback source){
        if(mRules != null){
            return mRules;
        }
        else{
            new AsyncRequest(source).execute(new GetRulesCommand());
            return null;
        }
    }

}
