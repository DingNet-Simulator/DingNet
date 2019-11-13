package IotDomain.collisionstrategy;

import IotDomain.networkcommunication.LoraTransmission;

public interface CollisionStrategy {

    void manageCollision(LoraTransmission loraTransmission);
}
