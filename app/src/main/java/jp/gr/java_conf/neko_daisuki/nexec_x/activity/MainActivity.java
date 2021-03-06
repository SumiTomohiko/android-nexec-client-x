package jp.gr.java_conf.neko_daisuki.nexec_x.activity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import jp.gr.java_conf.neko_daisuki.android.nexec.client.share.SessionId;
import jp.gr.java_conf.neko_daisuki.android.nexec.client.util.NexecClient;
import jp.gr.java_conf.neko_daisuki.android.nexec.client.util.NexecHost;
import jp.gr.java_conf.neko_daisuki.android.nexec.client.util.NexecUtil;
import jp.gr.java_conf.neko_daisuki.android.util.ActivityUtil;
import jp.gr.java_conf.neko_daisuki.android.util.ContextUtil;
import jp.gr.java_conf.neko_daisuki.android.util.MenuHandler;
import jp.gr.java_conf.neko_daisuki.nexec_x.Global;
import jp.gr.java_conf.neko_daisuki.nexec_x.R;
import jp.gr.java_conf.neko_daisuki.nexec_x.SettingsBuilder;
import jp.gr.java_conf.neko_daisuki.nexec_x.fragment.ApplicationsFragment;
import jp.gr.java_conf.neko_daisuki.nexec_x.fragment.StderrFragment;
import jp.gr.java_conf.neko_daisuki.nexec_x.fragment.XFragment;
import jp.gr.java_conf.neko_daisuki.nexec_x.model.Application;

public class MainActivity extends FragmentActivity implements ApplicationsFragment.Listener, StderrFragment.Listener, XFragment.Listener {

    private interface AfterResumeProc {

        public void run(SessionId savedSessionId);
    }

    private class ConnectAfterResumeProc implements AfterResumeProc {

        @Override
        public void run(SessionId savedSessionId) {
            mNexecClient.connect(savedSessionId);

            boolean isRunning = !savedSessionId.isNull();
            Fragment f = isRunning ? XFragment.newInstance()
                                   : ApplicationsFragment.newInstance();
            showFragment(f);
        }
    }

    private interface ResultProc {

        public static class Nop implements ResultProc {

            @Override
            public void run(int requestCode, int resultCode, Intent data) {
            }
        }

        public static final ResultProc NOP = new Nop();

        public void run(int requestCode, int resultCode, Intent data);
    }

    private class HostPreferenceResultProc implements ResultProc {

        @Override
        public void run(int requestCode, int resultCode, Intent data) {
            writeHost(NexecUtil.getHost(data));
        }
    }

    private class ConfirmResultProc implements ResultProc {

        private class ExecuteAfterResumeProc implements AfterResumeProc {

            private Intent mData;

            public ExecuteAfterResumeProc(Intent data) {
                mData = data;
            }

            @Override
            public void run(SessionId savedSessionId) {
                mProgressDialog.show();
                mStderr.clear();
                mNexecClient.setOnXInvalidateListener(mFirstOnXInvalidateListener);
                mNexecClient.execute(NexecUtil.getSessionId(mData));
                invalidateOptionsMenu();
                showFragment(XFragment.newInstance());
            }
        }

        @Override
        public void run(int requestCode, int resultCode, Intent data) {
            mAfterResumeProc = new ExecuteAfterResumeProc(data);
        }
    }

    private static class ResultProcs {

        private SparseArray<ResultProc> mProcs = new SparseArray<ResultProc>();

        public void put(int requestCode, int resultCode, ResultProc proc) {
            mProcs.put(computeKey(requestCode, resultCode), proc);
        }

        public void run(int requestCode, int resultCode, Intent data) {
            ResultProc entry = mProcs.get(computeKey(requestCode, resultCode));
            ResultProc proc = entry != null ? entry : ResultProc.NOP;
            proc.run(requestCode, resultCode, data);
        }

        private int computeKey(int requestCode, int resultCode) {
            return (requestCode << 16) + resultCode;
        }
    }

    private class AboutMenuProc implements MenuHandler.ItemHandler {

        @Override
        public boolean handle(MenuItem item) {
            AboutActivity.show(MainActivity.this);
            return true;
        }
    }

    private abstract class BaseOnXInvalidateListener implements NexecClient.OnXInvalidateListener {

        @Override
        public void onInvalidate(int left, int top, int right, int bottom) {
            preInvalidate();
            getXFragment().postInvalidate();
        }

        protected abstract void preInvalidate();
    }

    private class FirstOnXInvalidateListener extends BaseOnXInvalidateListener {

        @Override
        protected void preInvalidate() {
            mProgressDialog.dismiss();
            mNexecClient.setOnXInvalidateListener(mOnXInvalidateListener);
        }
    }

    private class OnXInvalidateListener extends BaseOnXInvalidateListener {

        @Override
        protected void preInvalidate() {
        }
    }

    private class OnErrorListener implements NexecClient.OnErrorListener {

        @Override
        public void onError(NexecClient nexecClient, Throwable e) {
            ActivityUtil.showException(MainActivity.this, "error", e);
        }

        @Override
        public void onServiceError(NexecClient nexecClient, String message) {
            ActivityUtil.showToast(MainActivity.this, message);
            mProgressDialog.dismiss();
        }
    }

    private class OnExitListener implements NexecClient.OnExitListener {

        @Override
        public void onExit(NexecClient nexecClient, int exitCode) {
            Locale locale = Locale.getDefault();
            String msg = String.format(locale, "exit: %d", exitCode);
            ActivityUtil.showToast(MainActivity.this, msg);
            mProgressDialog.dismiss();
        }
    }

    private class OnStderrListener implements NexecClient.OnStderrListener {

        @Override
        public void onWrite(NexecClient nexecClient, byte[] buf) {
            for (int i = 0; i < buf.length; i++) {
                mStderr.add(Byte.valueOf(buf[i]));
            }
        }
    }

    private class ProgressCancelListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            quit();
        }
    }

    private static final String PATH_SESSION_ID = "session_id";
    private static final int REQUEST_CONFIRM = 42;
    private static final int REQUEST_HOST_PREFERENCE = 43;
    private static final String DEFAULT_HOST = "neko-daisuki.ddo.jp";
    private static final Pattern RE_VARIABLE = Pattern.compile("\\$\\{[^}]*\\}");

    // documents
    private NexecHost mHost;
    private List<Byte> mStderr = new ArrayList<Byte>();

    // views
    private View mView;
    private Dialog mProgressDialog;

    // helpers
    private NexecClient mNexecClient;
    private MenuHandler mMenuHandler = new MenuHandler();
    private ResultProcs mResultProcs = new ResultProcs();
    private AfterResumeProc mAfterResumeProc = new ConnectAfterResumeProc();
    private NexecClient.OnXInvalidateListener mFirstOnXInvalidateListener;
    private NexecClient.OnXInvalidateListener mOnXInvalidateListener;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mMenuHandler.handle(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public void onSelected(ApplicationsFragment fragment,
                           Application application) {
        String physicalRootDir = Global.getPhysicalRootDirectory();
        String homeDir = String.format("%s/usr/home/fsyscall", physicalRootDir);
        new File(homeDir).mkdirs();
        String sdcardDir = String.format("%s/sdcard", homeDir);
        new File(sdcardDir).mkdirs();
        String tmpDir = String.format("%s/tmp", physicalRootDir);
        new File(tmpDir).mkdirs();

        int width = mView.getWidth();
        int height = mView.getHeight();
        try {
            String[] args = evaluateArguments(application.getArguments(), width,
                                              height);
            String host = mHost.getHost();
            int port = mHost.getPort();
            NexecClient.Settings settings = SettingsBuilder.build(host, port,
                                                                  args,
                                                                  physicalRootDir,
                                                                  width,
                                                                  height);
            mNexecClient.request(settings, REQUEST_CONFIRM);
        }
        catch (ParseException e) {
            ContextUtil.showException(this, "invalid argument found", e);
        }
    }

    @Override
    public void onHostPreference(ApplicationsFragment fragment) {
        NexecUtil.startHostPreferenceActivity(this, REQUEST_HOST_PREFERENCE,
                                              mHost.getHost(), mHost.getPort());
    }

    @Override
    public NexecClient onInitializingView(XFragment fragment) {
        return mNexecClient;
    }

    @Override
    public void onQuit(XFragment fragment) {
        quit();
    }

    @Override
    public void onViewStderr(XFragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        StderrFragment.newInstance().show(manager, null);
    }

    @Override
    public byte[] onShowStderr(StderrFragment fragment) {
        int size = mStderr.size();
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++) {
            data[i] = mStderr.get(i);
        }
        return data;
    }

    @Override
    protected void onPause() {
        super.onPause();
        writeSessionId(mNexecClient.getSessionId());
        mNexecClient.disconnect();
        new File(getApplicationDirectoryPath()).mkdirs();
        writeHost(mHost);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHost = readHost(getHostJsonPath());
        mAfterResumeProc.run(readSessionId());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNexecClient = new NexecClient(this);
        mNexecClient.setOnExitListener(new OnExitListener());
        mNexecClient.setOnErrorListener(new OnErrorListener());
        mNexecClient.setOnStderrListener(new OnStderrListener());
        mMenuHandler.put(R.id.action_about_this_app, new AboutMenuProc());
        mResultProcs.put(REQUEST_CONFIRM, RESULT_OK, new ConfirmResultProc());
        mResultProcs.put(REQUEST_HOST_PREFERENCE, RESULT_OK,
                         new HostPreferenceResultProc());
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel",
                         new ProgressCancelListener());
        dialog.setCancelable(false);
        dialog.setTitle("Please wait");
        dialog.setMessage("Initializing X...");
        mProgressDialog = dialog;
        mFirstOnXInvalidateListener = new FirstOnXInvalidateListener();
        mOnXInvalidateListener = new OnXInvalidateListener();

        mView = findViewById(R.id.fragment_container);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        mResultProcs.run(requestCode, resultCode, data);
    }

    private SessionId readSessionId() {
        InputStream in;
        try {
            in = openFileInput(PATH_SESSION_ID);
        }
        catch (FileNotFoundException e) {
            return SessionId.NULL;
        }
        try {
            try {
                Reader reader = new InputStreamReader(in);
                return new SessionId(new BufferedReader(reader).readLine());
            }
            finally {
                in.close();
            }
        }
        catch (IOException e) {
            ContextUtil.showException(this, "Cannot read the session id", e);
            return SessionId.NULL;
        }
    }

    private void writeSessionId(SessionId sessionId) {
        OutputStream out;
        try {
            out = openFileOutput(PATH_SESSION_ID, 0);
        }
        catch (FileNotFoundException e) {
            ContextUtil.showException(this, "Cannot open the session id", e);
            return;
        }
        try {
            try {
                OutputStreamWriter osw = new OutputStreamWriter(out);
                try {
                    PrintWriter writer = new PrintWriter(osw);
                    try {
                        writer.write(sessionId.toString());
                    }
                    finally {
                        writer.close();
                    }
                }
                finally {
                    osw.close();
                }
            }
            finally {
                out.close();
            }
        }
        catch (IOException e) {
            ContextUtil.showException(this, "Cannot write session id", e);
        }
    }

    private String getApplicationDirectoryPath() {
        String fmt = "%s/.android-nexec-client-x";
        return String.format(fmt, Environment.getExternalStorageDirectory());
    }

    private String getHostJsonPath() {
        return String.format("%s/host.json", getApplicationDirectoryPath());
    }

    private void writeHost(NexecHost host) {
        writeHost(getHostJsonPath(), host);
    }

    private void writeHost(String path, NexecHost host) {
        try {
            NexecUtil.writeHostToJson(path, host);
        }
        catch (IOException e) {
            String msg = "Cannot write the host information";
            ContextUtil.showException(this, msg, e);
        }
    }

    private NexecHost readHost(String path) {
        try {
            return NexecUtil.readHostFromJson(path);
        }
        catch (FileNotFoundException e) {
            return new NexecHost(DEFAULT_HOST);
        }
        catch (IOException e) {
            String msg = "Cannot read the host information";
            ContextUtil.showException(this, msg, e);
            return new NexecHost(DEFAULT_HOST);
        }
    }

    private void showFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.commit();
    }

    private XFragment getXFragment() {
        return (XFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    private void quit() {
        mNexecClient.quit();
        mProgressDialog.dismiss();
        showFragment(ApplicationsFragment.newInstance());
        invalidateOptionsMenu();
    }

    private String[] evaluateArguments(String[] args, int screenWidth,
                                       int screenHeight) throws ParseException {
        int len = args.length;
        String[] a = new String[len];
        for (int i = 0; i < len; i++) {
            StringBuilder buffer = new StringBuilder();
            String s = args[i];
            Matcher m = RE_VARIABLE.matcher(s);
            int position = 0;
            while (m.find(position)) {
                buffer.append(s.substring(position, m.start()));
                String v = m.group();
                if (v.equals("${screen_width}")) {
                    buffer.append(Integer.toString(screenWidth));
                }
                else if (v.equals("${screen_height}")) {
                    buffer.append(Integer.toString(screenHeight));
                }
                else {
                    String fmt = "unknown variable %s";
                    throw new ParseException(String.format(fmt, v), position);
                }
                position = m.end();
            }
            buffer.append(s.substring(position));
            a[i] = buffer.toString();
        }
        return a;
    }
}