package com.homesky.homesky.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import com.homesky.homesky.command.NewAdminCommand;
import com.homesky.homesky.homecloud.HomecloudHolder;
import com.homesky.homesky.request.AsyncRequest;
import com.homesky.homesky.request.RequestCallback;

/**
 * Created by henrique on 10/28/16.
 */

public class SigninFragment extends Fragment implements View.OnClickListener {

    private EditText mEditTextLogin, mEditTextPasswd1, mEditTextPasswd2;
    private Button mButtonOk, mButtonCancel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signin_fragment, container, false);

        mEditTextLogin = (EditText) view.findViewById(R.id.signin_fragment_edit_text_login);
        mEditTextPasswd1 = (EditText) view.findViewById(R.id.signin_fragment_edit_text_passwd1);
        mEditTextPasswd2 = (EditText) view.findViewById(R.id.signin_fragment_edit_text_passwd2);

        mButtonOk = (Button) view.findViewById(R.id.siginin_fragment_button_ok);
        mButtonOk.setOnClickListener(this);

        mButtonCancel = (Button) view.findViewById(R.id.siginin_fragment_button_cancel);
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(LoginActivity.newIntent(getActivity(), LoginActivity.LoginAction.LOGIN));
            }
        });

        return view;
    }

    @Override
    public void onClick(View v) {
        mButtonOk.setEnabled(false);
        mButtonCancel.setEnabled(false);

        String login = mEditTextLogin.getText().toString();
        String passwd1 = mEditTextPasswd1.getText().toString();
        String passwd2= mEditTextPasswd2.getText().toString();

        String errorMsg = null;

        if (login.isEmpty()) {
            errorMsg = "Type your login";
        } else if (passwd1.isEmpty()) {
            errorMsg = "Type your password";
        } else if (passwd2.isEmpty()) {
            errorMsg = "Confirm your password";
        } else if (!passwd1.equals(passwd2)) {
            errorMsg = "Password and confirmation are different";
        }

        if (errorMsg == null) {
            Toast.makeText(getActivity(), "Signing in...", Toast.LENGTH_SHORT).show();
            new AsyncRequest(new SigninCallback(login, passwd1))
                    .execute(new NewAdminCommand(login, passwd1));
        } else {
            Snackbar.make(
                    getActivity().findViewById(R.id.fragment_container),
                    errorMsg,
                    Snackbar.LENGTH_SHORT
            ).show();
        }
    }

    private class SigninCallback implements RequestCallback {

        private String mLogin, mPasswd;

        SigninCallback(String login, String passwd) {
            mLogin = login;
            mPasswd = passwd;
        }

        @Override
        public void onPostRequest(SimpleResponse s) {
            if (s != null && s.getStatus() == 400) {
                mButtonOk.setEnabled(true);
                mButtonCancel.setEnabled(true);

                Snackbar.make(
                        getActivity().findViewById(R.id.fragment_container),
                        s.getErrorMessage(),
                        Snackbar.LENGTH_SHORT
                ).show();
            } else if (s == null || s.getStatus() != 200) {
                mButtonOk.setEnabled(true);
                mButtonCancel.setEnabled(true);

                Snackbar.make(
                        getActivity().findViewById(R.id.fragment_container),
                        "No internet connection",
                        Snackbar.LENGTH_SHORT
                ).show();
            } else {
                Toast.makeText(getActivity(), "Welcome!", Toast.LENGTH_SHORT).show();

                HomecloudHolder.getInstance().setUsername(mLogin);
                HomecloudHolder.getInstance().setPassword(mPasswd);

                startActivity(MenuFragmentsActivity.newIntent(getActivity()));
            }
        }
    }
}
