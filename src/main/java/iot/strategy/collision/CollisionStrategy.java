package iot.strategy.collision;

import iot.networkcommunication.LoraTransmission;

public interface CollisionStrategy {

    void manageCollision(LoraTransmission loraTransmission);
}
