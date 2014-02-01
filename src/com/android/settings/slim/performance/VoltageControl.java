package com.android.settings.slim.performance;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.slim.performance.PreferencesProvider;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class VoltageControl extends Fragment implements Paths {

    public static final String TAG = "Performance-VoltageControl";

    public static final int DIALOG_VOLTAGE = 0;
    private List<Voltage> mVoltages;
    private List<String> mVoltageValues = new ArrayList<String>();
    private List<String> mFreqs;
    private ListAdapter mAdapter;
    private Voltage mVoltage;
    private Context mContext;

    private static final int[] STEPS = new int[] {600, 625, 650, 675, 700,
            725, 750, 775, 800, 825, 850, 875, 900, 925, 950, 975, 1000, 1025,
            1050, 1075, 1100, 1125, 1150, 1175, 1200, 1225, 1250, 1275, 1300,
            1325, 1350, 1375, 1400, 1425, 1450, 1475, 1500, 1525, 1550, 1575,
            1600};
    private static final int[] STEPS_VALUES = new int[] {600000, 625000, 650000, 675000, 700000,
            725000, 750000, 775000, 800000, 825000, 850000, 875000, 900000, 925000, 950000, 975000,
            1000000, 1025000, 1050000, 1075000, 1100000, 1125000, 1150000, 1175000, 1200000,
            1225000, 1250000, 1275000, 1300000, 1325000, 1350000, 1375000, 1400000, 1425000,
            1450000, 1475000, 1500000, 1525000, 1550000, 1575000, 1600000};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        mVoltages = getVolts(mContext);
        mFreqs = getFreqs();
        for (Voltage voltage : mVoltages) {
            mVoltageValues.add(voltage.getCurrentMv());
        }
        mAdapter = new ListAdapter(mContext);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.voltage_control, root, false);

        final ListView listView = (ListView) view.findViewById(R.id.ListView);

        if (mVoltages.isEmpty()) {
            view.findViewById(R.id.emptyList).setVisibility(View.VISIBLE);
            view.findViewById(R.id.BottomBar).setVisibility(View.GONE);
        }

        mAdapter.setListItems(mVoltages);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mVoltage = mVoltages.get(position);
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.voltage_control_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.volt_increase:
                increaseByStep(25);
                break;
            case R.id.volt_decrease:
                increaseByStep(-25);
                break;
            case R.id.reset:
                resetVolt();
                break;
            case R.id.apply:
                setVoltage(mVoltages);
                final List<Voltage> volts = getVolts(mContext);
                mVoltages.clear();
                mVoltages.addAll(volts);
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.volt_set_on_boot:
                item.setChecked(item.isChecked());
                PreferencesProvider.putBoolean(mContext, PreferencesProvider.VOLT_SET_ON_BOOT,
                        item.isChecked());
                break;
        }
        return true;
    }

    public static List<String> getFreqs() {
        List<String> freqs = new ArrayList<String>();
        String[] availableFrequencies = Utils.readOneLine(FREQ_LIST_FILE).split(" ");
        Arrays.sort(availableFrequencies, new Comparator<String>() {
            @Override
            public int compare(String object1, String object2) {
                return Integer.valueOf(object1).compareTo(Integer.valueOf(object2));
            }
        });
        for (String freq : availableFrequencies) {
            freqs.add(freq);
        }
        return freqs;
    }

    public static List<Voltage> getVolts(Context context) {
        Utils.voltageFileExists();
        final List<Voltage> volts = new ArrayList<Voltage>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Utils.getVoltagePath()), 256);
            String line = "";
            if (Utils.getVoltagePath().equals(VDD_PATH)) {
                while ((line = reader.readLine()) != null) {
                    line = line.replaceAll("\\s", "");
                    if (!line.equals("")) {
                        final String[] values = line.split(":");
                        final String freq = values[0];
                        final String currentMv = values[1];
                        final Voltage voltage = new Voltage();
                        voltage.setFreq(freq);
                        voltage.setCurrentMv(currentMv);
                        volts.add(voltage);
                    }
                }
            } else if (Utils.getVoltagePath().equals(VDD_TABLE_PATH)) {
                while ((line = reader.readLine()) != null) {
                    line = line.replaceAll("\\s", "");
                    if (!line.equals("")) {
                        final String[] values = line.split(":");
                        final String freq = values[0];
                        final String currentMv = values[1];
                        final Voltage voltage = new Voltage();
                        voltage.setFreq(freq);
                        voltage.setCurrentMv(currentMv);
                        volts.add(voltage);
                    }
                }
            } else {
                while ((line = reader.readLine()) != null) {
                    final String[] values = line.split("\\s+");
                    if (values != null) {
                        if (values.length >= 2) {
                            final String freq = values[0].replace("mhz:", "");
                            final String currentMv = values[1];
                            final String savedMv =
                                    PreferencesProvider.getString(context, freq, currentMv);
                            final Voltage voltage = new Voltage();
                            voltage.setFreq(freq);
                            voltage.setCurrentMv(currentMv);
                            voltage.setSavedMv(savedMv);
                            volts.add(voltage);
                        }
                    }
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, Utils.getVoltagePath() + " does not exist!");
        } catch (IOException e) {
            Log.d(TAG, "Error reading " + Utils.getVoltagePath());
        }
        return volts;
    }

    private void resetVolt() {
        for (Voltage volt : mVoltages) {
            PreferencesProvider.put(mContext).remove(volt.getFreq()).commit();
        }
        final List<Voltage> volts = getVolts(mContext);
        mVoltages.clear();
        mVoltages.addAll(volts);
        mAdapter.notifyDataSetChanged();
    }

    private void increaseByStep(int pas) {
        for (Voltage volt : mVoltages) {
            String value = Integer.toString(Integer.parseInt(volt.getSavedMv()) + pas);
            volt.setSavedMv(value);
        }
        mAdapter.setListItems(mVoltages);
        mAdapter.notifyDataSetChanged();
    }

    public static class Voltage {
        private String mFreq;
        private String mCurrentMv;
        private String mSavedMv;

        public void setFreq(String freq) {
            mFreq = freq;
        }

        public String getFreq() {
            return mFreq;
        }

        public void setCurrentMv(String currentMv) {
            mCurrentMv = currentMv;
        }

        public String getCurrentMv() {
            return mCurrentMv;
        }

        public void setSavedMv(String savedMv) {
            mSavedMv = savedMv;
        }

        public String getSavedMv() {
            if (mSavedMv != null) {
                return mSavedMv;
            } else {
                return mCurrentMv;
            }
        }
    }

    public class ListAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<Voltage> mResults;
        private ViewHolder mHolder;

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

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_volt, null);
                mHolder = new ViewHolder();
                mHolder.mFreq = (TextView) convertView.findViewById(R.id.Freq);
                mHolder.mCurrentMV = (Spinner) convertView.findViewById(R.id.mVCurrent);
                ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                        mContext, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                for (int volt : STEPS) {
                    adapter.add(Integer.toString(volt));
                }
                mHolder.mCurrentMV.setAdapter(adapter);
                mHolder.mCurrentMV.post(new Runnable() {
                    @Override
                    public void run() {
                        mHolder.mCurrentMV.setOnItemSelectedListener(
                                new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView,
                                                       View view, int i, long l) {
                                String value = "";
                                if (Utils.getVoltagePath().equals(VDD_PATH) ||
                                        Utils.getVoltagePath().equals(VDD_TABLE_PATH)) {
                                    value = Integer.toString(STEPS_VALUES[i]);
                                } else {
                                    value = adapterView.getItemAtPosition(i).toString();
                                }
                                mVoltage = mVoltages.get(position);
                                mVoltage.setSavedMv(value);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });
                    }
                });
                convertView.setTag(mHolder);
            } else {
                mHolder = (ViewHolder) convertView.getTag();
            }

            final Voltage voltage = mVoltages.get(position);
            mHolder.setFreq(voltage.getFreq());
            mHolder.setCurrentMV(position);
            return convertView;
        }

        public void setListItems(List<Voltage> voltages) {
            mResults = voltages;
        }

        public class ViewHolder {
            private TextView mFreq;
            private Spinner mCurrentMV;

            public void setFreq(final String freq) {
                mFreq.setText(freq + " Hz");
            }

            public void setCurrentMV(int currentMv) {
                mCurrentMV.setSelection(currentMv);
            }
        }
    }

    private static int getNearestStepIndex(final int value) {
        int index = 0;
        for (int STEP : STEPS) {
            if (value > STEP) index++;
            else break;
        }
        return index;
    }

    private static void setVoltage(List<Voltage> voltages) {
        StringBuilder sb = new StringBuilder();
        if (Utils.getVoltagePath().equals(VDD_PATH) ||
                Utils.getVoltagePath().equals(VDD_TABLE_PATH)) {
            for (Voltage volt : voltages) {
                sb.append(volt.getFreq() + ": " + volt.getSavedMv() + "\n");
            }
        } else {
            for (Voltage volt : voltages) {
                sb.append(volt.getFreq() + "mhz: " + volt.getSavedMv() + " mV" + "\n");
            }
        }
        Utils.writeValue(Utils.getVoltagePath(), sb.toString());
    }

    public static void restore(Context context) {
        if (PreferencesProvider.getBoolean(context, VOLTAGE_SOB, false)) {
            List<Voltage> voltages = getVolts(context);
            setVoltage(voltages);
        }
    }

    public static boolean isVoltageControlSupported() {
        return Utils.voltageFileExists();
    }
}
