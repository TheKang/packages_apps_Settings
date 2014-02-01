package com.android.settings.slim.performance;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utils implements Paths {

    private static String mVoltagePath;
    private static String mGovernorControlPath;
    private static String mIOControlPath;

    public static String readOneLine(String file) {
        BufferedReader reader;
        String line = null;
        try {
            reader = new BufferedReader(new FileReader(file), 512);
            try {
                line = reader.readLine();
            } finally {
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return line;
    }

    public static boolean writeValue(String filename, String value) {
        boolean useSu = false;
        if (new File(filename).exists()) {
            try {
                FileOutputStream fos = new FileOutputStream(new File(filename));
                fos.write(value.getBytes());
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                useSu = true;
            }
            if (useSu) {
                try {
                    Process process = Runtime.getRuntime().exec("su");
                    DataOutputStream dos = new DataOutputStream(process.getOutputStream());
                    dos.writeBytes("echo " + value + " > " + filename + "\n");
                    dos.flush();
                    dos.writeBytes("echo" + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return readOneLine(filename).equals(value);
        }
        return false;
    }

    public static boolean fileExists(String file) {
        return new File(file).exists();
    }

    public static int getNumOfCpus() {
        int numOfCpu = 1;
        String numOfCpus = readOneLine(CPU_NUM_FILE);
        String[] cpuCount = numOfCpus.split("-");
        if (cpuCount.length > 1) {
            try {
                int cpuStart = Integer.parseInt(cpuCount[0]);
                int cpuEnd = Integer.parseInt(cpuCount[1]);

                numOfCpu = cpuEnd - cpuStart + 1;

                if (numOfCpu < 0)
                    numOfCpu = 1;
            } catch (NumberFormatException ex) {
                numOfCpu = 1;
            }
        }
        return numOfCpu;
    }

    public static String toMHz(String mhzString) {
        return String.valueOf(Integer.parseInt(mhzString) / 1000) + " MHz";
    }

    public static String getCurrentIOScheduler() {
        String scheduler = null;
        String[] schedulers = null;
        String line = readOneLine(IO_SCHEDULER_PATH[0]);
        if (line != null) {
            schedulers = line.split(" ");
        }
        if (schedulers != null) {
            for (String s : schedulers) {
                if (s.charAt(0) == '[') {
                    scheduler = s.substring(1, s.length() - 1);
                    break;
                }
            }
        }
        return scheduler;
    }

    public static String[] getAvailableIOSchedulers() {
        String[] schedulers = null;
        String[] avail = null;
        String line = readOneLine(IO_SCHEDULER_PATH[0]);
        if (line != null) {
            avail = line.split(" ");
        }
        if (avail != null) {
            schedulers = new String[avail.length];
            for (int i = 0; i < avail.length; i++) {
                if (avail[i].charAt(0) == '[') {
                    schedulers[i] = avail[i].substring(1, avail[i].length() - 1);
                } else {
                    schedulers[i] = avail[i];
                }
            }
        }
        return schedulers;
    }

    public static void writeGamma(String color, int value) {
        if (fileExists(GAMMA_FILE)) {
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
            Utils.writeValue(GAMMA_FILE, gamma);
        } else {
            if (color.equals("red")) {
                if (fileExists(GAMMA_OLED_RED_FILE)) {
                    Utils.writeValue(GAMMA_OLED_RED_FILE, Integer.toString(value));
                }
            } else if (color.equals("green")) {
                if (fileExists(GAMMA_OLED_GREEN_FILE)) {
                    Utils.writeValue(GAMMA_OLED_GREEN_FILE, Integer.toString(value));
                }
            } else if (color.equals("blue")) {
                if (fileExists(GAMMA_OLED_BLUE_FILE)) {
                    Utils.writeValue(GAMMA_OLED_BLUE_FILE, Integer.toString(value));
                }
            }
        }
    }

    public static List<String> getGammaValues() {
        List<String> gammaValues = new ArrayList<String>();
        if (Utils.fileExists(GAMMA_FILE)) {
            String[] gamma = Utils.readOneLine(GAMMA_FILE).split(" ");
            gammaValues.add(gamma[0]);
            gammaValues.add(gamma[1]);
            gammaValues.add(gamma[2]);
        } else {
            gammaValues.add(Utils.readOneLine(GAMMA_OLED_RED_FILE));
            gammaValues.add(Utils.readOneLine(GAMMA_OLED_GREEN_FILE));
            gammaValues.add(Utils.readOneLine(GAMMA_OLED_BLUE_FILE));
        }
        return gammaValues;
    }

    public static boolean voltageFileExists() {
        if (new File(UV_MV_PATH).exists()) {
            setVoltagePath(UV_MV_PATH);
            return true;
        } else if (new File(VDD_PATH).exists()) {
            setVoltagePath(VDD_PATH);
            return true;
        } else if (new File(VDD_SYSFS_PATH).exists()) {
            setVoltagePath(VDD_SYSFS_PATH);
            return true;
        } else if (new File(COMMON_VDD_PATH).exists()) {
            setVoltagePath(COMMON_VDD_PATH);
            return true;
        } else if (new File(VDD_TABLE_PATH).exists()) {
            setVoltagePath(VDD_TABLE_PATH);
            return true;
        }
        return false;
    }

    public static void setVoltagePath(String voltageFile) {
        Log.d("Performance-Utils", "UV table path detected: " + voltageFile);
        mVoltagePath = voltageFile;
    }   

    public static boolean governorControlExists() {
        String gov = readOneLine(GOV_FILE).toLowerCase();
        File path;
        if (gov.equals("smartassv2")) {
            path = new File(GOVERNOR_CONTROL_PATH + "/smartass");
        } else {
            path = new File(GOVERNOR_CONTROL_PATH + "/" + gov);
        }
        if (path.exists()) {
            setGovernorControlPath(path);
            return true;
        }
        return false;
    }

    public static void setGovernorControlPath(File file) {
        mGovernorControlPath = file.toString();
    }

    public static String getVoltagePath() {
        return mVoltagePath;
    }

    public static String getGovernorControlPath() {
        return mGovernorControlPath;
    }

    public static List<String> getGovernorControl() {
        List<String> returnFiles = new ArrayList<String>();
        if (governorControlExists()) {
            File folder = new File(mGovernorControlPath);
            File[] files = folder.listFiles();
            for (File file : files) {
                returnFiles.add(file.getName());
            }
        }
        return returnFiles;
    }

    public static String getIOControlPath() {
        return mIOControlPath;
    }

    public static boolean IOControlExists() {
        String sched = getCurrentIOScheduler();
        File path = new File(IO_CONTROL_PATH);
        if (path.exists()) {
            setIOControlPath(path);
            return true;
        }
        return false;
    }

    public static void setIOControlPath(File file) {
        mIOControlPath = file.toString();
    }

    public static List<String> getIOControl() {
        List<String> returnFiles = new ArrayList<String>();
        if (IOControlExists()) {
            File folder = new File(mIOControlPath);
            File[] files = folder.listFiles();
            for (File file : files) {
                returnFiles.add(file.getName());
            }
        }
        return returnFiles;
    }

    public static boolean isGammaControlSupported() {
        if (new File(GAMMA_FILE).exists()) {
            return true;
        } else if (new File(GAMMA_OLED_RED_FILE).exists() &&
                new File(GAMMA_OLED_GREEN_FILE).exists() &&
                new File(GAMMA_OLED_BLUE_FILE).exists()) {
            return true;
        } else {
            return false;
        }
    }
}
