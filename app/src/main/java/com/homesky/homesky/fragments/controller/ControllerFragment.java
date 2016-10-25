package com.homesky.homesky.fragments.controller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.homesky.homecloud_lib.model.response.ControllerDataResponse;
import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homesky.R;
import com.homesky.homesky.fragments.ruleList.RuleListFragment;
import com.homesky.homesky.homecloud.HomecloudHolder;
import com.homesky.homesky.login.LoginActivity;
import com.homesky.homesky.request.ModelStorage;
import com.homesky.homesky.request.RequestCallback;
import com.homesky.homesky.utils.VerticalSpaceItemDecoration;

import java.util.List;

public class ControllerFragment extends Fragment implements RequestCallback {
    private static final String TAG = "ControllerFrag";

    enum PageState{
        LOADING, REFRESHING, SENDING_NEW_CONTROLLER, IDLE
    }
    private PageState mPageState;

    private List<String> mControllerIds;
    private ControllerAdapter mAdapter;

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mControllerSwipeRefresh;
    private FloatingActionButton mFloatingActionButton;
    private ProgressDialog mRingProgressDialog;
    private RelativeLayout mLoadingLayout;
    private TextView mNoInternetTextView;


    public ControllerFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_controller, container, false);

        mRecyclerView = (RecyclerView)view.findViewById(R.id.controller_fragment_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(48));

        mControllerSwipeRefresh = (SwipeRefreshLayout)view.findViewById(R.id.controller_fragment_swipe_refresh_layout);
        mControllerSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mControllerIds = null;
                ModelStorage.getInstance().invalidateControllerIdsCache();
                mPageState = PageState.REFRESHING;
                updateUI();
            }
        });

        mFloatingActionButton = (FloatingActionButton)view.findViewById(R.id.controller_fragment_fab);

        mLoadingLayout = (RelativeLayout)view.findViewById(R.id.controller_fragment_loading_panel);

        mNoInternetTextView = (TextView)view.findViewById(R.id.controller_fragment_no_internet_text_view);

        mPageState = PageState.LOADING;
        mLoadingLayout.setVisibility(View.VISIBLE);
        updateUI();
        return view;
    }

    public void updateUI(){
        mControllerIds = ModelStorage.getInstance().getControllerIds(this);
        if(mControllerIds != null) {
            if(mAdapter == null){
                mAdapter = new ControllerAdapter(mControllerIds);
                mNoInternetTextView.setVisibility(View.GONE);
            }
            else {
                mAdapter.setControllerIds(mControllerIds);
                mAdapter.notifyDataSetChanged();
                if(mRingProgressDialog != null && mRingProgressDialog.isShowing()){
                    mRingProgressDialog.dismiss();
                }
            }
            mRecyclerView.setAdapter(mAdapter);
            mNoInternetTextView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            if(mControllerSwipeRefresh.isRefreshing()){
                mControllerSwipeRefresh.setRefreshing(false);
            }
            mLoadingLayout.setVisibility(View.GONE);
            mPageState = PageState.IDLE;
        }
    }

    @Override
    public void onPostRequest(SimpleResponse s) {
        if(s instanceof ControllerDataResponse) {
            if(s.getStatus() == 200)
                updateUI();
            else if(s.getStatus() == 403){
                HomecloudHolder.getInstance().invalidateSession();
                getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        }
        // This should happen if there was a connection error
        else if(s instanceof SimpleResponse) {
            if(mPageState.equals(PageState.LOADING)){
                mLoadingLayout.setVisibility(View.GONE);
                mNoInternetTextView.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            }
            else if(mPageState.equals(PageState.REFRESHING)){
                mControllerSwipeRefresh.setRefreshing(false);
                mRecyclerView.setVisibility(View.GONE);
                mLoadingLayout.setVisibility(View.GONE);
                mNoInternetTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    class ControllerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_CONTROLLER = 1, TYPE_RETRY = 2;

        List<String> mControllerIds;
        boolean mShouldRetry;

        public ControllerAdapter(List<String> controllerIds) {
            setControllerIds(controllerIds);
            mShouldRetry = false;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            if(viewType == TYPE_CONTROLLER) {
                View view = layoutInflater.inflate(R.layout.list_controller_item, parent, false);
                return new ControllerHolder(view);
            }
            else if(viewType == TYPE_RETRY) {
                View view = layoutInflater.inflate(R.layout.list_rule_error_item, parent, false);
                return new RetryHolder(view);
            }
            else
                return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (position < mControllerIds.size() && !mShouldRetry) {
                ((ControllerHolder) holder).bindController(mControllerIds.get(position));
            }
        }

        @Override
        public int getItemCount() {
            if (!mShouldRetry)
                return mControllerIds.size();
            else
                return mControllerIds.size() + 1;        }

        public void setControllerIds(List<String> controllerIds){
            mControllerIds = controllerIds;
        }

        public void setShouldRetry(boolean shouldRetry) {
            mShouldRetry = shouldRetry;
        }

        @Override
        public int getItemViewType(int position) {
            if (mShouldRetry && position == mControllerIds.size()) {
                return TYPE_RETRY;
            } else {
                return TYPE_CONTROLLER;
            }
        }
    }

    class ControllerHolder extends RecyclerView.ViewHolder {

        TextView mIdTextView;

        public ControllerHolder(View itemView) {
            super(itemView);
            mIdTextView = (TextView)itemView.findViewById(R.id.list_controller_item_id);
        }

        public void bindController(String controllerId){
            mIdTextView.setText(controllerId);
        }
    }

    class RetryHolder extends RecyclerView.ViewHolder {

        private Button mRetryButton;

        public RetryHolder(View itemView) {
            super(itemView);
            mRetryButton = (Button)itemView.findViewById(R.id.list_rule_error_item_button);
            mRetryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }
}
