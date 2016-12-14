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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
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

    private static final String[] addresses = {
            "http://ec2-52-67-3-31.sa-east-1.compute.amazonaws.com:3000", //AWS Fabio
            "http://ec2-52-67-171-212.sa-east-1.compute.amazonaws.com:3000" //AWS Ricardo
    };
    private String mSelectedAddress = addresses[0];

    private Button mLoginButton;
    private Button mSigninButton;
    private EditText mEditTextLogin;
    private EditText mEditTextPassword;
    private CheckBox mRememberMe;
    private Spinner mServerAddressSpinner;
    private EditText mServerAddressEditText;

    public LoginFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //HomecloudHolder.getInstance().setUrl(URL);
        String token = FirebaseInstanceId.getInstance().getToken();

        if (token != null)
            Log.d(TAG, token);

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

        mServerAddressEditText = (EditText) view.findViewById(R.id.login_fragment_server_address_edit_text);
        mServerAddressEditText.setVisibility(View.GONE);

        mServerAddressSpinner = (Spinner)view.findViewById(R.id.login_fragment_server_spinner);
        mServerAddressSpinner.setAdapter(new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.server_options)
        ));
        mServerAddressSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if(position < addresses.length) {
                    mServerAddressEditText.setVisibility(View.GONE);
                    mSelectedAddress = addresses[position];
                }
                else {
                    mServerAddressEditText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mLoginButton = (Button) view.findViewById(R.id.login_fragment_login_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String login = mEditTextLogin.getText().toString();
                String passwd = mEditTextPassword.getText().toString();

                if(mServerAddressEditText.getVisibility() == View.VISIBLE)
                    mSelectedAddress = mServerAddressEditText.getText().toString();

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
                } else if (mSelectedAddress.length() == 0 || !mSelectedAddress.startsWith("http://")) {
                    Toast.makeText(
                            getActivity(),
                            getResources().getText(R.string.login_fragment_address_format_error),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                HomecloudHolder.getInstance().setUrl(mSelectedAddress);

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
                if(mServerAddressEditText.getVisibility() == View.VISIBLE)
                    mSelectedAddress = mServerAddressEditText.getText().toString();

                if (mSelectedAddress.length() == 0 || !mSelectedAddress.startsWith("http://")) {
                    Toast.makeText(
                            getActivity(),
                            getResources().getText(R.string.login_fragment_address_format_error),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                HomecloudHolder.getInstance().setUrl(mSelectedAddress);

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
        } else if (s.getStatus() == 200){
            startActivity(MenuFragmentsActivity.newIntent(getActivity()));
        } else {
            Toast.makeText(
                    getActivity(),
                    getResources().getString(R.string.login_fragment_connection_failed),
                    Toast.LENGTH_LONG).show();
        }

        mEditTextPassword.setEnabled(true);
        mEditTextLogin.setEnabled(true);
    }
}
