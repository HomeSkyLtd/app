package com.homesky.homesky.fragments.clause;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.homesky.homecloud_lib.model.Proposition;
import com.homesky.homesky.R;

/**
 * Created by fabio on 12/10/2016.
 */

public class PropositionDialog extends DialogFragment {

    PropositionDialogCallback mCallback;

    public interface PropositionDialogCallback{
        public void onPropositionResult(Proposition p, int orStatementIndex);
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (PropositionDialogCallback)getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_proposition_dialog, container, false);
        return v;
    }
}
