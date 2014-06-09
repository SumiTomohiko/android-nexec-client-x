package jp.gr.java_conf.neko_daisuki.nexec_x.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.gr.java_conf.neko_daisuki.android.nexec.client.util.NexecClient;
import jp.gr.java_conf.neko_daisuki.nexec_x.R;
import jp.gr.java_conf.neko_daisuki.nexec_x.widget.XView;

public class XFragment extends Fragment {

    public interface OnInitializingViewListener {

        public NexecClient onInitializingView(XFragment fragment);
    }

    private XView mView;
    private OnInitializingViewListener mOnInitializingViewListener;

    public static XFragment newInstance() {
        return new XFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mOnInitializingViewListener = (OnInitializingViewListener)activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = (XView)inflater.inflate(R.layout.fragment_x, null);
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mView.setNexecClient(mOnInitializingViewListener.onInitializingView(this));
    }

    public int getScale() {
        return mView.getScale();
    }

    public void zoomIn() {
        mView.zoomIn();
    }

    public void zoomOut() {
        mView.zoomOut();
    }

    public void postInvalidate() {
        mView.postInvalidate();
    }
}