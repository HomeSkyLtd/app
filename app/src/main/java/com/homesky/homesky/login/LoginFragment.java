package com.homesky.homesky.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.SharedPreferencesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.homesky.homecloud_lib.Homecloud;
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
    //private static final String URL = "http://192.168.0.197:3000";             // Henrique no energia
    //private static final String URL = "http://10.144.38.149:3000";    // Henrique no Eduroam
    private static final String URL = "http://192.168.1.109:3000";      // Casa do Henrique
    //private static final String URL = "http://192.168.1.102:3000";      // Casa do Henrique
    //private static final String URL = "http://ec2-52-67-169-17.sa-east-1.compute.amazonaws.com:3000"; // AWS do Henrique
    //private static final String URL = "http://ec2-52-67-3-31.sa-east-1.compute.amazonaws.com:3000"; // AWS do Fabão
    //private static final String URL = "http://192.168.1.116:3000";      // Scopus

    private Button mLoginButton;
    private Button mSigninButton;
    private EditText mEditTextLogin;
    private EditText mEditTextPassword;
    private CheckBox mRememberMe;

    public LoginFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HomecloudHolder.getInstance().setUrl(URL);
        String token = FirebaseInstanceId.getInstance().getToken();
        HomecloudHolder.getInstance().setToken(token);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        if (HomecloudHolder.getInstance().isLogged()) {
            startActivity(MenuFragmentsActivity.newIntent(getActivity()));
        }

        view.findViewById(R.id.login_fragment_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomecloudHolder.getInstance().setUsername("admin1");
                HomecloudHolder.getInstance().setPassword("mypass");
                Toast.makeText(getActivity(), "Logging in...", Toast.LENGTH_SHORT).show();
                new AsyncRequest(null, LoginFragment.this).execute(new LoginCommand());
            }
        });

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

                Toast.makeText(getActivity(), "Logging in...", Toast.LENGTH_SHORT).show();

                HomecloudHolder.getInstance().setUsername(login);
                HomecloudHolder.getInstance().setPassword(passwd);
                mLoginButton.setEnabled(false);
                mSigninButton.setEnabled(false);

                new AsyncRequest(null, LoginFragment.this).execute(new LoginCommand());
            }
        });

        mSigninButton = (Button) view.findViewById(R.id.login_fragment_signin_button);
        mSigninButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().startActivity(LoginActivity.newIntent(getActivity(), LoginActivity.LoginAction.SIGNIN));
            }
        });

        mRememberMe = (CheckBox) view.findViewById(R.id.login_fragment_check_box);

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
                    "No internet connection",
                    Toast.LENGTH_LONG).show();
            mEditTextPassword.setEnabled(true);
            mEditTextLogin.setEnabled(true);
        }
    }
}
