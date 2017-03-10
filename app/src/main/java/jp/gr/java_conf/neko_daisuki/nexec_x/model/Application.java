package jp.gr.java_conf.neko_daisuki.nexec_x.model;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;

public class Application {

    public static class CaptionComparator implements Comparator<Application> {

        @Override
        public int compare(Application lhs, Application rhs) {
            return lhs.getCaption().compareTo(rhs.getCaption());
        }
    }

    private static final int NO_IMAGE = -1;

    private String mCaption;
    private String[] mArguments;
    private String mDescription;
    private int mScreenshotId;

    public static Collection<Application> list() {
        Collection<Application> apps = new HashSet<Application>();

        apps.add(new Application("Inkscape", "inkscape", "SVG editor"));
        apps.add(new Application("gedit", "gedit", "Text editor"));
        apps.add(new Application("xmine", "xmine", "Mine sweeper"));

        return apps;
    }

    public Application(String caption, String command, String description) {
        mCaption = caption;
        mArguments = new String[] { command };
        mDescription = description;
        mScreenshotId = NO_IMAGE;
    }

    public String getCaption() {
        return mCaption;
    }

    public String[] getArguments() {
        return mArguments;
    }

    public String getDescription() {
        return mDescription;
    }

    public boolean isScreenshotAvailable() {
        return mScreenshotId != NO_IMAGE;
    }

    public int getScreenshotId() {
        return mScreenshotId;
    }
}