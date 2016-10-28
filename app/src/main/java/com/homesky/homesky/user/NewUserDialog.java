package com.homesky.homesky.user;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.homesky.homesky.R;

public class NewUserDialog extends DialogFragment {

    private NewUserDialogCallback mCallback;

    private EditText mUsernameEditText, mPasswordEditText, mConfirmPasswordEditText;
    private Button mOkButton, mCancelButton;

    public interface NewUserDialogCallback {
        void onNewUserResult(String username, String password);
    }

    public static NewUserDialog newInstance() {
        NewUserDialog dialog = new NewUserDialog();
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (NewUserDialogCallback)getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_new_user_dialog, container, false);

        mUsernameEditText = (EditText)v.findViewById(R.id.fragment_new_user_dialog_username_edit_text);
        mPasswordEditText = (EditText)v.findViewById(R.id.fragment_new_user_dialog_password_edit_text);
        mConfirmPasswordEditText = (EditText)v.findViewById(R.id.fragment_new_user_dialog_password_confirm_edit_text);

        mOkButton = (Button)v.findViewById(R.id.fragment_new_user_dialog_ok_button);
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateFields()){
                    String username = mUsernameEditText.getText().toString();
                    String password = mPasswordEditText.getText().toString();
                    mCallback.onNewUserResult(username, password);
                    NewUserDialog.this.dismiss();
                }
            }
        });

        mCancelButton = (Button)v.findViewById(R.id.fragment_new_user_dialog_cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewUserDialog.this.dismiss();
            }
        });

        return v;
    }

    private boolean validateFields(){
        if(mUsernameEditText.getText().length() == 0 || mPasswordEditText.getText().length() == 0 ||
                mConfirmPasswordEditText.getText().length() == 0) {
            Toast.makeText(
                    getActivity(),
                    getResources().getText(R.string.new_user_fragment_empty_field_message),
                    Toast.LENGTH_LONG).show();
            return false;
        }
        else if(!mPasswordEditText.getText().toString().equals(mConfirmPasswordEditText.getText().toString())){
            Toast.makeText(
                    getActivity(),
                    getResources().getText(R.string.new_user_fragment_password_mismatch_message),
                    Toast.LENGTH_LONG).show();
            return false;
        }
        else
            return true;

    }
}
