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
