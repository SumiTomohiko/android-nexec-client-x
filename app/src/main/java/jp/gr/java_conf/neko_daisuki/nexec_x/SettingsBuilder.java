package jp.gr.java_conf.neko_daisuki.nexec_x;

import android.os.Environment;

import java.io.File;

import jp.gr.java_conf.neko_daisuki.android.nexec.client.util.NexecClient;

public class SettingsBuilder {

    private static final boolean DEBUGGING = false;

    public static NexecClient.Settings build(String host, int port,
                                             String[] args, String rootDir,
                                             int width, int height) {
        NexecClient.Settings settings = new NexecClient.Settings();
        settings.host = host;
        settings.port = port;
        settings.args = args;

        File sdcard = Environment.getExternalStorageDirectory();
        String sdcardPath = sdcard.getAbsolutePath();
        settings.addLink(rootDir, "/");
        settings.addLink(String.format("%s/usr/home", rootDir), "/home");
        settings.addLink(sdcardPath, "/usr/home/fsyscall/sdcard");
        settings.addEnvironment("DISPLAY", ":0");
        settings.addEnvironment("HOME", "/home/fsyscall");
        settings.files = new String[] {
            sdcardPath,
            String.format("%s/**", sdcardPath)
        };
        settings.x = true;
        settings.xWidth = width;
        settings.xHeight = height;

        if (DEBUGGING) {
            enableDebugging(settings);
        }

        return settings;
    }

    private static void enableDebugging(NexecClient.Settings settings) {
        /*
         * If you want to see debug output of dbus-launch, enable DBUS_VERBOSE.
         */
        //settings.addEnvironment("DBUS_VERBOSE", "1");

        /*
         * If you want to use your own system dbus-daemon, give the path.
         */
        /*
        settings.addEnvironment("DBUS_SYSTEM_BUS_ADDRESS",
                                "unix:path=/path/to/system_bus_socket");
                                */

        /*
         * For glib debugging
         */
        /*
        settings.addEnvironment("G_DBUS_DEBUG", "all");
        settings.addEnvironment("G_MAIN_POLL_DEBUG", "1");
        */
    }
}