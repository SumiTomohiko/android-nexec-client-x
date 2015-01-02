package jp.gr.java_conf.neko_daisuki.nexec_x.fragment;

import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;

public class StderrFragment extends DialogFragment {

    public interface Listener {

        public byte[] onShowStderr(StderrFragment fragment);
    }

    private EditText mView;
    private Listener mListener;

    public static DialogFragment newInstance() {
        return new StderrFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (Listener)activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        mView = new EditText(context);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(mView);
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();

        byte[] buf = mListener.onShowStderr(this);
        String s;
        try {
            s = new String(buf, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }
        mView.setText(s);
    }
}