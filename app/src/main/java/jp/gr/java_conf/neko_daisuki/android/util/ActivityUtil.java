package jp.gr.java_conf.neko_daisuki.android.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.Toast;

public class ActivityUtil {

    private static class Toaster implements Runnable {

        private Context mContext;
        private String mMessage;
        private int mLength;

        public Toaster(Context context, String message, int length) {
            mContext = context;
            mMessage = message;
            mLength = length;
        }

        @Override
        public void run() {
            Toast.makeText(mContext, mMessage, mLength).show();
        }
    }

    public static void showException(Activity activity, String msg,
                                     Throwable e) {
        e.printStackTrace();
        showToast(activity, String.format("%s: %s", msg, e.getMessage()));
    }

    public static void showToast(Activity activity, String message) {
        String name;
        try {
            name = ContextUtil.getApplicationName(activity);
        }
        catch (NameNotFoundException e) {
            showException(activity, "Cannot find the package", e);
            return;
        }
        String s = String.format("%s: %s", name, message);
        activity.runOnUiThread(new Toaster(activity, s, Toast.LENGTH_LONG));
    }
}