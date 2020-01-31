package iot.mqtt;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import io.moquette.broker.Server;
import iot.lora.BasicFrameHeader;
import iot.lora.EU868ParameterByDataRate;
import iot.lora.FrameHeader;
import iot.lora.RegionalParameter;
import it.unibo.acdingnet.protelis.model.FrameHeaderApp;
import it.unibo.mqttclientwrapper.MQTTClientSingleton.ClientBuilder;
import it.unibo.mqttclientwrapper.MqttClientType;
import it.unibo.mqttclientwrapper.api.MqttClientBasicApi;
import util.Constants;
import util.Converter;
import util.SettingsReader;
import util.time.DoubleTime;
import util.time.Time;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;

/**
 * Factory to retrieve an instance of {@link MqttClientBasicApi}
 */
public class MQTTClientFactory {

    private final static MqttClientType DEFAULT_INSTANCE_TYPE = SettingsReader.getInstance().getMQTTClientType();
    private final static String ADDRESS = SettingsReader.getInstance().getMQTTServerAddress();
    private static MqttClientBasicApi clientBasicApi;
    private static Server server;

    /**
     *
     * @return the singleton instance of {@link MqttClientBasicApi} of the predefined type {@link MqttClientType}
     */
    public static MqttClientBasicApi getSingletonInstance() {
        if (clientBasicApi == null) {
            var builder = new ClientBuilder();
            if (DEFAULT_INSTANCE_TYPE == MqttClientType.MOCK_SERIALIZATION) {
                addAdapters(builder);
            }
            if (DEFAULT_INSTANCE_TYPE == MqttClientType.PAHO) {
                addAdapters(builder)
                    .setAddress(ADDRESS)
                    .setClientId(Constants.PAHO_CLIENT);
                if (ADDRESS.equals(Constants.MQTT_LOCALHOST_ADDRESS)) {
                    server = new Server();
                    try {
                        var prop = new Properties();
                        prop.load(MQTTClientFactory.class.getResourceAsStream("/config/moquette.conf"));
                        server.startServer(prop);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            clientBasicApi = builder.build(DEFAULT_INSTANCE_TYPE);
        }
        return clientBasicApi;
    }

    private static ClientBuilder addAdapters(ClientBuilder builder) {
        return builder
            .addSerializer(FrameHeader.class, (JsonSerializer<FrameHeader>) (header, type, context) -> {
                var obj = new JsonObject();
                obj.addProperty("sourceAddress", Base64.getEncoder().encodeToString(header.getSourceAddress()));
                obj.addProperty("fCtrl", header.getFCtrl());
                obj.addProperty("fCnt", header.getFCntAsShort());
                obj.addProperty("fOpts", Base64.getEncoder().encodeToString(header.getFOpts()));
                return obj;
            })
            .addDeserializer(FrameHeader.class, (JsonDeserializer<FrameHeader>) (jsonElement, type, jsonDeserializationContext) -> new BasicFrameHeader()
                .setSourceAddress(Base64.getDecoder().decode(((JsonObject) jsonElement).get("sourceAddress").getAsString()))
                .setFCnt(((JsonObject) jsonElement).get("fCnt").getAsShort())
                .setFCtrl(((JsonObject) jsonElement).get("fCtrl").getAsByte())
                .setFOpts(Base64.getDecoder().decode(((JsonObject) jsonElement).get("fOpts").getAsString())))
            .addDeserializer(FrameHeaderApp.class, (JsonDeserializer<FrameHeaderApp>) (jsonElement, type, jsonDeserializationContext) ->
                new FrameHeaderApp(
                    Arrays.asList(Converter.toObjectType(Base64.getDecoder().decode(((JsonObject) jsonElement).get("sourceAddress").getAsString()))),
                    ((JsonObject) jsonElement).get("fCnt").getAsInt(),
                    ((JsonObject) jsonElement).get("fCtrl").getAsInt(),
                    Arrays.asList(Converter.toObjectType(Base64.getDecoder().decode(((JsonObject) jsonElement).get("fOpts").getAsString())))
                )
            )
            .addDeserializer(RegionalParameter.class,
                (JsonDeserializer<RegionalParameter>) (element, type, context) -> EU868ParameterByDataRate.valueOf(element.getAsString()))
            .addSerializer(Time.class, (JsonSerializer<Time>) (header, type, context) -> {
                var obj = new JsonObject();
                obj.addProperty("time", header.asMilli());
                return obj;
            })
            .addDeserializer(Time.class,
                (JsonDeserializer<Time>) (element, type, context) -> new DoubleTime(((JsonObject) element).get("time").getAsDouble()));
    }
}
