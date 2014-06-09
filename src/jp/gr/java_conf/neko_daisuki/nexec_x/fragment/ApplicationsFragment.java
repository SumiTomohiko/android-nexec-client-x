package jp.gr.java_conf.neko_daisuki.nexec_x.fragment;

import java.util.Arrays;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import jp.gr.java_conf.neko_daisuki.nexec_x.R;
import jp.gr.java_conf.neko_daisuki.nexec_x.model.Application;

public class ApplicationsFragment extends Fragment {

    public interface OnSelectedListener {

        public void onSelected(ApplicationsFragment fragment,
                               Application application);
    }

    private class Adapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public Adapter(LayoutInflater inflater) {
            mInflater = inflater;
        }

        @Override
        public int getCount() {
            return mApplications.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView != null ? convertView : makeView(position);
            Application application = mApplications[position];
            setText(view, R.id.caption_text, application.getCaption());
            setText(view, R.id.description_text, application.getDescription());
            return view;
        }

        private void setText(View parent, int id, String text) {
            TextView view = (TextView)parent.findViewById(id);
            view.setText(text);
        }

        private View makeView(int position) {
            return mInflater.inflate(R.layout.row_application, null);
        }
    }

    private class OnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            Application application = mApplications[position];
            mListener.onSelected(ApplicationsFragment.this, application);
        }
    }

    // documents
    private Application[] mApplications;
    private OnSelectedListener mListener;

    public static Fragment newInstance() {
        return new ApplicationsFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (OnSelectedListener)activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplications = Application.list().toArray(new Application[0]);
        Arrays.sort(mApplications, new Application.CaptionComparator());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int layout = R.layout.fragment_applications;
        ListView view = (ListView)inflater.inflate(layout, null);
        view.setAdapter(new Adapter(inflater));
        view.setOnItemClickListener(new OnItemClickListener());
        return view;
    }
}