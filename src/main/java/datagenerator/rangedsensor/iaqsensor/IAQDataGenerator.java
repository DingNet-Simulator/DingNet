package datagenerator.rangedsensor.iaqsensor;

import datagenerator.rangedsensor.abstractimpl.RangeDataGenerator;

public class IAQDataGenerator extends RangeDataGenerator {

    private static final String DEFAULT_CONFIG_FILE = "/sensorsConfigurations/IAQSensorConfig.toml";
    private final String configFile;

    public IAQDataGenerator(String configFile) {
        super(new IAQSensorConfigSpec(), configFile);
        this.configFile = configFile;
    }

    public IAQDataGenerator() {
        this(DEFAULT_CONFIG_FILE);
    }
}
