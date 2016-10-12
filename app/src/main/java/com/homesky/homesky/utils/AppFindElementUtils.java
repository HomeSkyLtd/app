package com.homesky.homesky.utils;

import com.homesky.homecloud_lib.model.response.NodesResponse;

import java.util.List;

public class AppFindElementUtils {

    public static NodesResponse.Node findNodeFromId(int id, List<NodesResponse.Node> nodes) {
        for (NodesResponse.Node n : nodes) {
            if (n.getNodeId() == id)
                return n;
        }
        return null;
    }
}
