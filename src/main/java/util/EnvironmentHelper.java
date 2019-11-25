package util;

import iot.Environment;
import iot.networkentity.NetworkEntity;

import java.util.stream.Stream;

public class EnvironmentHelper {

    /**
     * Retrieve the {@link iot.networkentity.NetworkEntity} with the required id
     * @param env the environment that contains the entity
     * @param id the id of the required entity
     * @return the entity
     */
    public static NetworkEntity getNetworkEntityById(Environment env, long id) {
        return Stream.concat(env.getMotes().stream(), env.getGateways().stream())
            .filter(ne -> ne.getEUI() == id)
            .findFirst()
            .orElseThrow();
    }
}
