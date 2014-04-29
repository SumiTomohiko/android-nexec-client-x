package jp.gr.java_conf.neko_daisuki.nexec_x.fragment;

import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import jp.gr.java_conf.neko_daisuki.nexec_x.R;
import jp.gr.java_conf.neko_daisuki.nexec_x.model.Application;

public class ApplicationFragment extends DialogFragment {

    public interface OnSelectedListener {

        public void onSelected(ApplicationFragment fragment,
                               Application application);
    }

    private class Adapter extends BaseAdapter {

        private class OnClickListener implements View.OnClickListener {

            private int mPosition;

            public OnClickListener(int position) {
                mPosition = position;
            }

            @Override
            public void onClick(View v) {
                mSelectedPosition = mPosition;
                notifyDataSetChanged();
            }
        }

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
            OnClickListener listener = new OnClickListener(position);
            view.setOnClickListener(listener);

            int id = R.id.checkbox;
            CompoundButton checkable = (CompoundButton)view.findViewById(id);
            checkable.setChecked(position == mSelectedPosition);
            checkable.setOnClickListener(listener);

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

    private class OnClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            Application application = mApplications[mSelectedPosition];
            mListener.onSelected(ApplicationFragment.this, application);
        }
    }

    // documents
    private Application[] mApplications;
    private OnSelectedListener mListener;

    // views
    private int mSelectedPosition = 0;

    public static DialogFragment newInstance() {
        return new ApplicationFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (OnSelectedListener)activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mApplications = Application.list().toArray(new Application[0]);
        Arrays.sort(mApplications, new Application.CaptionComparator());

        Context context = getActivity();
        String name = Context.LAYOUT_INFLATER_SERVICE;
        Object service = context.getSystemService(name);
        LayoutInflater inflater = (LayoutInflater)service;

        ListView view = new ListView(context);
        view.setAdapter(new Adapter(inflater));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select an application");
        builder.setPositiveButton(R.string.positive, new OnClickListener());
        builder.setNegativeButton(R.string.negative, null);
        builder.setView(view);
        return builder.create();
    }
}