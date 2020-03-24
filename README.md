# DingNet

[![Build Status](https://travis-ci.com/Placu95/DingNet.svg?branch=protelisOverMqtt)](https://travis-ci.com/Placu95/DingNet)

The source code for the DingNet simulator.

Current up to date version: **1.2.1.**


## Building the simulator

To build the simulator, simply run the command `gradlew build`. The generated source are placed in the `build` folder.
The simulator can then be run with the following command: `gradlew run`.

Alternatively, run the command `gradlew shadowJar`. This will generate a jar file under the build/libs directory: `DingNet-{version}.jar`.

Similarly to the previously listed commands, `gradlew test` runs the tests for the project and `gradlew chack` extend it performing style check.

## Running the simulator

Either run the jar file generated from the previous step, or use the `gradlew run` command.

## Simulate Protelis application

The simulator support execution of [Protelis](https://github.com/Protelis/Protelis) application with version 13.0.3.

The protelis backend provided from this simulator is MQTT-based and it use the same MQTT server of the LoRa network.
At every LoRa mote of the simulation correspond a protelis node, and its ExecutionContext put all 
the sensed value received from the LoRa mote to its environment.

The name of the protelis program to execute is retrieved from the `InputProfile` under the xml tag `protelisProgram`.
(Currently the only way to define the protelis program to execute is to add it manually in the xml file,
in further it will be also possible with the simulator GUI)

## Future goals


- Rewrite of adaptation logic
- Project consistency (e.g. adjust gateway position assignment to geo-coordinates instead of legacy integer values)
- Refactor InputProfile and QualityOfService
- Rewrite transmission logic
- Provide means of realistic sensor data generation
- Improve testability of project (e.g. removal of singletons where applicable)
