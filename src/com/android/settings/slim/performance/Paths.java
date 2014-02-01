package com.android.settings.slim.performance;

public interface Paths {

    public static final String PREF_MAX_FREQ = "pref_max_freq";
    public static final String PREF_MIN_FREQ = "pref_min_freq";
    public static final String PREF_GOVERNOR = "pref_governor";
    public static final String PREF_IOSCHED = "pref_iosched";
    public static final String PREF_CPU_SOB = "pref_cpu_sob";
    public static final String PREF_GAMMA = "pref_gamma";
    public static final String PREF_RED_OLED = "pref_gamma_oled_red";
    public static final String PREF_GREEN_OLED = "pref_gamma_oled_green";
    public static final String PREF_BLUE_OLED = "pref_gamma_oled_blue";
    public static final String PREF_GAMMA_SOB = "pref_gamma_sob";
    public static final String PREF_GOVERNOR_CONTROL_SOB = "pref_governor_control_sob";
    public static final String PREF_IO_CONTROL_SOB = "pref_io_control_sob";
    public static final String PREF_VIBRATION_VAL = "pref_vibration_val";

    public static final String FREQ_LIST_FILE =
            "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";

    public static final String MAX_FREQ_FILE =
            "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    public static final String MIN_FREQ_FILE =
            "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";

    public static final String DYN_MAX_FREQ = "/sys/power/cpufreq_max_limit";
    public static final String DYN_MIN_FREQ = "/sys/power/cpufreq_min_limit";

    public static final String CPU_NUM_FILE = "/sys/devices/system/cpu/present";

    public static final String GOV_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    public static final String GOV_LIST_FILE =
            "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
    public static final String GOVERNOR_CONTROL_PATH = "/sys/devices/system/cpu/cpufreq";

    public static final String IO_CONTROL_PATH = "/sys/block/mmcblk0/queue/iosched";
    public static final String[] IO_SCHEDULER_PATH = {
            "/sys/block/mmcblk0/queue/scheduler",
            "/sys/block/mmcblk1/queue/scheduler"
    };

    // Voltage Control
    public static final String VOLTAGE_SOB = "voltage_sob";
    public static final String UV_MV_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table";
    public static final String VDD_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/vdd_levels";
    public static final String COMMON_VDD_PATH = "/sys/devices/system/cpu/cpufreq/vdd_levels";
    public static final String VDD_SYSFS_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/vdd_sysfs_levels";
    public static final String VDD_TABLE_PATH = "/sys/devices/system/cpu/cpufreq/vdd_table/vdd_levels";

    // N4 Voltage
    public static final String LOWER_UV_FILE = "/sys/module/acpuclock_krait/parameters/lower_uV";
    public static final String HIGHER_UV_FILE = "/sys/module/acpuclock_krait/parameters/higher_uV";
    public static final String HIGHER_KHZ_THRES_FILE =
            "/sys/module/acpuclock_krait/parameters/higher_khz_thres";
    public static final String UV_BOOST_FILE = "/sys/module/acpuclock_krait/parameters/boost";

    // Gamma
    public static final String GAMMA_FILE = "/sys/devices/platform/kcal_ctrl.0/kcal";
    public static final String GAMMA_CTRL_FILE = "/sys/devices/platform/kcal_ctrl.0/kcal_ctrl";
    public static final String GAMMA_OLED_RED_FILE = "/sys/class/misc/samoled_color/red_v1_offset";
    public static final String GAMMA_OLED_GREEN_FILE = "/sys/class/misc/samoled_color/green_v1_offset";
    public static final String GAMMA_OLED_BLUE_FILE = "/sys/class/misc/samoled_color/blue_v1_offset";

    // Vibration
    public static final String VIBRATION_PATH = "/sys/class/misc/pwm_duty/pwm_duty";

    // FCharge
    public static final String FCHARGE_PATH = "/sys/kernel/fast_charge/force_fast_charge";

}
