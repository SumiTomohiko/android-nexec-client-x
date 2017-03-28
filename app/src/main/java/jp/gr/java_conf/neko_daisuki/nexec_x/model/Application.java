package jp.gr.java_conf.neko_daisuki.nexec_x.model;

public class Application {

    private static final int NO_IMAGE = -1;

    private String mCaption;
    private String[] mArguments;
    private String mDescription;
    private int mScreenshotId;

    public Application(String caption, String command, String description) {
        this(caption, new String[] { command }, description);
    }

    public Application(String caption, String[] args, String description) {
        mCaption = caption;
        mArguments = args;
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