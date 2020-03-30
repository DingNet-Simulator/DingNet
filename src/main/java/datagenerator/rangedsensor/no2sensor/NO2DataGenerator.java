package datagenerator.rangedsensor.no2sensor;

import datagenerator.rangedsensor.abstractimpl.RangeDataGenerator;

public class NO2DataGenerator extends RangeDataGenerator {

    private static final String configFile = "/sensorsConfigurations/no2SensorConfig.toml";

    public NO2DataGenerator() {
        super(new NO2SensorConfigSpec());
    }

    @Override
    public int getAmountOfData() {
        return 2;
    }

    @Override
    protected String getDefaultConfigFilePath() {
        return configFile;
    }
}
