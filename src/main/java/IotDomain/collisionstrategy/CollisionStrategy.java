package IotDomain.collisionstrategy;

import IotDomain.lora.LoraTransmission;

public interface CollisionStrategy {

    void manageCollision(LoraTransmission loraTransmission);
}
