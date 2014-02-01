/*
 * Copyright (C) 2014 The SlimRoms Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.slim.performance;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

//import com.slim.performance.preference.PreferencesProvider;
import com.android.settings.slim.performance.Paths;
import com.android.settings.slim.performance.Utils;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import com.android.settings.R;

public class Processor extends Fragment implements SeekBar.OnSeekBarChangeListener, Paths {
    private SeekBar mMaxSlider;
    private TextView mMaxSpeed;
    private SeekBar mMinSlider;
    private TextView mMinSpeed;

    private Spinner mGovernor;
    private Spinner mIOScheduler;

    private String[] mAvailableFrequencies;
    private String mMaxFreq;
    private String mMinFreq;

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.processor_settings, root, false);

        mAvailableFrequencies = Utils.readOneLine(FREQ_LIST_FILE).split(" ");
        int frequenciesNum = mAvailableFrequencies.length - 1;
        Arrays.sort(mAvailableFrequencies, new Comparator<String>() {
            @Override
            public int compare(String object1, String object2) {
                return Integer.valueOf(object1).compareTo(Integer.valueOf(object2));
            }
        });

        if (new File(DYN_MAX_FREQ).exists()) {
            mMaxFreq = Utils.readOneLine(DYN_MAX_FREQ);
        } else {
            mMaxFreq = Utils.readOneLine(MAX_FREQ_FILE);
        }
        if (new File(DYN_MIN_FREQ).exists()) {
            mMinFreq = Utils.readOneLine(DYN_MIN_FREQ);
        } else {
            mMinFreq = Utils.readOneLine(MIN_FREQ_FILE);
        }

        mMaxSlider = (SeekBar) view.findViewById(R.id.max_slider);
        mMaxSlider.setMax(frequenciesNum);
        mMaxSlider.setProgress(Arrays.asList(mAvailableFrequencies).indexOf(mMaxFreq));
        mMaxSlider.setOnSeekBarChangeListener(this);
        mMaxSpeed = (TextView) view.findViewById(R.id.max_speed_text);
        mMaxSpeed.setText(Utils.toMHz(mMaxFreq));

        mMinSlider = (SeekBar) view.findViewById(R.id.min_slider);
        mMinSlider.setMax(frequenciesNum);
        mMinSlider.setProgress(Arrays.asList(mAvailableFrequencies).indexOf(mMinFreq));
        mMinSlider.setOnSeekBarChangeListener(this);
        mMinSpeed = (TextView) view.findViewById(R.id.min_speed_text);
        mMinSpeed.setText(Utils.toMHz(mMinFreq));

        String currentGov = Utils.readOneLine(GOV_FILE);
        String[] availableGovernors = Utils.readOneLine(GOV_LIST_FILE).split(" ");

        mGovernor = (Spinner) view.findViewById(R.id.governor);
        ArrayAdapter<CharSequence> governorAdapter = new ArrayAdapter<CharSequence>(
                mContext, android.R.layout.simple_spinner_item);
        governorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (String governor : availableGovernors) {
            governorAdapter.add(governor);
        }
        mGovernor.setAdapter(governorAdapter);
        mGovernor.setSelection(Arrays.asList(availableGovernors).indexOf(currentGov));
        mGovernor.post(new Runnable() {
            public void run() {
                mGovernor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> adapterView, View view, int pos, long l) {
                        String selected = adapterView.getItemAtPosition(pos).toString();
                        for (int i = 0;  i < Utils.getNumOfCpus(); i++) {
                            Utils.writeValue(GOV_FILE.replace("cpu0", "cpu" + i), selected);
                        }
                        PreferencesProvider.putString(mContext, PREF_GOVERNOR, selected);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
            }
        });

        String currentIOSched = Utils.getCurrentIOScheduler();
        String[] availableIOScheds = Utils.getAvailableIOSchedulers();

        mIOScheduler = (Spinner) view.findViewById(R.id.io_scheduler);
        ArrayAdapter<CharSequence> ioAdapter = new ArrayAdapter<CharSequence>(
                mContext, android.R.layout.simple_spinner_item);
        ioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (String sched : availableIOScheds) {
            ioAdapter.add(sched);
        }
        mIOScheduler.setAdapter(ioAdapter);
        mIOScheduler.setSelection(Arrays.asList(availableIOScheds).indexOf(currentIOSched));
        mIOScheduler.post(new Runnable() {
            @Override
            public void run() {
                mIOScheduler.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView,
                                               View view, int position, long l) {
                        String selected = adapterView.getItemAtPosition(position).toString();
                        for (String path : IO_SCHEDULER_PATH) {
                            if (new File(path).exists()) {
                                Utils.writeValue(path, selected);
                            }
                        }
                        PreferencesProvider.putString(mContext, PREF_IOSCHED, selected);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
            }
        });

        Switch setOnBoot = (Switch) view.findViewById(R.id.cpu_set_on_boot);
        setOnBoot.setChecked(PreferencesProvider.getBoolean(mContext, PREF_CPU_SOB, false));
        setOnBoot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PreferencesProvider.putBoolean(mContext, PREF_CPU_SOB, b);
            }
        });

        return view;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        for (int i = 0; i < Utils.getNumOfCpus(); i++) {
            Utils.writeValue(MAX_FREQ_FILE.replace("cpu0", "cpu" + i), mMaxFreq);
            Utils.writeValue(MIN_FREQ_FILE.replace("cpu0", "cpu" + i), mMinFreq);
        }
        //if (mIsDynFreq) {
        //    Utils.writeOneLine(DYN_MAX_FREQ_PATH, mMaxFreqSetting);
        //    Utils.writeOneLine(DYN_MIN_FREQ_PATH, mMinFreqSetting);
        //}
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            if (seekBar.getId() == R.id.max_slider) {
                setMaxSpeed(seekBar, progress);
            } else if (seekBar.getId() == R.id.min_slider) {
                setMinSpeed(seekBar, progress);
            }
        }
    }

    public void setMaxSpeed(SeekBar seekBar, int progress) {
        String current = "";
        current = mAvailableFrequencies[progress];
        int minSliderProgress = mMinSlider.getProgress();
        if (progress <= minSliderProgress) {
            mMinSlider.setProgress(progress);
            mMinSpeed.setText(Utils.toMHz(current));
            mMinFreq = current;
        }
        mMaxSpeed.setText(Utils.toMHz(current));
        mMaxFreq = current;
        PreferencesProvider.putString(mContext, PREF_MAX_FREQ, current);
    }

    public void setMinSpeed(SeekBar seekBar, int progress) {
        String current = "";
        current = mAvailableFrequencies[progress];
        int maxSliderProgress = mMaxSlider.getProgress();
        if (progress >= maxSliderProgress) {
            mMaxSlider.setProgress(progress);
            mMaxSpeed.setText(Utils.toMHz(current));
            mMaxFreq = current;
        }
        mMinSpeed.setText(Utils.toMHz(current));
        mMinFreq = current;
        PreferencesProvider.putString(mContext, PREF_MIN_FREQ, current);
    }

    public static void restore(Context context) {
        Log.d("CPUSettings", "restoring cpu settings");
        if (PreferencesProvider.getBoolean(context, PREF_CPU_SOB, false)) {
            String maxFreq = PreferencesProvider.getString(context,  PREF_MAX_FREQ, null);
            String minFreq = PreferencesProvider.getString(context, PREF_MIN_FREQ, null);
            String governor = PreferencesProvider.getString(context, PREF_GOVERNOR, null);
            String iosched = PreferencesProvider.getString(context, PREF_IOSCHED, null);

            if (maxFreq != null) {
                Utils.writeValue(MAX_FREQ_FILE, maxFreq);
            }

            if (minFreq != null) {
                Utils.writeValue(MIN_FREQ_FILE, minFreq);
            }

            if (governor != null) {
                Utils.writeValue(GOV_FILE, governor);
            }

            if (iosched != null) {
                for (String path : IO_SCHEDULER_PATH) {
                    if (new File(path).exists()) {
                        Utils.writeValue(path, iosched);
                    }
                }
            }
        }
    }
}
