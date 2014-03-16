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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import jp.gr.java_conf.neko_daisuki.android.nexec.client.share.SessionId;
import jp.gr.java_conf.neko_daisuki.android.nexec.client.util.NexecClient;
import jp.gr.java_conf.neko_daisuki.android.nexec.client.util.NexecHost;
import jp.gr.java_conf.neko_daisuki.android.nexec.client.util.NexecUtil;
import jp.gr.java_conf.neko_daisuki.nexec_x.R;
import jp.gr.java_conf.neko_daisuki.nexec_x.widget.XView;

public class MainActivity extends Activity {

    private interface AfterResumeProc {

        public void run();
    }

    private class ConnectAfterResumeProc implements AfterResumeProc {

        @Override
        public void run() {
            mNexecClient.connect(mSessionId);
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
            public void run() {
                mSessionId = NexecUtil.getSessionId(mData);
                mNexecClient.execute(mSessionId);
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

    private interface MenuProc {

        public void run(MenuItem item);
    }

    private class HostPreferenceMenuProc implements MenuProc {

        @Override
        public void run(MenuItem item) {
            int requestCode = REQUEST_HOST_PREFERENCE;
            NexecUtil.startHostPreferenceActivity(MainActivity.this,
                                                  requestCode, mHost.getHost(),
                                                  mHost.getPort());
        }
    }

    private class NewSessionMenuProc implements MenuProc {

        @Override
        public void run(MenuItem item) {
            NexecClient.Settings settings = new NexecClient.Settings();
            settings.host = mHost.getHost();
            settings.port = mHost.getPort();
            settings.args = new String[] { "xeyes" };
            settings.addEnvironment("DISPLAY", ":0");
            settings.files = new String[0];
            settings.xWidth = mView.getWidth();
            settings.xHeight = mView.getHeight();

            mNexecClient.request(settings, REQUEST_CONFIRM);
        }
    }

    private class OnXInvalidateListener implements NexecClient.OnXInvalidateListener {

        @Override
        public void onInvalidate(int left, int top, int right, int bottom) {
            mView.postInvalidate();
        }
    }

    private static final String PATH_SESSION_ID = "session_id";
    private static final int REQUEST_CONFIRM = 42;
    private static final int REQUEST_HOST_PREFERENCE = 43;
    private static final String DEFAULT_HOST = "neko-daisuki.ddo.jp";

    // documents
    private NexecHost mHost;
    private SessionId mSessionId;

    // views
    private XView mView;

    // helpers
    private NexecClient mNexecClient;
    private SparseArray<MenuProc> mMenuProcs = new SparseArray<MenuProc>();
    private ResultProcs mResultProcs = new ResultProcs();
    private AfterResumeProc mAfterResumeProc = new ConnectAfterResumeProc();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mMenuProcs.get(item.getItemId()).run(item);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNexecClient.disconnect();
        writeSessionId(mSessionId);
        new File(getApplicationDirectoryPath()).mkdirs();
        writeHost(mHost);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHost = readHost(getHostJsonPath());
        mSessionId = readSessionId();
        mAfterResumeProc.run();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mView = (XView)findViewById(R.id.x_view);

        mNexecClient = new NexecClient(this);
        mNexecClient.setOnXInvalidateListener(new OnXInvalidateListener());
        mMenuProcs.put(R.id.action_new_session, new NewSessionMenuProc());
        mMenuProcs.put(R.id.action_host_preference,
                       new HostPreferenceMenuProc());
        mResultProcs.put(REQUEST_CONFIRM, RESULT_OK, new ConfirmResultProc());
        mResultProcs.put(REQUEST_HOST_PREFERENCE, RESULT_OK,
                         new HostPreferenceResultProc());
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
            handleException("read session id", e);
            return SessionId.NULL;
        }
    }

    private void writeSessionId(SessionId sessionId) {
        OutputStream out;
        try {
            out = openFileOutput(PATH_SESSION_ID, 0);
        }
        catch (FileNotFoundException e) {
            handleException("open session id", e);
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
            handleException("write session id", e);
        }
    }

    private void handleException(String msg, Throwable e) {
        e.printStackTrace();

        String s = String.format("%s: %s", msg, e.getMessage());
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
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
            handleException("Cannot write host information", e);
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
            handleException("Cannot read host information", e);
            return new NexecHost(DEFAULT_HOST);
        }
    }
}