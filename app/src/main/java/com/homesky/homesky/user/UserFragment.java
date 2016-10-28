package com.homesky.homesky.user;

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
import android.widget.Toast;

import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homecloud_lib.model.response.UserDataResponse;
import com.homesky.homesky.R;
import com.homesky.homesky.command.NewUserCommand;
import com.homesky.homesky.homecloud.HomecloudHolder;
import com.homesky.homesky.login.LoginActivity;
import com.homesky.homesky.request.AsyncRequest;
import com.homesky.homesky.request.ModelStorage;
import com.homesky.homesky.request.RequestCallback;
import com.homesky.homesky.utils.VerticalSpaceItemDecoration;

import java.util.List;

/**
 * Created by henrique on 9/22/16.
 */
public class UserFragment extends Fragment implements RequestCallback, NewUserDialog.NewUserDialogCallback {
    private static final String TAG = "ControllerFrag";
    private static final String DIALOG_TAG = "NewUserDialogTag";

    enum PageState{
        LOADING, REFRESHING, SENDING_NEW_USER, IDLE
    }
    private PageState mPageState;

    private List<String> mUsers;
    private UserAdapter mAdapter;
    private User mUserToAdd = null;

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mUserSwipeRefresh;
    private FloatingActionButton mFloatingActionButton;
    private ProgressDialog mRingProgressDialog;
    private RelativeLayout mLoadingLayout;
    private TextView mNoInternetTextView;


    public UserFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        mRecyclerView = (RecyclerView)view.findViewById(R.id.user_fragment_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(48));

        mUserSwipeRefresh = (SwipeRefreshLayout)view.findViewById(R.id.user_fragment_swipe_refresh_layout);
        mUserSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mUsers = null;
                ModelStorage.getInstance().invalidateControllerIdsCache();
                mPageState = PageState.REFRESHING;
                updateUI();
            }
        });

        mFloatingActionButton = (FloatingActionButton)view.findViewById(R.id.user_fragment_fab);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewUserDialog dialog = NewUserDialog.newInstance();
                dialog.show(getActivity().getSupportFragmentManager(), DIALOG_TAG);
            }
        });

        mLoadingLayout = (RelativeLayout)view.findViewById(R.id.user_fragment_loading_panel);

        mNoInternetTextView = (TextView)view.findViewById(R.id.user_fragment_no_internet_text_view);

        mPageState = PageState.LOADING;
        mLoadingLayout.setVisibility(View.VISIBLE);
        updateUI();
        return view;
    }

    public void updateUI(){
        mUsers = ModelStorage.getInstance().getUsers(this);
        if(mUsers != null) {
            if(mAdapter == null){
                mAdapter = new UserAdapter(mUsers);
                mNoInternetTextView.setVisibility(View.GONE);
            }
            else {
                mAdapter.setUsers(mUsers);
                mAdapter.notifyDataSetChanged();
                if(mRingProgressDialog != null && mRingProgressDialog.isShowing()){
                    mRingProgressDialog.dismiss();
                }
            }
            mRecyclerView.setAdapter(mAdapter);
            mNoInternetTextView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            if(mUserSwipeRefresh.isRefreshing()){
                mUserSwipeRefresh.setRefreshing(false);
            }
            mLoadingLayout.setVisibility(View.GONE);
            mPageState = PageState.IDLE;
        }
    }

    @Override
    public void onPostRequest(SimpleResponse s) {
        if(s instanceof UserDataResponse){
            if(s.getStatus() == 200){
                updateUI();
            }
            else if(s.getStatus() == 403){
                HomecloudHolder.getInstance().invalidateSession();
                getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        }
        else if(s instanceof SimpleResponse){
            if(s.getStatus() == 200 && mPageState.equals(PageState.SENDING_NEW_USER)){
                mUsers.add(mUserToAdd.getUsername());
                mAdapter.setUsers(mUsers);
                mAdapter.setShouldRetry(false);
                mAdapter.notifyDataSetChanged();
                mUserToAdd = null;
                ModelStorage.getInstance().invalidateUsersCache();
                mRingProgressDialog.dismiss();
                mPageState = PageState.IDLE;
            }
            else if(s.getStatus() == 403){
                HomecloudHolder.getInstance().invalidateSession();
                getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
            }
            // This happens if new user data is invalid
            else if(s.getStatus() == 400 && mPageState.equals(PageState.SENDING_NEW_USER)){
                Toast.makeText(
                        getActivity(),
                        getResources().getText(R.string.new_user_fragment_send_error_message) + " " + s.getErrorMessage(),
                        Toast.LENGTH_LONG).show();
                mAdapter.setShouldRetry(false);
                mAdapter.notifyDataSetChanged();
                mUserToAdd = null;
                mRingProgressDialog.dismiss();
                mPageState = PageState.IDLE;
            }
            // This should happen if there was a connection error
            else {
                if(mPageState.equals(PageState.LOADING)){
                    mLoadingLayout.setVisibility(View.GONE);
                    mNoInternetTextView.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                }
                else if(mPageState.equals(PageState.REFRESHING)){
                    mUserSwipeRefresh.setRefreshing(false);
                    mRecyclerView.setVisibility(View.GONE);
                    mLoadingLayout.setVisibility(View.GONE);
                    mNoInternetTextView.setVisibility(View.VISIBLE);
                }
                else if(mPageState.equals(PageState.SENDING_NEW_USER)){
                    mAdapter.setShouldRetry(true);
                    mAdapter.notifyDataSetChanged();
                    mRingProgressDialog.dismiss();
                }
            }
        }
    }

    @Override
    public void onNewUserResult(String username, String password) {
        mPageState = PageState.SENDING_NEW_USER;
        mUserToAdd = new User(username, password);
        mRingProgressDialog = ProgressDialog.show(
                getActivity(),
                getString(R.string.user_sending_progress_title),
                getString(R.string.user_sending_progress_message),
                true);
        new AsyncRequest(this).execute(new NewUserCommand(username, password));
    }

    class UserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_USER = 1, TYPE_RETRY = 2;

        List<String> mUsers;
        boolean mShouldRetry;

        public UserAdapter(List<String> users) {
            setUsers(users);
            mShouldRetry = false;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            if(viewType == TYPE_USER) {
                View view = layoutInflater.inflate(R.layout.list_user_item, parent, false);
                return new UserHolder(view);
            }
            else if(viewType == TYPE_RETRY) {
                View view = layoutInflater.inflate(R.layout.list_user_error_item, parent, false);
                return new RetryHolder(view);
            }
            else
                return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (position < mUsers.size()) {
                ((UserHolder) holder).bindController(mUsers.get(position));
            }
        }

        @Override
        public int getItemCount() {
            if (!mShouldRetry)
                return mUsers.size();
            else
                return mUsers.size() + 1;
        }

        public void setUsers(List<String> users){
            mUsers = users;
        }

        public void setShouldRetry(boolean shouldRetry) {
            mShouldRetry = shouldRetry;
        }

        @Override
        public int getItemViewType(int position) {
            if (mShouldRetry && position == mUsers.size()) {
                return TYPE_RETRY;
            } else {
                return TYPE_USER;
            }
        }
    }

    class UserHolder extends RecyclerView.ViewHolder {

        TextView mUserTextView;

        public UserHolder(View itemView) {
            super(itemView);
            mUserTextView = (TextView)itemView.findViewById(R.id.list_user_item_text_view);
        }

        public void bindController(String user){
            mUserTextView.setText(user);
        }
    }

    class RetryHolder extends RecyclerView.ViewHolder {

        private Button mRetryButton;

        public RetryHolder(View itemView) {
            super(itemView);
            mRetryButton = (Button)itemView.findViewById(R.id.list_controller_error_item_button);
            mRetryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPageState = PageState.SENDING_NEW_USER;
                    mRingProgressDialog = ProgressDialog.show(
                            getActivity(),
                            getString(R.string.user_sending_progress_title),
                            getString(R.string.user_sending_progress_message),
                            true);
                    new AsyncRequest(UserFragment.this).execute(new NewUserCommand(mUserToAdd.getUsername(), mUserToAdd.getPassword()));

                }
            });
        }
    }

    class User {
        public String mUsername;
        public String mPassword;

        public User(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        public String getUsername() {
            return mUsername;
        }

        public String getPassword() {
            return mPassword;
        }
    }
}
