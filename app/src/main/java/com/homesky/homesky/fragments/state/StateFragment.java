package com.homesky.homesky.fragments.state;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.homesky.homecloud_lib.model.enums.TypeEnum;
import com.homesky.homecloud_lib.model.response.NodesResponse;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homecloud_lib.model.response.StateResponse;
import com.homesky.homesky.R;
import com.homesky.homesky.command.GetHouseStateCommand;
import com.homesky.homesky.command.GetNodesInfoCommand;
import com.homesky.homesky.command.LogoutCommand;
import com.homesky.homesky.command.NewActionCommand;
import com.homesky.homesky.fragments.node.NodeActivity;
import com.homesky.homesky.fragments.node.NodeFragment;
import com.homesky.homesky.fragments.notification.FirebaseBackgroundService;
import com.homesky.homesky.homecloud.HomecloudHolder;
import com.homesky.homesky.login.LoginActivity;
import com.homesky.homesky.request.AsyncRequest;
import com.homesky.homesky.request.ModelStorage;
import com.homesky.homesky.request.RequestCallback;
import com.homesky.homecloud_lib.model.response.StateResponse.NodeState;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.view.View.GONE;

/**
 * Created by henrique on 9/22/16.
 */
public class StateFragment extends Fragment {

    private static final String TAG = "StateFragment";

    private RecyclerView mListOfNodes;
    private StateAdapter mStateAdapter;
    private RelativeLayout mLoadingPanel;
    private SwipeRefreshLayout mStateSwipeRefresh;
    private TextView mEmptyListMessage;

    private int mAttemptsCounter = 0;
    private static final int sNumberOfAttempts = 3;

    public StateFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_state, container, false);

        mEmptyListMessage = (TextView) view.findViewById(R.id.state_fragment_empty_list_message);
        mLoadingPanel = (RelativeLayout) view.findViewById(R.id.state_fragment_loading_panel);
        mListOfNodes = (RecyclerView) view.findViewById(R.id.state_fragment_list_nodes);
        mListOfNodes.setLayoutManager(new LinearLayoutManager(getActivity()));

        mStateSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.state_fragment_swipe_refresh_layout);
        mStateSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ModelStorage.getInstance().invalidateNodesCache();
                mAttemptsCounter = 0;
                updateUI();
                mStateSwipeRefresh.setRefreshing(false);
            }
        });

        updateUI();

        return view;
    }

    private void updateUI() {
        if (getActivity() == null) return;

        if (mAttemptsCounter > sNumberOfAttempts) {
            mAttemptsCounter = 0;
            mLoadingPanel.setVisibility(View.GONE);
            mStateSwipeRefresh.setRefreshing(false);
            ((NodeActivity) getActivity()).lockActivity(false, null);
            Snackbar.make(
                    getActivity().findViewById(R.id.fragment_container),
                    getActivity().getResources().getText(R.string.node_fragment_no_connection),
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        if (mStateAdapter == null) {
            mStateAdapter = new StateAdapter();
        } else {
            List<NodesResponse.Node> list = ModelStorage.getInstance().getNodes(new GetNodesInfoRequest());

            if (list != null) {
                mLoadingPanel.setVisibility(View.GONE);
                mListOfNodes.setVisibility(View.VISIBLE);

                if (list.isEmpty()) {
                    mEmptyListMessage.setVisibility(View.VISIBLE);
                } else {
                    mEmptyListMessage.setVisibility(View.GONE);
                }
            }

            mStateAdapter.setNodes(list);
            mStateAdapter.notifyDataSetChanged();
        }

        mListOfNodes.setAdapter(mStateAdapter);
    }

    class GetNodesInfoRequest implements RequestCallback {
        @Override
        public void onPostRequest(SimpleResponse s) {
            if(!StateFragment.this.isAdded()) return;

            if (s == null) {
                Toast.makeText(
                        getActivity(),
                        getResources().getText(R.string.state_fragment_error),
                        Toast.LENGTH_LONG).show();
            } else if (s.getStatus() == 0 && s.getErrorMessage().equals(AsyncRequest.NOT_CREDENTIALS_ERROR)) {
                getActivity().startActivity(LoginActivity.newIntent(getActivity(), LoginActivity.LoginAction.LOGIN));
            } else if (s.getStatus() == 0) {
                Toast.makeText(
                        getActivity(),
                        getResources().getText(R.string.login_fragment_connection_failed),
                        Toast.LENGTH_LONG).show();
            } else if (s.getStatus() == 403){
                HomecloudHolder.getInstance().invalidateSession();
                getActivity().startActivity(LoginActivity.newIntent(getActivity(), LoginActivity.LoginAction.LOGIN));
            } else {
                updateUI();
            }

            mLoadingPanel.setVisibility(View.GONE);
            mListOfNodes.setVisibility(View.VISIBLE);
        }
    }














    /* State adapter */

    class StateAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<Object> mNodes;
        private static final int ITEM_STATE = 0;
        private static final int ITEM_HEADER = 1;

        StateAdapter() {
            mNodes = new ArrayList<>();

            setNodes(ModelStorage.getInstance().getNodes(new GetNodesInfoRequest()));
            if (!mNodes.isEmpty()) {
                mLoadingPanel.setVisibility(View.GONE);
                mListOfNodes.setVisibility(View.VISIBLE);
                mEmptyListMessage.setVisibility(GONE);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            if (viewType == ITEM_STATE) {
                View view = layoutInflater.inflate(R.layout.list_item_state, parent, false);
                return new StateHolder(view);
            } else if (viewType == ITEM_HEADER) {
                View view = layoutInflater.inflate(R.layout.list_header, parent, false);
                return new HeaderHolder(view);
            }

            return null;
        }

        @Override
        public int getItemViewType(int position) {
            if (mNodes.get(position) instanceof String) {
                return ITEM_HEADER;
            } else {
                return ITEM_STATE;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int itemType = getItemViewType(position);

            if (itemType == ITEM_HEADER) {
                ((HeaderHolder) holder).bind((String) mNodes.get(position));
            } else if (itemType == ITEM_STATE) {
                ((StateHolder) holder).bind((NodesResponse.Node) mNodes.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return mNodes.size();
        }

        void setNodes (List<NodesResponse.Node> nodes) {
            if (nodes == null || nodes.isEmpty()) return;

            Map<String, List<NodesResponse.Node>> nodesPerRoom = new HashMap<>();

            for (NodesResponse.Node n : nodes) {
                if (n.getAccepted() == 1) {
                    String room = n.getExtra().get("room");
                    if (nodesPerRoom.containsKey(room)) {
                        nodesPerRoom.get(room).add(n);
                    } else {
                        List<NodesResponse.Node> list = new LinkedList<>();
                        list.add(n);
                        nodesPerRoom.put(room, list);
                    }
                }
            }

            mNodes = new ArrayList<>();
            for (Map.Entry entry : nodesPerRoom.entrySet()) {
                mNodes.add(entry.getKey());
                mNodes.addAll((Collection<?>) entry.getValue());
            }
        }
    }

    class StateHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private static final String DIALOG_TAG = "state_holder_dialog_tag";

        private TextView mNodeName;
        private ImageView mNodeIcon;

        private int mNodeId;
        private String mControllerId;
        private NodesResponse.Node mNode;

        StateHolder (View itemView) {
            super(itemView);
            mNodeName = (TextView) itemView.findViewById(R.id.state_fragment_node_name);

            mNodeIcon = (ImageView) itemView.findViewById(R.id.list_item_state_icon);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        void bind (NodesResponse.Node node) {
            mNodeId = node.getNodeId();
            mControllerId = node.getControllerId();

            mNodeName.setText(node.getExtra().get("name"));

            mNode = node;

            if (mNode.getCommandType().isEmpty()) {
                mNodeIcon.setImageResource(R.drawable.ic_developer_board_black_24dp);
            } else {
                mNodeIcon.setImageResource(R.drawable.ic_touch_app_black_24dp);
            }
        }

        @Override
        public void onClick(View v) {
            startActivity(NodeActivity.newIntent(getActivity(), mNodeId, mControllerId));
        }

        @Override
        public boolean onLongClick(View v) {
            ShowInfosDialogFragment.newInstance(mNode).show(getFragmentManager(), DIALOG_TAG);
            return true;
        }
    }

    class HeaderHolder extends RecyclerView.ViewHolder {

        private TextView mHeaderName;

        HeaderHolder(View itemView) {
            super(itemView);
            mHeaderName = (TextView) itemView.findViewById(R.id.list_item_header);
        }

        void bind(String header) {
            mHeaderName.setText(header);
        }
    }

    public static class ShowInfosDialogFragment extends DialogFragment {

        private NodesResponse.Node mNode;
        private TextView mTitle;
        private TextView mControllerId;
        private TextView mNodeId;
        private TextView mNodeRoom;
        private TextView mNodeAlive;

        public void setNode(NodesResponse.Node node) {
            mNode = node;
        }

        static ShowInfosDialogFragment newInstance(NodesResponse.Node node) {
            ShowInfosDialogFragment fragment = new ShowInfosDialogFragment();

            fragment.setNode(node);

            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialogInfos);
            return super.onCreateDialog(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.node_infos_dialog_fragment, container, false);

            getDialog().setCanceledOnTouchOutside(true);
            Window window  = getDialog().getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(R.drawable.round_dialog);
            }

            mTitle = (TextView) view.findViewById(R.id.node_infos_dialog_fragment_name);
            mTitle.setText(mNode.getExtra().get("name"));

            mControllerId = (TextView) view.findViewById(R.id.node_infos_dialog_fragment_controller_id);
            mControllerId.setText(mNode.getControllerId());

            mNodeId = (TextView) view.findViewById(R.id.node_infos_dialog_fragment_node_id);
            String nodeId = String.valueOf(mNode.getNodeId());
            mNodeId.setText(nodeId);

            mNodeRoom = (TextView) view.findViewById(R.id.node_infos_dialog_fragment_node_room);
            mNodeRoom.setText(mNode.getExtra().get("room"));

            mNodeAlive = (TextView) view.findViewById(R.id.node_infos_dialog_fragment_node_alive);
            String alive = mNode.getAlive() == 0 ?
                            getActivity().getResources().getString(R.string.no) :
                            getActivity().getResources().getString(R.string.yes);
            mNodeAlive.setText(alive);

            return view;
        }
    }


}
