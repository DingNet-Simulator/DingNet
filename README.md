# DingNet
The source code for the DingNet simulator.

## Libraries

DingNet uses the following libraries:
- AnnotationsDoclets (included in the lib folder, since it is not available online)
- jfreechart-1.5.0
- jxmapviewer2-2.4


## Building the simulator

To build the simulator, simply run the command `mvn compile`. This will generate a jar file under the target directory: `DingNet-{version}-jar-with-dependencies.jar`.


## Running the simulator

Run the jar file generated from the previous step.
<!-- A jar file is exported to the folder DingNetExe which also contains the correct file structure. Run the jar file to run the simulator.
The simulator can also be started from the main method in the MainGUI class. -->

## TODO

- [ ] Refactor GUI
- [ ] Implement clock-based simulation, not event-based
- [ ] Correct bug concerning event-based handeling of recieved signal
- [ ] Refactor Inputprofile
- [ ] Refactor QualityOfService