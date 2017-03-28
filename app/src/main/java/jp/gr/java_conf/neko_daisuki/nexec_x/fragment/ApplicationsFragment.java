package jp.gr.java_conf.neko_daisuki.nexec_x.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;

import jp.gr.java_conf.neko_daisuki.android.util.MenuHandler;
import jp.gr.java_conf.neko_daisuki.nexec_x.Global;
import jp.gr.java_conf.neko_daisuki.nexec_x.R;
import jp.gr.java_conf.neko_daisuki.nexec_x.model.Application;
import jp.gr.java_conf.neko_daisuki.nexec_x.model.Genre;
import jp.gr.java_conf.neko_daisuki.nexec_x.model.Genres;

public class ApplicationsFragment extends Fragment {

    public interface Listener {

        public void onSelected(ApplicationsFragment fragment,
                               Application application);
        public void onHostPreference(ApplicationsFragment fragment);
    }

    private class HostPreferenceMenuProc implements MenuHandler.ItemHandler {

        @Override
        public boolean handle(MenuItem item) {
            mListener.onHostPreference(ApplicationsFragment.this);
            return true;
        }
    }

    private class Group {

        private String mName;
        private int mIconResourceId;
        private Application[] mApplications;

        public Group(String name, int iconResourceId,
                     Application[] applications) {
            mName = name;
            mIconResourceId = iconResourceId;
            mApplications = applications;
        }

        public String getName() {
            return mName;
        }

        public int getIconResourceId() {
            return mIconResourceId;
        }

        public Application[] getApplications() {
            return mApplications;
        }
    }

    private class CaptionComparator implements Comparator<Application> {

        @Override
        public int compare(Application lhs, Application rhs) {
            String l = lhs.getCaption().toLowerCase();
            String r = rhs.getCaption().toLowerCase();
            return l.compareTo(r);
        }
    }

    private class Adapter extends BaseExpandableListAdapter {

        private LayoutInflater mInflater;

        public Adapter(LayoutInflater inflater) {
            mInflater = inflater;
        }

        @Override
        public int getGroupCount() {
            return mGenres.length;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mGenres[groupPosition].getApplications().length;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mGenres[groupPosition];
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mGenres[groupPosition].getApplications()[childPosition];
        }

        @Override
        public long getGroupId(int groupPosition) {
            return mGenres[groupPosition].hashCode();
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            Group genre = mGenres[groupPosition];
            return genre.getApplications()[childPosition].hashCode();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            Group genre = mGenres[groupPosition];

            View view = convertView != null ? convertView : makeGenreView();
            setText(view, R.id.caption_text, capitalize(genre.getName()));

            ImageView iconView = (ImageView)view.findViewById(R.id.genre_image);
            iconView.setImageResource(genre.getIconResourceId());

            return view;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView,
                                 ViewGroup parent) {
            Group genre = mGenres[groupPosition];
            Application app = genre.getApplications()[childPosition];

            View view = convertView != null ? convertView
                                            : makeApplicationView();
            setText(view, R.id.caption_text, app.getCaption());
            setText(view, R.id.description_text, app.getDescription());

            int viewId = R.id.screenshot_image;
            ImageView screenshot = (ImageView)view.findViewById(viewId);
            int id = app.isScreenshotAvailable() ? app.getScreenshotId()
                                                 : R.drawable.ss_no_image;
            screenshot.setImageResource(id);

            return view;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        private View makeGenreView() {
            return makeView(R.layout.row_genre);
        }

        private View makeApplicationView() {
            return makeView(R.layout.row_application);
        }

        private View makeView(int id) {
            return mInflater.inflate(id, null);
        }

        private void setText(View parent, int id, String text) {
            TextView view = (TextView)parent.findViewById(id);
            view.setText(text);
        }

        private String capitalize(String s) {
            StringBuilder buffer = new StringBuilder();
            int length = s.length();
            for (int i = 0; i < length; i++) {
                String c = s.substring(i, i + 1);
                buffer.append(i == 0 ? c.toUpperCase() : c.toLowerCase());
            }
            return buffer.toString();
        }
    }

    private class OnChildClickListener implements ExpandableListView.OnChildClickListener {

        @Override
        public boolean onChildClick(ExpandableListView parent, View v,
                                    int groupPosition, int childPosition,
                                    long id) {
            Adapter adapter = (Adapter)parent.getExpandableListAdapter();
            Object application = adapter.getChild(groupPosition, childPosition);
            mListener.onSelected(ApplicationsFragment.this,
                                 (Application)application);
            return true;
        }
    }

    private Group[] mGenres;
    private Listener mListener;
    private ExpandableListView mView;

    // helpers
    private MenuHandler mMenuHandler = new MenuHandler();

    public static Fragment newInstance() {
        return new ApplicationsFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (Listener)activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mMenuHandler.put(R.id.action_host_preference,
                         new HostPreferenceMenuProc());
    }

    @Override
    public void onResume() {
        super.onResume();
        restoreListState();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveListState();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Genres genres = new Genres();
        String[] order = new String[] { "favorite", "file manager", "game",
                                        "painting" };
        mGenres = new Group[order.length];
        int len = order.length;
        for (int i = 0; i < len; i++) {
            String name = order[i];
            Genre genre = genres.get(name);
            Collection<Application> c = genre.getApplications();
            Application[] a = c.toArray(new Application[0]);
            Arrays.sort(a, new CaptionComparator());
            mGenres[i] = new Group(name, genre.getIconResourceId(), a);
        }

        int layout = R.layout.fragment_applications;
        mView = (ExpandableListView)inflater.inflate(layout, null);
        mView.setAdapter(new Adapter(inflater));
        mView.setOnChildClickListener(new OnChildClickListener());

        return mView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_applications, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mMenuHandler.handle(item);
        return true;
    }

    private void expandList(String name) {
        int nGenres = mGenres.length;
        for (int i = 0; i < nGenres; i++) {
            Group g = mGenres[i];
            if (name.equals(g.getName())) {
                mView.expandGroup(i);
                return;
            }
        }
    }

    private String getUiStateDirectory() {
        return Global.getUiStateDirectory("applications_fragment");
    }

    private File getUiStateFile() {
        return new File(getUiStateDirectory(), "expandeds");
    }

    private void saveListState() {
        String dir = getUiStateDirectory();
        new File(dir).mkdirs();

        Collection<String> expandeds = new HashSet<String>();
        ExpandableListAdapter adapter = mView.getExpandableListAdapter();
        int nGroups = adapter.getGroupCount();
        for (int i = 0; i < nGroups; i++) {
            if (mView.isGroupExpanded(i)) {
                expandeds.add(mGenres[i].getName());
            }
        }

        File file = getUiStateFile();
        try {
            OutputStream out = new FileOutputStream(file);
            try {
                OutputStream bout = new BufferedOutputStream(out);
                try {
                    PrintWriter pw = new PrintWriter(bout);
                    try {
                        for (String name: expandeds) {
                            pw.println(name);
                        }
                    }
                    finally {
                        pw.close();
                    }
                }
                finally {
                    bout.close();
                }
            }
            finally {
                out.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void restoreListState() {
        File file = getUiStateFile();
        try {
            InputStream in = new FileInputStream(file);
            try {
                Reader r = new InputStreamReader(in);
                try {
                    BufferedReader br = new BufferedReader(r);
                    try {
                        String name;
                        while ((name = br.readLine()) != null) {
                            expandList(name);
                        }
                    }
                    finally {
                        br.close();
                    }
                }
                finally {
                    r.close();
                }
            }
            finally {
                in.close();
            }
        }
        catch (FileNotFoundException unused) {
            expandList("game");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}