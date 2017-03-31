package uom.synergen.fyp.smart_insole.gait_analysis.mqtt;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import uom.synergen.fyp.smart_insole.gait_analysis.constants.SmartInsoleConstants;

public class MqttConnector {

    private static final String TAG = "Mqtt Connector";
    private MqttAndroidClient client;
    private static MqttConnector connector;

    private MqttConnector(Context context , String url, String userName) {

        MemoryPersistence memPer = new MemoryPersistence();
        client = new MqttAndroidClient(context,url, userName,memPer);
    }

    public static MqttConnector getInstance(Context context) {
        if(connector == null) {
            connector = new MqttConnector(context, SmartInsoleConstants.MQTT_URL,
                    SmartInsoleConstants.MQTT_USER);
        }
        return connector;
    }

    public void connect() throws MqttException {

        IMqttToken token = client.connect();

        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.i(TAG, "Connected");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.i(TAG, "Failure");
            }
        });

    }

    public void publish(String topic, String payload, int qos, boolean retained) throws MqttException {

        if(client.isConnected()) {
            MqttMessage message = new MqttMessage();
            message.setQos(qos);
            message.setRetained(retained);
            message.setPayload(payload.getBytes());
            client.publish(topic, message);
        }
    }

    public void publish(String topic, String payload) throws MqttException {

        publish(topic, payload, 0, false);
    }

}
