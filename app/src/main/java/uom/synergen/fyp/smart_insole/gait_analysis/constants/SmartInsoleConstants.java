package uom.synergen.fyp.smart_insole.gait_analysis.constants;

public class SmartInsoleConstants {

    public static final short [][] LEFT_LEG_PRESSURE_POINTS = {{150,164},{117,161},{89,166},{66,183},{42,204},
            {146,263},{112,244},{85,252},{64,266},{44,292},{46,330},{50,374},{60,443},{110,469},{80,528},{114,385}};

    public static final short [][] RIGHT_LEG_PRESSURE_POINTS = {{261,164},{294,161},{322,166},{345,183},{369,204},
            {265,263},{299,244},{326,252},{347,266},{367,292},{365,330},{361,374},{351,443},{301,469},{331,528},{297,385}};

    public static final String MQTT_URL = "tcp://smartinsole.projects.mrt.ac.lk:1883";
    public static final String MQTT_USER = "smart_insole_android_app";
    public static final String MQTT_SERVER_TOPIC = "smart_insole_server";

    public static final String CARE_GIVER_PHONE_NUMBER = "";
    public static final String fallDetectMessage = "Person Falling";

    public static final int WIFI_PORT = 1234;

    public static final int P1_REF_LOW = -50;
    public static final int P1_REF_HIGH = 1;
    public static final int P7_REF_LOW = -50;
    public static final int P7_REF_HIGH = 1;
    public static final int DELTA_GX = 6;

}
