package jp.gr.java_conf.neko_daisuki.nexec_x.model;

import java.util.Collection;
import java.util.HashSet;

public class Genre {

    private String mName;
    private int mIconResourceId;
    private Collection<Application> mApplications = new HashSet<Application>();

    public Genre(String name, int iconResourceId) {
        mName = name;
        mIconResourceId = iconResourceId;
    }

    public Collection<Application> getApplications() {
        return mApplications;
    }

    public String getName() {
        return mName;
    }

    public int getIconResourceId() {
        return mIconResourceId;
    }

    public void add(Application application) {
        mApplications.add(application);
    }
}