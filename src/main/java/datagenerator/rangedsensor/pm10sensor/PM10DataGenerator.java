package datagenerator.rangedsensor.pm10sensor;

import datagenerator.rangedsensor.abstractimpl.RangeDataGenerator;

public class PM10DataGenerator extends RangeDataGenerator {

    private static final String DEFAULT_CONFIG_FILE = "/sensorsConfigurations/pm10SensorConfig.toml";
    private final String configFile;

    public PM10DataGenerator() {
        this(DEFAULT_CONFIG_FILE);
    }

    public PM10DataGenerator(String configFile) {
        super(new PM10SensorConfigSpec(), configFile);
        this.configFile = configFile;
    }
}
