package com.homesky.homesky.utils;

import android.content.Context;
import android.text.TextUtils;

import com.homesky.homecloud_lib.model.Proposition;
import com.homesky.homecloud_lib.model.Rule;
import com.homesky.homecloud_lib.model.response.NodesResponse;
import com.homesky.homesky.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fabio on 12/10/2016.
 */

public class AppStringUtils {

    private static final String NODE_EXTRA_NAME = "name";

    public static String getPropositionLegibleText(Context context, Proposition p, List<NodesResponse.Node> nodes){
        StringBuilder sb = new StringBuilder();

        if(p.isLhsValue())
            sb.append(p.getLhs());
        else{
            String[] nodeAndCommand = p.getLhs().split("\\.");
            int nodeId = Integer.parseInt(nodeAndCommand[0]);
            int dataTypeId = Integer.parseInt(nodeAndCommand[1]);
            NodesResponse.Node node = AppFindElementUtils.findNodeFromId(nodeId, nodes);
            sb.append(node.getExtra().get(NODE_EXTRA_NAME));
            sb.append("'s ");
            sb.append(AppEnumUtils.dataCategoryToString(context,
                    AppFindElementUtils.findDatatypeFromId(dataTypeId, node.getDataType()).getDataCategory()));
        }

        sb.append(" ");
        sb.append(AppEnumUtils.operatorToString(context, p.getOperator()));
        sb.append(" ");

        if(p.isRhsValue())
            sb.append(p.getRhs());
        else{
            String[] nodeAndCommand = p.getRhs().split("\\.");
            int nodeId = Integer.parseInt(nodeAndCommand[0]);
            int dataTypeId = Integer.parseInt(nodeAndCommand[1]);
            NodesResponse.Node node = AppFindElementUtils.findNodeFromId(nodeId, nodes);
            sb.append(node.getExtra().get(NODE_EXTRA_NAME));
            sb.append("'s ");
            sb.append(AppEnumUtils.dataCategoryToString(context,
                    AppFindElementUtils.findDatatypeFromId(dataTypeId, node.getDataType()).getDataCategory()));
        }
        return sb.toString();
    }

    public static String getRuleConditionLegibleText(Context context, Rule r, List<NodesResponse.Node> nodes){
        List<List<Proposition>> clause = r.getClause();

        List<String> orParts = new ArrayList<>();
        for(List<Proposition> andStatement : clause){
            List<String> andParts = new ArrayList<>();
            for(Proposition p : andStatement)
                andParts.add(getPropositionLegibleText(context, p, nodes));
            orParts.add(TextUtils.join(" " + context.getString(R.string.rule_list_and) + " ", andParts));
        }
        return TextUtils.join(" " + context.getString(R.string.rule_list_or) + " ", orParts);
    }

    public static String getRuleEffectLegibleText(Context context, Rule r, List<NodesResponse.Node> nodes){
        StringBuilder sb = new StringBuilder();
        sb.append(context.getString(R.string.rule_list_set));
        sb.append(" ");
        NodesResponse.Node node = AppFindElementUtils.findNodeFromId(r.getCommand().getNodeId(), nodes);
        sb.append(node.getExtra().get(NODE_EXTRA_NAME));
        sb.append("'s ");
        sb.append(AppEnumUtils.commandCategoryToString(context,
                AppFindElementUtils.findCommandtypeFromId(r.getCommand().getCommandId(), node.getCommandType()).getCommandCategory()
        ));
        sb.append(" to ");
        sb.append(r.getCommand().getValue());

        return sb.toString();
    }
}
