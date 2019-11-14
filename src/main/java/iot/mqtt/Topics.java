package iot.mqtt;

public class Topics {

    private final static String UPSTREAM_SUFFIX = "/rx";
    private final static String DOWNSTREAM_SUFFIX = "/tx";

    public static String getGatewayToNetServer(long applicationId, long gatewayId, long nodeId) {
        return getGatewayToNetServer(""+applicationId, ""+gatewayId, ""+nodeId);
    }

    public static String getGatewayToNetServer(String applicationId, String gatewayId, String nodeId) {
        return createTopicWithGateway(applicationId, gatewayId, nodeId, UPSTREAM_SUFFIX);
    }

    public static String getNetServerToGateway(long applicationId, long gatewayId, long nodeId) {
        return getNetServerToGateway(""+applicationId, ""+gatewayId, ""+nodeId);
    }

    public static String getNetServerToGateway(String applicationId, String gatewayId, String nodeId) {
        return createTopicWithGateway(applicationId, gatewayId, nodeId, DOWNSTREAM_SUFFIX);
    }

    public static String getNetServerToApp(long applicationId, long nodeId) {
        return getNetServerToApp(""+applicationId, ""+nodeId);
    }

    public static String getNetServerToApp(String applicationId, String nodeId) {
        return createTopic(applicationId, nodeId, UPSTREAM_SUFFIX);
    }

    public static String getAppToNetServer(long applicationId, long nodeId) {
        return getAppToNetServer(""+applicationId, ""+nodeId);
    }

    public static String getAppToNetServer(String applicationId, String nodeId) {
        return createTopic(applicationId, nodeId, DOWNSTREAM_SUFFIX);
    }

    private static String createTopic(String applicationId, String nodeId, String suffix) {
        return new StringBuilder()
            .append("application/")
            .append(applicationId)
            .append("/node/")
            .append(nodeId)
            .append(suffix)
            .toString();
    }

    private static String createTopicWithGateway(String applicationId, String gatewayId, String nodeId, String suffix) {
        return new StringBuilder()
            .append("application/")
            .append(applicationId)
            .append("/gateway/")
            .append(gatewayId)
            .append("/node/")
            .append(nodeId)
            .append(suffix)
            .toString();
    }
}
