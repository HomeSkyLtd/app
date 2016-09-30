package com.homesky.homesky.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.homesky.homecloud_lib.model.response.SimpleResponse;
import com.homesky.homesky.R;
import com.homesky.homesky.activities.MenuFragmentsActivity;
import com.homesky.homesky.command.LoginCommand;
import com.homesky.homesky.homecloud.HomecloudHolder;
import com.homesky.homesky.request.AsyncRequest;
import com.homesky.homesky.request.RequestCallback;
import com.homesky.homesky.user.UserActivity;

public class LoginFragment extends Fragment implements RequestCallback {

    private static final String TAG = "LoginFragment";
    private static final String URL = "http://192.168.1.111:3000";

    private Button mLoginButton;
    private Button mSigninButton;
    private EditText mEditTextLogin;
    private EditText mEditTextPassword;

    public LoginFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        HomecloudHolder.setUrl(URL);

        mEditTextLogin = (EditText) view.findViewById(R.id.login_fragment_edit_login);
        mEditTextPassword = (EditText) view.findViewById(R.id.login_fragment_edit_passwd);

        mLoginButton = (Button) view.findViewById(R.id.login_fragment_login_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String login = mEditTextLogin.getText().toString();
                String passwd = mEditTextPassword.getText().toString();

                if (login.length() == 0) {
                    Toast.makeText(
                            getActivity(),
                            getResources().getText(R.string.login_fragment_empty_login),
                            Toast.LENGTH_LONG).show();
                    return;
                } else if (passwd.length() == 0) {
                    Toast.makeText(
                            getActivity(),
                            getResources().getText(R.string.login_fragment_empty_passwd),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                HomecloudHolder.getInstance().setUsername(login);
                HomecloudHolder.getInstance().setPassword(passwd);
                mLoginButton.setEnabled(false);
                mSigninButton.setEnabled(false);

                Toast.makeText(getActivity(), "Logging in...", Toast.LENGTH_SHORT).show();

                new AsyncRequest(LoginFragment.this).execute(new LoginCommand());
            }
        });

        mSigninButton = (Button) view.findViewById(R.id.login_fragment_signin_button);
        mSigninButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(UserActivity.newIntent(getActivity()));
            }
        });

        return view;
    }

    @Override
    public void onPostRequest(SimpleResponse s) {

        mLoginButton.setEnabled(true);
        mSigninButton.setEnabled(true);

        if (s == null) {
            Toast.makeText(
                    getActivity(),
                    getResources().getText(R.string.login_fragment_server_offline),
                    Toast.LENGTH_LONG).show();
            mEditTextPassword.setEnabled(true);
            mEditTextLogin.setEnabled(true);
        } else if (s.getStatus() == 200){
            startActivity(MenuFragmentsActivity.newIntent(getActivity()));
        } else {
            Toast.makeText(
                    getActivity(),
                    getResources().getText(R.string.login_fragment_error)  + " " + s.getErrorMessage(),
                    Toast.LENGTH_LONG).show();
            mEditTextPassword.setEnabled(true);
            mEditTextLogin.setEnabled(true);
        }
    }
}
