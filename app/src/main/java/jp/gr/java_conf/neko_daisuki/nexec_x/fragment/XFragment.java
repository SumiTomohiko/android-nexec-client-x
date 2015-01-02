package jp.gr.java_conf.neko_daisuki.nexec_x.fragment;

import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import jp.gr.java_conf.neko_daisuki.android.nexec.client.util.NexecClient;
import jp.gr.java_conf.neko_daisuki.android.util.ContextUtil;
import jp.gr.java_conf.neko_daisuki.android.util.MenuHandler;
import jp.gr.java_conf.neko_daisuki.nexec_x.R;
import jp.gr.java_conf.neko_daisuki.nexec_x.widget.XView;

public class XFragment extends Fragment {

    public interface Listener {

        public NexecClient onInitializingView(XFragment fragment);
        public void onViewStderr(XFragment fragment);
        public void onQuit(XFragment fragment);
    }

    private class PressRightButtonMenuProc implements MenuHandler.ItemHandler {

        @Override
        public boolean handle(MenuItem item) {
            mNexecClient.xRightButtonPress();
            mPressingRightButton = true;
            invalidateX();
            return true;
        }
    }

    private class ReleaseRightButtonMenuProc implements MenuHandler.ItemHandler {

        @Override
        public boolean handle(MenuItem item) {
            mNexecClient.xRightButtonRelease();
            mPressingRightButton = false;
            invalidateX();
            return true;
        }
    }

    private class PressLeftButtonMenuProc implements MenuHandler.ItemHandler {

        @Override
        public boolean handle(MenuItem item) {
            mNexecClient.xLeftButtonPress();
            mPressingLeftButton = true;
            invalidateX();
            return true;
        }
    }

    private class ReleaseLeftButtonMenuProc implements MenuHandler.ItemHandler {

        @Override
        public boolean handle(MenuItem item) {
            mNexecClient.xLeftButtonRelease();
            mPressingLeftButton = false;
            invalidateX();
            return true;
        }
    }

    private class QuitSessionMenuProc implements MenuHandler.ItemHandler {

        @Override
        public boolean handle(MenuItem item) {
            mListener.onQuit(XFragment.this);
            return true;
        }
    }

    private abstract class ZoomMenuProc implements MenuHandler.ItemHandler {

        public abstract boolean handle(MenuItem item);

        protected void showScale() {
            Locale locale = Locale.getDefault();
            String msg = String.format(locale, "x%d", mView.getScale());
            ContextUtil.showShortToast(getActivity(), msg);
        }
    }

    private class ZoomInMenuProc extends ZoomMenuProc {

        @Override
        public boolean handle(MenuItem item) {
            mView.zoomIn();
            showScale();
            return true;
        }
    }

    private class ZoomOutMenuProc extends ZoomMenuProc {

        @Override
        public boolean handle(MenuItem item) {
            mView.zoomOut();
            showScale();
            return true;
        }
    }

    private class ViewStderrMenuProc implements MenuHandler.ItemHandler {

        @Override
        public boolean handle(MenuItem item) {
            mListener.onViewStderr(XFragment.this);
            return true;
        }
    }

    private Listener mListener;
    private boolean mPressingLeftButton = false;
    private boolean mPressingRightButton = false;
    private NexecClient mNexecClient;

    // views
    private XView mView;

    // helpers
    private MenuHandler mMenuHandler = new MenuHandler();

    public static XFragment newInstance() {
        return new XFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (Listener)activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mMenuHandler.put(R.id.action_quit_session, new QuitSessionMenuProc());
        mMenuHandler.put(R.id.action_press_left_button,
                         new PressLeftButtonMenuProc());
        mMenuHandler.put(R.id.action_release_left_button,
                         new ReleaseLeftButtonMenuProc());
        mMenuHandler.put(R.id.action_press_right_button,
                         new PressRightButtonMenuProc());
        mMenuHandler.put(R.id.action_release_right_button,
                         new ReleaseRightButtonMenuProc());
        mMenuHandler.put(R.id.action_zoom_in, new ZoomInMenuProc());
        mMenuHandler.put(R.id.action_zoom_out, new ZoomOutMenuProc());
        mMenuHandler.put(R.id.action_view_stderr, new ViewStderrMenuProc());
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
        mNexecClient = mListener.onInitializingView(this);
        mView.setNexecClient(mNexecClient);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_x, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mMenuHandler.handle(item);
        return true;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.action_press_left_button).setVisible(!mPressingLeftButton);
        menu.findItem(R.id.action_release_left_button).setVisible(mPressingLeftButton);
        menu.findItem(R.id.action_press_right_button).setVisible(!mPressingRightButton);
        menu.findItem(R.id.action_release_right_button).setVisible(mPressingRightButton);
    }

    public void postInvalidate() {
        mView.postInvalidate();
    }

    private void invalidateX() {
        getActivity().invalidateOptionsMenu();
        postInvalidate();
    }
}