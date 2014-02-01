package com.android.settings.slim.performance;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.android.settings.R;


import java.security.Key;
import java.util.List;

public class IOControl extends Fragment implements Paths {
    private static final String TAG = "Performance-IOControl";

    private static final int DIALOG_EDIT = 0;
    private List<String> mValues;
    private ListAdapter mAdapter;
    private String mCurrent;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        mValues = Utils.getIOControl();
        mAdapter = new ListAdapter(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.io_settings, root, false);
        ListView listView = (ListView) view.findViewById(R.id.ListView);
        if (mValues.isEmpty()) {
            view.findViewById(R.id.emptyList).setVisibility(View.VISIBLE);
            view.findViewById(R.id.BottomBar).setVisibility(View.GONE);
        }
        mAdapter.setListItems(mValues);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mCurrent = mValues.get(i);
                showDialog(DIALOG_EDIT);
            }
        });

        Switch setOnBoot = (Switch) view.findViewById(R.id.io_control_sob);
        setOnBoot.setChecked(PreferencesProvider.getBoolean(mContext, PREF_IO_CONTROL_SOB, false));
        setOnBoot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PreferencesProvider.putBoolean(mContext, PREF_IO_CONTROL_SOB, b);
            }
        });
        return view;
    }

    protected void showDialog(int id) {
        AlertDialog dialog = null;

        switch (id) {
            case DIALOG_EDIT:
                LayoutInflater factory = LayoutInflater.from(mContext);
                View editDialog = factory.inflate(R.layout.edit_dialog, null);

                final EditText editText = (EditText) editDialog.findViewById(R.id.editText);

                String saved = Utils.readOneLine(Utils.getIOControlPath() + "/" + mCurrent);
                editText.setText(saved);

                dialog = new AlertDialog.Builder(mContext)
                        .setTitle(mCurrent)
                        .setView(editDialog)
                        .setPositiveButton(getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.cancel();
                                    final String val = editText.getText().toString();
                                    Utils.writeValue(Utils.getIOControlPath() + "/" + mCurrent, val);
                                    PreferencesProvider.putIO(mContext, mCurrent, val);
                                }
                            })
                        .setNegativeButton(getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.cancel();
                                }
                            })
                        .create();
                break;
            default:
                break;
        }
        if (dialog != null) {
            dialog.show();
        }
    }

    public class ListAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<String> mResults;

        public ListAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mResults.size();
        }

        @Override
        public Object getItem(int position) {
            return mResults.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item, null);
                holder = new ViewHolder();
                holder.mTitle = (TextView) convertView.findViewById(R.id.title);
                holder.mValue = (TextView) convertView.findViewById(R.id.current);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final String title = mResults.get(position);
            holder.setTitle(title);
            String saved = Utils.readOneLine(Utils.getIOControlPath() + "/" + mResults.get(position));
            Log.d(TAG, title + ": " + saved);
            holder.setValue(saved);
            return convertView;
        }

        public void setListItems(List<String> results) {
            mResults = results;
        }

        public class ViewHolder {
            private TextView mTitle;
            private TextView mValue;

            public void setTitle(String title) {
                mTitle.setText(title);
            }

            public void setValue(String value) {
                mValue.setText(value);
            }
        }
    }

    public static void restore(Context context) {
        if (PreferencesProvider.getBoolean(context, PREF_IO_CONTROL_SOB, false)) {
            List<String> files = Utils.getIOControl();
            if (files != null) {
                for (String file : files) {
                    Log.d(TAG, file);
                    String value = PreferencesProvider.getIO(context, file, null);
                    if (value != null) {
                        Utils.writeValue(Utils.getIOControlPath() + "/" + file, value);
                    }
                }
            }
        }
    }
}
