# DingNet
The source code for the DingNet simulator.

Current up to date version: **1.2.1.**


## Building the simulator

To build the simulator, simply run the command `mvn compile`. The generated source are placed in the `target` folder.
The simulator can then be run with the following command: `mvn exec:java`.

Alternatively, run the command `mvn package`. This will generate a jar file under the target directory: `DingNet-{version}-jar-with-dependencies.jar`.

Similarly to the previously listed commands, `mvn test` runs the tests for the project.

## Running the simulator

Either run the jar file generated from the previous step, or use the maven exec plugin.
<!-- A jar file is exported to the folder DingNetExe which also contains the correct file structure. Run the jar file to run the simulator.
The simulator can also be started from the main method in the MainGUI class. -->



## Future goals

- Rewrite of adaptation logic
- Project consistency (e.g. adjust gateway position assignment to geo-coordinates instead of legacy integer values)
- Refactor InputProfile and QualityOfService
- Rewrite transmission logic
- Provide means of realistic sensor data generation
- Improve testability of project (e.g. removal of singletons where applicable)
