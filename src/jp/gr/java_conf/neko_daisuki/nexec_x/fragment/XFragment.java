package jp.gr.java_conf.neko_daisuki.nexec_x.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.gr.java_conf.neko_daisuki.nexec_x.R;

public class XFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_x, container);
    }
}