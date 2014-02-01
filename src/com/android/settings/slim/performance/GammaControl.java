package com.android.settings.slim.performance;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

import com.android.settings.R;
import com.android.settings.slim.performance.PreferencesProvider;
import com.android.settings.slim.performance.Paths;
import com.android.settings.slim.performance.Utils;

import java.io.File;
import java.util.List;

public class GammaControl extends Fragment implements Paths {

    private static final String TAG = "SlimPerformance-GammaControl";

    private static final int OFFSET_VALUE = 100;

    private Spinner mRedSpinner;
    private Spinner mGreenSpinner;
    private Spinner mBlueSpinner;

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gamma_settings, root, false);

        mRedSpinner = (Spinner) view.findViewById(R.id.red_gamma);
        mGreenSpinner = (Spinner) view.findViewById(R.id.green_gamma);
        mBlueSpinner = (Spinner) view.findViewById(R.id.blue_gamma);

        int max = 256;
        int min = 1;
        if (!Utils.fileExists(GAMMA_FILE)) {
            max = 101;
            min = -100;
        }

        List<String> gamma = Utils.getGammaValues();
        int red_gamma = Integer.parseInt(gamma.get(0)) - 1;
        int green_gamma = Integer.parseInt(gamma.get(1)) -1 ;
        int blue_gamma = Integer.parseInt(gamma.get(2)) - 1;

        ArrayAdapter<CharSequence> gammaAdapter = new ArrayAdapter<CharSequence>(
                mContext, android.R.layout.simple_spinner_item);
        gammaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (int i = min; i < max; i++) {
            gammaAdapter.add(Integer.toString(i));
        }

        mRedSpinner.setAdapter(gammaAdapter);
        mRedSpinner.setSelection(red_gamma);
        mRedSpinner.post(new Runnable() {
            @Override
            public void run() {
                mRedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if (Utils.fileExists(GAMMA_FILE)) {
                            Utils.writeGamma("red", i + 1);
                            saveGamma("red", i + 1);
                            applyGamma();
                        } else {
                            int val = i - OFFSET_VALUE;
                            Utils.writeValue(GAMMA_OLED_RED_FILE, Integer.toString(val));
                            PreferencesProvider.putInt(mContext, PREF_RED_OLED, i);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
            }
        });
        mGreenSpinner.setAdapter(gammaAdapter);
        mGreenSpinner.setSelection(green_gamma);
        mGreenSpinner.post(new Runnable() {
            @Override
            public void run() {
                mGreenSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if (Utils.fileExists(GAMMA_FILE)) {
                            Utils.writeGamma("green", i + 1);
                            saveGamma("green", i + 1);
                            applyGamma();
                        } else {
                            int val = i - OFFSET_VALUE;
                            Utils.writeValue(GAMMA_OLED_GREEN_FILE, Integer.toString(val));
                            PreferencesProvider.putInt(mContext, PREF_GREEN_OLED, i);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
            }
        });
        mBlueSpinner.setAdapter(gammaAdapter);
        mBlueSpinner.setSelection(blue_gamma);
        mBlueSpinner.post(new Runnable() {
            @Override
            public void run() {
                mBlueSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if (Utils.fileExists(GAMMA_FILE)) {
                            Utils.writeGamma("blue", i + 1);
                            saveGamma("blue", i + 1);
                            applyGamma();
                        } else {
                            int val = i - OFFSET_VALUE;
                            Utils.writeValue(GAMMA_OLED_BLUE_FILE, Integer.toString(val));
                            PreferencesProvider.putInt(mContext, PREF_BLUE_OLED, i);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
            }
        });

        Switch setOnBoot = (Switch) view.findViewById(R.id.cpu_set_on_boot);
        setOnBoot.setChecked(PreferencesProvider.getBoolean(mContext, PREF_GAMMA_SOB, false));
        setOnBoot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PreferencesProvider.putBoolean(mContext, PREF_GAMMA_SOB, b);
            }
        });

        return view;
    }

    private void applyGamma() {
        if (Utils.fileExists(GAMMA_CTRL_FILE)) {
            Utils.writeValue(GAMMA_CTRL_FILE, "1");
        }
    }

    private void saveGamma(String color, int value) {
        String[] cur_gamma = Utils.readOneLine(GAMMA_FILE).split(" ");
        String red = cur_gamma[0];
        String green = cur_gamma[1];
        String blue = cur_gamma[2];
        if (color.equals("red")) {
            red = Integer.toString(value);
        } else if (color.equals("green")) {
            green = Integer.toString(value);
        } else if (color.equals("blue")) {
            blue = Integer.toString(value);
        }
        String gamma = red + " " + green + " " + blue;
        PreferencesProvider.putString(mContext, PREF_GAMMA, gamma);
    }

    public static void restore(Context context) {
        if (PreferencesProvider.getBoolean(context, PREF_GAMMA_SOB, false)) {
            if (Utils.fileExists(GAMMA_FILE)) {
                String gamma = PreferencesProvider.getString(context, PREF_GAMMA, null);

                if (gamma != null) {
                    String[] saved = gamma.split(" ");
                    String red = saved[0];
                    String green = saved[1];
                    String blue = saved[2];
                    Utils.writeGamma("red", Integer.parseInt(red));
                    Utils.writeGamma("green", Integer.parseInt(green));
                    Utils.writeGamma("blue", Integer.parseInt(blue));
                    if (Utils.fileExists(GAMMA_CTRL_FILE)) {
                        Utils.writeValue(GAMMA_CTRL_FILE, "1");
                    }
                }
            } else {
                int r = PreferencesProvider.getInt(context, PREF_RED_OLED, 99999) - OFFSET_VALUE;
                int g = PreferencesProvider.getInt(context, PREF_GREEN_OLED, 99999) - OFFSET_VALUE;
                int b = PreferencesProvider.getInt(context, PREF_BLUE_OLED, 99999) - OFFSET_VALUE;
                if (r != 99999) {
                    Utils.writeValue(GAMMA_OLED_RED_FILE, Integer.toString(r));
                }
                if (g != 99999) {
                    Utils.writeValue(GAMMA_OLED_GREEN_FILE, Integer.toString(g));
                }
                if (b != 99999) {
                    Utils.writeValue(GAMMA_OLED_BLUE_FILE, Integer.toString(b));
                }
            }
        }
    }
}
