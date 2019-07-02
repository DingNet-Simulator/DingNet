package IotDomain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * A class represting th MQTT Server
 */
public class MQTTServer implements Serializable{
    private static final long serialVersionUID = 1L;
    /**
     * The buffer of the server.
     */
    private HashMap<Long,LinkedList<BufferPair<LinkedList<Byte>,Long>>> buffer;
    /**
     * The list with subscriptions.
     */
    private HashMap<Long, HashMap<Long,Integer>> subscriptions;

    /**
     * A constructor generating a new MQTT server.
     */
    public MQTTServer(){
        buffer = new HashMap<>();
        subscriptions = new HashMap<>();
    }

    /**
     * A method for publishing a message from a device to an application given by a gateway.
     * @param message The message to publish.
     * @param deviceEUI The EUI of device which sent the message.
     * @param applicationEUI The EUI of application which should receive the message.
     * @param gatewayEUI The EUI of gateway which delivered the message.
     */
    public void publish(LinkedList<Byte> message, Long deviceEUI, Long applicationEUI, Long gatewayEUI){
        if(buffer.get(deviceEUI) != null){
            if(buffer.get(deviceEUI).contains(new BufferPair<>(message,gatewayEUI)) ){
                if(buffer.get(deviceEUI).get(buffer.get(deviceEUI).lastIndexOf(new BufferPair<>(message,gatewayEUI))).getRight() == gatewayEUI){
                    buffer.get(deviceEUI).add(new BufferPair<>(message,gatewayEUI));
                }
            }
            else{
                buffer.get(deviceEUI).add(new BufferPair<>(message,gatewayEUI));
            }
        }
        else {
            LinkedList<BufferPair<LinkedList<Byte>,Long>> list = new LinkedList<>();
            list.add(new BufferPair<>(message,gatewayEUI));
            buffer.put(deviceEUI, list);
            subscriptions.put(deviceEUI,new HashMap<>());
        }
    }

    /**
     * A method for an application to subscribe to a device.
     * @param applicationEUI The EUI of the application.
     * @param deviceEUI The EUI of the device.
     */
    public void subscribe(Long applicationEUI, Long deviceEUI){
        if(subscriptions.get(deviceEUI) != null)
            subscriptions.get(deviceEUI).put(applicationEUI,0);
        else{
            HashMap<Long,Integer> map = new HashMap<>();
            map.put(applicationEUI,0);
            buffer.put(deviceEUI, new LinkedList<>());
            subscriptions.put(deviceEUI,map);
        }

    }

    /**
     * Returns if an application is subscribed to a device.
     * @param applicationEUI The EUI of the application.
     * @param deviceEUI The EUI of the device.
     * @return True if an application is subscribed to a device.
     */
    public Boolean isSubscribed(Long applicationEUI, Long deviceEUI){
        if(subscriptions.get(applicationEUI)!=null && subscriptions.get(applicationEUI).get(deviceEUI)!= null){
            return true;
        }
        return false;
    }

    /**
     * Returns if an application has data to receive from a device.
     * @param applicationEUI The EUI of the application.
     * @param deviceEUI The EUI of the device.
     * @return True if an application is subscribed to a device and has data that it has not yet received.
     */
    public Boolean hasNext(Long applicationEUI, Long deviceEUI){
        if(buffer.get(deviceEUI) != null && isSubscribed(applicationEUI, deviceEUI)){
            if(buffer.get(deviceEUI).size()-1 >= subscriptions.get(applicationEUI).get(deviceEUI)){
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    /**
     * Returns the data that the gateway has not yet received from the device.
     * @param applicationEUI The EUI of the application.
     * @param deviceEUI The EUI of the device.
     * @return The data that it has not yet received.
     */
    public LinkedList<Byte> getNext(Long applicationEUI, Long deviceEUI){
        if(isSubscribed(applicationEUI, deviceEUI) && hasNext(applicationEUI, deviceEUI)){
            return buffer.get(deviceEUI).get(subscriptions.get(applicationEUI).get(deviceEUI)).getLeft();
        }
        return null;
    }

    public LinkedList<LinkedList<Byte>> getData(Long applicationEUI, Long deviceEUI){
        LinkedList<LinkedList<Byte>> data = new LinkedList<>();
        while (hasNext(applicationEUI,deviceEUI)){
            data.add(getNext(applicationEUI,deviceEUI));
        }
        return data;
    }
}
