package iot.mqtt;

import iot.Environment;
import iot.networkentity.Gateway;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.List;

public class CallBack implements MqttCallback {
    private List<Gateway> gateways;
    private PahoMqttClient pahoMqttClient;
    public CallBack( List<Gateway> gateways,PahoMqttClient pahoMqttClient){
        this.gateways = gateways;
        this.pahoMqttClient = pahoMqttClient;
    }
    @Override
    public void connectionLost(Throwable throwable) {
        pahoMqttClient.connect();
        for (Gateway gateway : gateways){
            gateway.subscribeToMotes();
        }
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
