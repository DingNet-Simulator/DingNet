package datagenerator.rangedsensor.iaqsensor;

import datagenerator.SensorDataGenerator;
import datagenerator.rangedsensor.abstractimpl.RangeDataGenerator;


public class IAQDataGeneratorSingleton extends RangeDataGenerator {

    private static final String configFile = "/sensorsConfigurations/IAQSensorConfig.toml";

    private IAQDataGeneratorSingleton() {
        super(new IAQSensorConfigSpec());
    }

    public static SensorDataGenerator getInstance() {
        if (instance == null) {
            instance = new IAQDataGeneratorSingleton();
        }
        return instance;
    }

    @Override
    protected String getConfigFilePath() {
        return configFile;
    }
}
