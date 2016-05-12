package jp.gr.java_conf.neko_daisuki.nexec_x;

import jp.gr.java_conf.neko_daisuki.android.nexec.client.util.NexecClient;

public class SettingsBuilder {

    public static NexecClient.Settings build(String host, int port,
                                             String[] args, String homeDir,
                                             String tmpDir, int width,
                                             int height) {
        NexecClient.Settings settings = new NexecClient.Settings();
        settings.host = host;
        settings.port = port;
        settings.args = args;
        settings.addLink(homeDir, "/home/fsyscall");
        settings.addLink(tmpDir, "/tmp");
        settings.addEnvironment("DISPLAY",":0");

        // If you want to see debug output of dbus-launch, enable DBUS_VERBOSE.
        settings.addEnvironment("DBUS_VERBOSE","1");

        // If you want to use your own system dbus-daemon, give the path.
        /*
        settings.addEnvironment("DBUS_SYSTEM_BUS_ADDRESS",
                                "unix:path=/path/to/system_bus_socket");
                                */

        // For glib debugging
        settings.addEnvironment("G_DBUS_DEBUG","all");
        settings.addEnvironment("G_MAIN_POLL_DEBUG","1");

        settings.files = new String[] {
            String.format("%s/**", homeDir),
            tmpDir,
            String.format("%s/**", tmpDir)
        };
        settings.x = true;
        settings.xWidth = width;
        settings.xHeight = height;

        return settings;
    }
}