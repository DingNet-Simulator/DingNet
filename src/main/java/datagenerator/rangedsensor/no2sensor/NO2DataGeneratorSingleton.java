package datagenerator.rangedsensor.no2sensor;

import datagenerator.SensorDataGenerator;
import datagenerator.rangedsensor.abstractimpl.RangeDataGenerator;

public class NO2DataGeneratorSingleton extends RangeDataGenerator {

    private static final String configFile = "/sensorsConfigurations/no2SensorConfig.toml";

    private NO2DataGeneratorSingleton() {
        super(new NO2SensorConfigSpec());
    }

    public static SensorDataGenerator getInstance() {
        if (instance == null) {
            instance = new NO2DataGeneratorSingleton();
        }
        return instance;
    }

    @Override
    public int getAmountOfData() {
        return 2;
    }

    @Override
    protected String getConfigFilePath() {
        return configFile;
    }
}
