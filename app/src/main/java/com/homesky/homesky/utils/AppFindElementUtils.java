package com.homesky.homesky.utils;

import com.homesky.homecloud_lib.model.Rule;
import com.homesky.homecloud_lib.model.response.NodesResponse;

import java.util.ArrayList;
import java.util.List;

public class AppFindElementUtils {

    public static NodesResponse.Node findNodeFromId(int id, List<NodesResponse.Node> nodes) {
        for (NodesResponse.Node n : nodes) {
            if (n.getNodeId() == id)
                return n;
        }
        return null;
    }

    public static List<Rule> findRulesFromNodeId(int id, List<Rule> rules){
        List<Rule> filteredRules = new ArrayList<>();
        for(Rule r : rules){
            if(r.getCommand().getNodeId() == id)
                filteredRules.add(r);
        }
        return filteredRules;
    }

    public static NodesResponse.DataType findDatatypeFromId(int id, List<NodesResponse.DataType> types){
        for(NodesResponse.DataType type : types){
            if(type.getId() == id)
                return type;
        }
        return null;
    }

    public static NodesResponse.CommandType findCommandtypeFromId(int id, List<NodesResponse.CommandType> types){
        for(NodesResponse.CommandType type : types){
            if(type.getId() == id)
                return type;
        }
        return null;
    }
}
