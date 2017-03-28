package jp.gr.java_conf.neko_daisuki.nexec_x;

import android.os.Environment;

import java.io.File;

public class Global {

    public static String getPhysicalRootDirectory() {
        return String.format("%s/%s", getApplicationDirectory(), "rootdir");
    }

    public static String getUiStateDirectory() {
        return String.format("%s/ui_state", getApplicationDirectory());
    }

    public static String getUiStateDirectory(String name) {
        return String.format("%s/%s", getUiStateDirectory(), name);
    }

    private static String getApplicationDirectory() {
        File file = Environment.getExternalStorageDirectory();
        return String.format("%s/nexec", file.getAbsolutePath());
    }
}