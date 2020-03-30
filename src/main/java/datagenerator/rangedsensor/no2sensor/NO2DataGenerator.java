package datagenerator.rangedsensor.no2sensor;

import datagenerator.rangedsensor.abstractimpl.RangeDataGenerator;

public class NO2DataGenerator extends RangeDataGenerator {

    private static final String DEFAULT_CONFIG_FILE = "/sensorsConfigurations/no2SensorConfig.toml";
    private final String configFile;

    public NO2DataGenerator() {
        this(DEFAULT_CONFIG_FILE);
    }

    public NO2DataGenerator(String configFile) {
        super(new NO2SensorConfigSpec(), configFile);
        this.configFile = configFile;
    }

    @Override
    public int getAmountOfData() {
        return 2;
    }

}
