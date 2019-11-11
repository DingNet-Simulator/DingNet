package application;

import iot.Environment;
import iot.lora.MessageType;
import iot.mqtt.MqttMessage;
import iot.networkentity.MoteSensor;
import util.MapHelper;
import util.pollution.PollutionGrid;
import util.pollution.PollutionLevel;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PollutionMonitor extends Application {

    private PollutionGrid pollutionGrid;
    // TODO alternative way of retrieving the mote position? Store explicitly in header?
    private Environment environment;


    public PollutionMonitor(Environment environment) {
        super(List.of("application/+/node/+/rx"));

        this.pollutionGrid = PollutionGrid.getInstance();
        this.environment = environment;
    }


    private double determinePollutionLevel(Map<MoteSensor, Byte[]> sensorData) {
        // NOTE: only consider IAQ sensors for now
        return sensorData.entrySet().stream()
            .filter(me -> me.getKey().equals(MoteSensor.IAQ))
            .map(Map.Entry::getValue)
            .flatMap(Arrays::stream) // Can do this here since we filter the IAQ sensor (we know it generates a single byte)
            .mapToDouble(b -> (b.intValue() - 1) / 4.0)
            .average()
            .orElse(0.0);
    }

    private void handleSensorData(MqttMessage message) {
        // Filter out the first byte
        var body = message.getData().subList(1, message.getData().size());
        if (body.isEmpty()) {
            return;
        }

        var mote = this.environment.getMotes().stream()
            .filter(m -> m.getEUI() == message.getDeviceEUI())
            .findFirst()
            .orElseThrow();

        // Retrieve the position of the mote
        // TODO is this position even correct when getting it at this point? Has it changed since the transmission of the data?
        var position = MapHelper.getInstance().toGeoPosition(mote.getPosInt());

        // Retrieve the individual sensor readings
        Map<MoteSensor, Byte[]> sensorData = this.retrieveSensorData(mote, body);


        // Make sure the IAQ sensor is present in the currently processed mote
        if (!sensorData.containsKey(MoteSensor.IAQ)) {
            return;
        }

        this.pollutionGrid.addMeasurement(message.getDeviceEUI(), position, new PollutionLevel(this.determinePollutionLevel(sensorData)));
    }

    @Override
    public void consumePackets(String topicFilter, MqttMessage message) {
        // Only handle packets with sensor data
        if (message.getData().get(0) == MessageType.SENSOR_VALUE.getCode()) {
            handleSensorData(message);
        }
    }
}
