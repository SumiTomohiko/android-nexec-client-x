package jp.gr.java_conf.neko_daisuki.nexec_x.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import jp.gr.java_conf.neko_daisuki.nexec_x.R;

public class AboutActivity extends Activity {

    public static void show(Context context) {
        context.startActivity(new Intent(context, AboutActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        showVersion();
        showLicense();
    }

    private void showLicense() {
        StringBuilder buffer = new StringBuilder();

        try {
            InputStream in = getAssets().open("LICENSE");
            try {
                InputStreamReader reader = new InputStreamReader(in);
                try {
                    BufferedReader br = new BufferedReader(reader);
                    try {
                        String line;
                        while ((line = br.readLine()) != null) {
                            buffer.append(line);
                            buffer.append("\n");
                        }
                    }
                    finally {
                        br.close();
                    }
                }
                finally {
                    reader.close();
                }
            }
            finally {
                in.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        TextView view = (TextView)findViewById(R.id.license);
        view.setText(buffer.toString());
    }

    private void showVersion() {
        PackageManager pm = getPackageManager();
        String name = getPackageName();
        int flags = PackageManager.GET_INSTRUMENTATION;

        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(name, flags);
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return;
        }

        TextView view = (TextView)findViewById(R.id.version);
        view.setText(pi.versionName);
    }
}