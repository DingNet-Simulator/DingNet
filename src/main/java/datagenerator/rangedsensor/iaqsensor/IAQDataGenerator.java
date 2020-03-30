package datagenerator.rangedsensor.iaqsensor;

import datagenerator.rangedsensor.abstractimpl.RangeDataGenerator;

public class IAQDataGenerator extends RangeDataGenerator {

    private static final String configFile = "/sensorsConfigurations/IAQSensorConfig.toml";

    public IAQDataGenerator() {
        super(new IAQSensorConfigSpec());
    }

    @Override
    protected String getDefaultConfigFilePath() {
        return configFile;
    }
}
