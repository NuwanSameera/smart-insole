package uom.synergen.fyp.smart_insole.gait_analysis.mqtt;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttConnector {

    private static final String TAG = "Mqtt Connector";
    private MqttAndroidClient client;
    private static MqttConnector connector;
    private boolean connected;


    private MqttConnector(Context context , String url, String userName) {

        MemoryPersistence memPer = new MemoryPersistence();
        client = new MqttAndroidClient(context,url, userName,memPer);

    }

    public MqttConnector getInstance(Context context, String url, String userName) {
        if(connector == null) {
            connector = new MqttConnector(context, url, userName);
        }
        return connector;
    }

    public boolean connect() throws MqttException {
        client.connect(null, new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken mqttToken) {
                    Log.i(TAG, "Connected.");
                    connected = true;

                }

                @Override
                public void onFailure(IMqttToken arg0, Throwable arg1) {
                    Log.e(TAG, "Connection Failed : " + arg1);
                    connected = false;
                }
            });

            return connected;

    }

    public void publish(String topic, String payload, int qos, boolean retained) throws MqttException {
         MqttMessage message = new MqttMessage();
         message.setQos(qos);
         message.setRetained(retained);
         message.setPayload(payload.getBytes());
         client.publish(topic, message);
    }


}
