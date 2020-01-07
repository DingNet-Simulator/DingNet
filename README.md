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

## Libraries

DingNet uses the following libraries:
- AnnotationsDoclets (included in the lib folder, since it is not available online (yet))
- jfreechart-1.5.0
- jxmapviewer2-2.4


## Future goals

- [ ] Refactor Inputprofile
- [ ] Refactor QualityOfService
- [ ] Realistic data generation
- [ ] Rewrite transmission logic (moveTo, transmission power, ...)
- [ ] \(Not important) Allow creation of circular routes for motes
- [ ] update used library on README
