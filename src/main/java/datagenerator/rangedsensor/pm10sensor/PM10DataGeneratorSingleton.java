package datagenerator.rangedsensor.pm10sensor;

import datagenerator.SensorDataGenerator;
import datagenerator.rangedsensor.abstractimpl.RangeDataGenerator;

public class PM10DataGeneratorSingleton extends RangeDataGenerator {

    private static final String configFile = "/sensorsConfigurations/pm10SensorConfig.toml";

    private PM10DataGeneratorSingleton() {
        super(new PM10SensorConfigSpec());
    }

    public static SensorDataGenerator getInstance() {
        if (instance == null) {
            instance = new PM10DataGeneratorSingleton();
        }
        return instance;
    }

    @Override
    protected String getConfigFilePath() {
        return configFile;
    }
}
